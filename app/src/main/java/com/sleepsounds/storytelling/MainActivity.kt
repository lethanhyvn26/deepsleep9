package com.sleepsounds.storytelling

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleepsounds.storytelling.ads.AdMobManager
import com.sleepsounds.storytelling.data.billing.BillingRepository
import com.sleepsounds.storytelling.data.remote.ShopCatalogRepository
import com.sleepsounds.storytelling.data.repository.UserPreferencesRepository
import com.sleepsounds.storytelling.di.CatalogSyncScheduler
import com.sleepsounds.storytelling.presentation.navigation.DeepSleepNavHost
import com.sleepsounds.storytelling.presentation.splash.CustomSplashScreen
import com.sleepsounds.storytelling.presentation.splash.StartupSplashCoordinator
import com.sleepsounds.storytelling.presentation.splash.WelcomeBackOverlay
import com.sleepsounds.storytelling.service.AudioPlayerManager
import com.sleepsounds.storytelling.service.PlaybackDeepLinks
import com.sleepsounds.storytelling.service.PlaybackMode
import com.sleepsounds.storytelling.service.PlaybackNavigationManager
import com.sleepsounds.storytelling.service.SleepTimerManager
import com.sleepsounds.storytelling.ui.theme.DeepSleepTheme
import com.sleepsounds.storytelling.ui.theme.ProvideAppLocale
import com.sleepsounds.storytelling.util.StartupTrace
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Single-activity host for Deep Sleep v3.1.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var audioPlayerManager: AudioPlayerManager

    @Inject
    lateinit var sleepTimerManager: SleepTimerManager

    @Inject
    lateinit var playbackNavigationManager: PlaybackNavigationManager

    @Inject
    lateinit var catalogSyncScheduler: CatalogSyncScheduler

    @Inject
    lateinit var shopCatalogRepository: ShopCatalogRepository

    @Inject
    lateinit var startupSplashCoordinator: StartupSplashCoordinator

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var adMobManager: AdMobManager

    @Inject
    lateinit var billingRepository: BillingRepository

    private var pendingDeepLinkRoute by mutableStateOf<String?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* optional notification permission result */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        StartupTrace.mark("MainActivity.onCreate:enter")
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        super.onCreate(savedInstanceState)

        val isColdStart = savedInstanceState == null
        val openedViaNotification = intent?.getBooleanExtra(EXTRA_FROM_NOTIFICATION, false) == true

        if (isColdStart) {
            startupSplashCoordinator.resetForFreshLaunch()
            shopCatalogRepository.reshuffleForSession()
            if (!openedViaNotification && !audioPlayerManager.playbackState.value.isPlaying) {
                sleepTimerManager.cancelTimer()
                audioPlayerManager.stopAll()
            }
        }

        consumeNotificationLaunchFlag(intent)
        extractDeepLinkTarget(intent, fromNotification = openedViaNotification)
        audioPlayerManager.setAutoNavigateEnabled(true)

        setContent {
            val activeLanguage by userPreferencesRepository.appLanguage
                .collectAsStateWithLifecycle(initialValue = UserPreferencesRepository.LANGUAGE_EN)
            val hasSelectedLanguage by userPreferencesRepository.hasSelectedLanguageFirstOpen
                .collectAsStateWithLifecycle(initialValue = false)
            val hasCompletedOnboarding by userPreferencesRepository.hasCompletedOnboarding
                .collectAsStateWithLifecycle(initialValue = false)

            ProvideAppLocale(languageCode = activeLanguage) {
                DeepSleepTheme {
                    val isSplashShown by startupSplashCoordinator.splashVisible
                        .collectAsStateWithLifecycle()
                    val isColdStartDone by startupSplashCoordinator.coldStartSequenceCompleted
                        .collectAsStateWithLifecycle()
                    val isShowingFullScreenAd by adMobManager.isShowingFullScreenAdState
                        .collectAsStateWithLifecycle()
                    val isWelcomeBackVisible by adMobManager.welcomeBackVisible
                        .collectAsStateWithLifecycle()
                    val sessionToken by startupSplashCoordinator.launchSessionId
                        .collectAsStateWithLifecycle()

                    LaunchedEffect(isWelcomeBackVisible, isSplashShown) {
                        if (isWelcomeBackVisible && !isSplashShown) {
                            delay(350L)
                            adMobManager.showWelcomeBackAd(this@MainActivity)
                        }
                    }

                    LaunchedEffect(
                        isSplashShown,
                        isColdStartDone,
                        isShowingFullScreenAd,
                        isWelcomeBackVisible,
                        hasSelectedLanguage,
                        hasCompletedOnboarding,
                    ) {
                        if (!isSplashShown &&
                            isColdStartDone &&
                            !isShowingFullScreenAd &&
                            !isWelcomeBackVisible &&
                            hasSelectedLanguage &&
                            hasCompletedOnboarding
                        ) {
                            delay(600L)
                            if (!adMobManager.isShowingFullScreenAdState.value) {
                                promptNotificationPermissionIfRequired()
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        DeepSleepNavHost(
                            audioPlayerManager = audioPlayerManager,
                            sleepTimerManager = sleepTimerManager,
                            playbackNavigationManager = playbackNavigationManager,
                            adMobManager = adMobManager,
                            isSplashVisible = isSplashShown,
                            initialRoute = pendingDeepLinkRoute,
                            onDeepLinkConsumed = { pendingDeepLinkRoute = null },
                            launchSessionId = sessionToken,
                            onContentFirstFrame = startupSplashCoordinator::onContentReady,
                        )
                        WelcomeBackOverlay(
                            visible = isWelcomeBackVisible && !isSplashShown,
                            adMobManager = adMobManager,
                        )
                        CustomSplashScreen(visible = isSplashShown)
                    }
                }
            }
        }

        window.decorView.post {
            if (!isFinishing && !isDestroyed) {
                startupSplashCoordinator.begin()
            }
        }
        StartupTrace.mark("MainActivity.onCreate:exit")
    }

    override fun onResume() {
        super.onResume()
        catalogSyncScheduler.syncOnResume()
        billingRepository.refreshCatalogAndPurchases()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val openedViaNotification = intent.getBooleanExtra(EXTRA_FROM_NOTIFICATION, false)
        consumeNotificationLaunchFlag(intent)
        extractDeepLinkTarget(intent, fromNotification = openedViaNotification)
    }

    private fun consumeNotificationLaunchFlag(incomingIntent: Intent?) {
        if (incomingIntent?.getBooleanExtra(EXTRA_FROM_NOTIFICATION, false) != true) return
        incomingIntent.removeExtra(EXTRA_FROM_NOTIFICATION)
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            startupSplashCoordinator.markOpenedFromNotification()
        }
    }

    private fun extractDeepLinkTarget(incomingIntent: Intent?, fromNotification: Boolean) {
        val explicitTarget = incomingIntent?.getStringExtra(EXTRA_ROUTE)?.takeIf { it.isNotBlank() }
        incomingIntent?.removeExtra(EXTRA_ROUTE)
        val currentPlayback = audioPlayerManager.playbackState.value
        val targetRoute = when {
            fromNotification && currentPlayback.mode != PlaybackMode.IDLE ->
                PlaybackDeepLinks.routeFor(currentPlayback)
            !explicitTarget.isNullOrBlank() -> explicitTarget
            else -> return
        }
        pendingDeepLinkRoute = targetRoute
        playbackNavigationManager.requestDeepLink(targetRoute)
    }

    private fun promptNotificationPermissionIfRequired() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val alreadyGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!alreadyGranted) {
                runCatching {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    override fun onDestroy() {
        StartupTrace.mark("MainActivity.onDestroy")
        if (!isChangingConfigurations) {
            startupSplashCoordinator.resetForFreshLaunch()
        }
        super.onDestroy()
    }

    companion object {
        const val EXTRA_ROUTE = "extra_route"
        const val EXTRA_FROM_NOTIFICATION = "extra_from_notification"
    }
}

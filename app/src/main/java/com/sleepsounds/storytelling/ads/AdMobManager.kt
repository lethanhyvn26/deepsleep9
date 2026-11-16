package com.sleepsounds.storytelling.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.sleepsounds.storytelling.BuildConfig
import com.sleepsounds.storytelling.data.repository.UserPreferencesRepository
import com.sleepsounds.storytelling.di.ApplicationScope
import com.sleepsounds.storytelling.service.AudioPlayerManager
import com.sleepsounds.storytelling.util.R2Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.math.min

/**
 * Unwraps any nested [ContextWrapper] (including `ProvideAppLocale`, `ModalBottomSheet`, or dialog contexts)
 * to return the underlying host [Activity], or `null` if called from an Application/Service context.
 */
tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * High-Fill, Look-Ahead Preloading AdMob Manager modeled 100% after the reference APK's
 * ad funnel (`Splash Dual Ad + Look-Ahead Preload -> Language Click-Swap -> Onboarding ->
 * Sticky Collapsible Home + inter_home + inter_back + WelcomeBack Resume`).
 */
@Singleton
class AdMobManager @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val audioPlayerManager: AudioPlayerManager,
    @param:ApplicationScope private val appScope: CoroutineScope,
) : Application.ActivityLifecycleCallbacks {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val isSdkInitialized = AtomicBoolean(false)
    private val isSdkInitializing = AtomicBoolean(false)

    private val _sdkReady = MutableStateFlow(false)
    val sdkReady: StateFlow<Boolean> = _sdkReady.asStateFlow()

    private val _networkAvailableTick = MutableStateFlow(0L)
    val networkAvailableTick: StateFlow<Long> = _networkAvailableTick.asStateFlow()

    private val _isRewardedAdReady = MutableStateFlow(false)
    val isRewardedAdReady: StateFlow<Boolean> = _isRewardedAdReady.asStateFlow()

    private val _isLoadingRewarded = MutableStateFlow(false)
    val isLoadingRewarded: StateFlow<Boolean> = _isLoadingRewarded.asStateFlow()

    private val _isShowingFullScreenAdState = MutableStateFlow(false)
    val isShowingFullScreenAdState: StateFlow<Boolean> = _isShowingFullScreenAdState.asStateFlow()

    private val _welcomeBackVisible = MutableStateFlow(false)
    val welcomeBackVisible: StateFlow<Boolean> = _welcomeBackVisible.asStateFlow()

    private val clockTickerMs = MutableStateFlow(System.currentTimeMillis())

    val isPaidVip: StateFlow<Boolean> = userPreferencesRepository.isPaidSubscriber
        .stateIn(appScope, SharingStarted.Eagerly, false)

    val rewardedAdWatchCount: StateFlow<Int> = userPreferencesRepository.rewardedAdWatchCount
        .stateIn(appScope, SharingStarted.Eagerly, 0)

    val rewardedAdFreeUntilMs: StateFlow<Long> = userPreferencesRepository.rewardedAdFreeUntilMs
        .stateIn(appScope, SharingStarted.Eagerly, 0L)

    /**
     * `true` when the user has unlocked the 3-Hour Ad-Free & Full VIP Pass by watching 20 Rewarded Ads.
     */
    val isRewardedAdFreeActive: StateFlow<Boolean> = combine(
        rewardedAdFreeUntilMs,
        clockTickerMs,
    ) { untilMs, nowMs ->
        untilMs > nowMs
    }.stateIn(appScope, SharingStarted.Eagerly, false)

    /**
     * `true` when Banner, Interstitial, and App Open ads should be shown.
     * Automatically becomes `false` for Paid VIP subscribers OR during the 3-Hour Rewarded Ad-Free Pass.
     */
    val shouldShowAds: StateFlow<Boolean> = combine(
        isPaidVip,
        rewardedAdFreeUntilMs,
        clockTickerMs,
    ) { paidVip, adFreeUntil, nowMs ->
        !paidVip && (adFreeUntil <= nowMs)
    }.stateIn(appScope, SharingStarted.Eagerly, true)

    private var currentActivityRef: WeakReference<Activity>? = null
    private var startedNonAdActivities = 0
    private var hasGoneToBackground = false
    private var backgroundedAtElapsedMs: Long = 0L
    private val coldStartAppOpenHandled = AtomicBoolean(false)
    private val coldStartAppOpenFailed = AtomicBoolean(false)
    private val coldStartFullScreenShown = AtomicBoolean(false)
    private var coldStartTimedOutAtElapsedMs: Long = 0L
    private var isShowingFullScreenAd = false
    private var lastFullScreenDismissedElapsedMs: Long = 0L
    private var wasNetworkDisconnected = false
    private var interstitialRequestedBeforeInit = false
    private var rewardedRequestedBeforeInit = false

    fun currentHostActivity(): Activity? =
        currentActivityRef?.get()?.takeIf { it !is AdActivity && !it.isFinishing && !it.isDestroyed }

    private fun setFullScreenAdShowing(showing: Boolean) {
        isShowingFullScreenAd = showing
        _isShowingFullScreenAdState.value = showing
    }

    private val pendingForegroundAppOpenRunnable = Runnable {
        val host = currentHostActivity()
        if (host != null && !isShowingFullScreenAd && shouldShowAds.value) {
            // Modeled after `WelcomeBackActivity` in the reference APK:
            // Show the sleek WelcomeBackOverlay and trigger the Welcome Back ad (AppOpen or inter_welcome_back fallback)
            _welcomeBackVisible.value = true
            mainHandler.postDelayed(
                {
                    val liveHost = currentHostActivity()
                    if (liveHost != null && _welcomeBackVisible.value) {
                        showWelcomeBackAd(liveHost)
                    } else {
                        _welcomeBackVisible.value = false
                    }
                },
                380L,
            )
        } else {
            _welcomeBackVisible.value = false
        }
    }

    // Event counter for Interstitial Ad: triggers on every 3rd click (1 -> 2 -> 3 -> show ad & reset to 0)
    private val interstitialActionCounter = AtomicInteger(0)
    private var lastActionRecordedElapsedMs: Long = 0L

    // Single-slot App Open cache
    private var appOpenAd: AppOpenAd? = null
    private var appOpenLoadedElapsedMs: Long = 0L
    private var isPreloadingAppOpen = false
    private var appOpenLoadStartedElapsedMs: Long = 0L
    private var lastAppOpenLoadAttemptElapsedMs: Long = 0L
    private var appOpenRetryAttempt = 0
    private var lastAppOpenShownElapsedMs: Long = 0L
    private val appOpenRetryRunnable = Runnable { preloadAppOpenAd() }

    // Warm Interstitial cache
    private data class LoadedInterstitialSlot(
        val ad: InterstitialAd,
        val loadedElapsedMs: Long,
    )
    private val interstitialAdPool = ArrayDeque<LoadedInterstitialSlot>(MAX_INTERSTITIAL_POOL_SIZE)
    private var isPreloadingInterstitial = false
    private var interstitialLoadStartedElapsedMs: Long = 0L
    private var lastInterstitialLoadAttemptElapsedMs: Long = 0L
    private var interstitialLastLoadFailed = false
    private var interstitialRetryAttempt = 0
    private val interstitialRetryRunnable = Runnable { preloadInterstitialAd() }
    private val interstitialLoadWatchdogRunnable = Runnable {
        if (isPreloadingInterstitial) {
            R2Log.w("AdMob Interstitial preload watchdog triggered; resetting hung load state")
            isPreloadingInterstitial = false
            scheduleInterstitialRetry()
        }
    }

    // Single-slot Rewarded cache
    private data class LoadedRewardedSlot(
        val ad: RewardedAd,
        val loadedElapsedMs: Long,
    )
    private val rewardedAdPool = ArrayDeque<LoadedRewardedSlot>(MAX_REWARDED_POOL_SIZE)
    private var isPreloadingRewarded = false
    private var rewardedLoadStartedElapsedMs: Long = 0L
    private var lastRewardedLoadAttemptElapsedMs: Long = 0L
    private var rewardedLastLoadFailed = false
    private var rewardedLoadGeneration: Int = 0
    private var rewardedRetryAttempt = 0
    private var pendingRewardedWaiter: ((RewardedAd?) -> Unit)? = null
    private var activeRewardedTimeoutRunnable: Runnable? = null
    private val rewardedRetryRunnable = Runnable { preloadRewardedAd() }
    private val rewardedLoadWatchdogRunnable = Runnable {
        if (isPreloadingRewarded) {
            R2Log.w("AdMob Rewarded preload watchdog triggered; resetting hung load state")
            isPreloadingRewarded = false
            val waiter = pendingRewardedWaiter
            if (waiter == null) {
                scheduleRewardedRetry()
            }
        }
    }

    init {
        AdConfigManager.ensureLoaded(appContext)
        (appContext as? Application)?.registerActivityLifecycleCallbacks(this)
        registerNetworkReconnectCallback()

        appScope.launch {
            rewardedAdFreeUntilMs.collectLatest { untilMs ->
                clockTickerMs.value = System.currentTimeMillis()
                while (untilMs > System.currentTimeMillis()) {
                    val remainingMs = (untilMs - System.currentTimeMillis()).coerceAtLeast(1_000L)
                    delay(min(15_000L, remainingMs))
                    clockTickerMs.value = System.currentTimeMillis()
                }
            }
        }
        appScope.launch {
            var previousShowAds = shouldShowAds.value
            shouldShowAds.collect { showAds ->
                if (!showAds) {
                    interstitialActionCounter.set(0)
                    _welcomeBackVisible.value = false
                    mainHandler.post {
                        mainHandler.removeCallbacks(appOpenRetryRunnable)
                        mainHandler.removeCallbacks(interstitialRetryRunnable)
                        mainHandler.removeCallbacks(interstitialLoadWatchdogRunnable)
                        mainHandler.removeCallbacks(pendingForegroundAppOpenRunnable)
                        appOpenAd = null
                        interstitialAdPool.clear()
                        clearAllCachedBanners()
                    }
                } else if (!previousShowAds && isSdkInitialized.get()) {
                    mainHandler.post {
                        _networkAvailableTick.value = SystemClock.elapsedRealtime()
                        preloadInterstitialAd()
                    }
                }
                previousShowAds = showAds
            }
        }
    }

    fun isNetworkConnected(): Boolean = runCatching {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return@runCatching true
        val activeNetwork = cm.activeNetwork ?: return@runCatching false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return@runCatching false
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }.getOrDefault(true)

    private fun registerNetworkReconnectCallback() {
        runCatching {
            val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return
            wasNetworkDisconnected = !isNetworkConnected()
            cm.registerDefaultNetworkCallback(
                object : ConnectivityManager.NetworkCallback() {
                    override fun onLost(network: Network) {
                        wasNetworkDisconnected = true
                    }

                    override fun onAvailable(network: Network) {
                        mainHandler.post {
                            val reconnected = wasNetworkDisconnected
                            wasNetworkDisconnected = false
                            if (!isSdkInitialized.get()) {
                                initialize()
                                return@post
                            }
                            if (!reconnected) return@post

                            _networkAvailableTick.value = SystemClock.elapsedRealtime()
                            appOpenRetryAttempt = 0
                            interstitialRetryAttempt = 0
                            rewardedRetryAttempt = 0
                            if (shouldShowAds.value) {
                                if (!coldStartAppOpenHandled.get() || hasGoneToBackground) {
                                    preloadAppOpenAd()
                                }
                                preloadInterstitialAd()
                            }
                            if (pendingRewardedWaiter != null || rewardedRequestedBeforeInit) {
                                preloadRewardedAd()
                            }
                        }
                    }
                },
            )
        }
    }

    private val umpConsentSynced = AtomicBoolean(false)

    private fun syncUmpConsentIfAvailable(activity: Activity) {
        if (activity is AdActivity || activity.isFinishing || activity.isDestroyed) return
        if (!umpConsentSynced.compareAndSet(false, true)) return

        mainHandler.post {
            runCatching {
                val consentInfo = UserMessagingPlatform.getConsentInformation(appContext)
                val paramsBuilder = ConsentRequestParameters.Builder()
                    .setTagForUnderAgeOfConsent(false)
                if (BuildConfig.DEBUG) {
                    val debugSettings = ConsentDebugSettings.Builder(appContext)
                        .addTestDeviceHashedId("E1C08C7B994E73EAB55EF8634D7120E1")
                        .build()
                    paramsBuilder.setConsentDebugSettings(debugSettings)
                }
                val params = paramsBuilder.build()
                consentInfo.requestConsentInfoUpdate(
                    activity,
                    params,
                    {
                        val host = currentHostActivity()
                            ?: activity.takeIf { !it.isFinishing && !it.isDestroyed }
                        if (host != null) {
                            runCatching {
                                UserMessagingPlatform.loadAndShowConsentFormIfRequired(host) {
                                    onPrivacyConsentUpdated()
                                }
                            }.onFailure {
                                onPrivacyConsentUpdated()
                            }
                        } else {
                            onPrivacyConsentUpdated()
                        }
                    },
                    {
                        onPrivacyConsentUpdated()
                    },
                )
            }.onFailure {
                // UMP not required or unavailable; proceed with standard AdMob serving
            }
        }
    }

    private fun onPrivacyConsentUpdated() {
        mainHandler.post {
            if (isSdkInitialized.get() && shouldShowAds.value) {
                retryAllFailedBannerChannels()
            }
        }
    }

    private fun markSdkReadyAndPreload() {
        if (!isSdkInitialized.compareAndSet(false, true)) {
            isSdkInitializing.set(false)
            return
        }
        isSdkInitializing.set(false)
        _sdkReady.value = true
        mainHandler.post {
            _networkAvailableTick.value = SystemClock.elapsedRealtime()
            // Stagger cold-start requests so `banner_splash` gets 0ms exclusive priority first,
            // followed by `app_open_splash` (+250ms) and `inter_splash` (+550ms) — eliminating
            // "W/Ads: Not retrying to fetch app settings" caused by simultaneous burst requests.
            if (shouldShowAds.value) {
                retryAllFailedBannerChannels()
                if (!coldStartAppOpenHandled.get()) {
                    mainHandler.postDelayed({ preloadAppOpenAd() }, 350L)
                }
                mainHandler.postDelayed({ preloadInterstitialAd() }, 650L)
            }
            if (rewardedRequestedBeforeInit || pendingRewardedWaiter != null) {
                rewardedRequestedBeforeInit = false
                mainHandler.postDelayed({ preloadRewardedAd() }, 950L)
            }
        }
    }

    /**
     * Initializes the Google Mobile Ads SDK (`OPTIMIZE_INITIALIZATION` + `OPTIMIZE_AD_LOADING` enabled
     * in `AndroidManifest.xml`) and waits for `OnInitializationCompleteListener` before firing ad requests
     * so `fetchAppSettings` completes cleanly without `W/Ads: Not retrying to fetch app settings`.
     */
    fun initialize() {
        if (isSdkInitialized.get() || !isSdkInitializing.compareAndSet(false, true)) return
        AdConfigManager.ensureLoaded(appContext)

        mainHandler.postDelayed(
            {
                if (isSdkInitializing.get() && !isSdkInitialized.get()) {
                    R2Log.i("AdMob SDK activated via 3.8s safety fallback")
                    markSdkReadyAndPreload()
                }
            },
            3_800L,
        )

        appScope.launch(Dispatchers.IO) {
            runCatching {
                val requestConfigBuilder = RequestConfiguration.Builder()
                if (BuildConfig.DEBUG) {
                    requestConfigBuilder.setTestDeviceIds(
                        listOf(
                            "E1C08C7B994E73EAB55EF8634D7120E1",
                            "A0D70182BE48AD26C48D3BF318EC725B",
                        ),
                    )
                }
                MobileAds.setRequestConfiguration(requestConfigBuilder.build())
                MobileAds.initialize(appContext) { initStatus ->
                    Log.d(
                        "AdMobDebug",
                        "AdMob SDK khởi tạo xong (Debug=${BuildConfig.DEBUG}, adapters=${initStatus.adapterStatusMap})",
                    )
                    R2Log.i("AdMob SDK initialized callback complete (Debug=${BuildConfig.DEBUG})")
                    markSdkReadyAndPreload()
                }
            }.onFailure { error ->
                isSdkInitializing.set(false)
                Log.e("AdMobDebug", "Lỗi khởi tạo AdMob SDK: ${error.message}", error)
                R2Log.w("AdMob initialize error: ${error.message}")
            }
        }
    }

    // -------------------------------------------------------------------------
    // App Open Ad + Dual Splash Fallback (`app_open_splash` + `inter_splash`)
    // -------------------------------------------------------------------------

    private fun isAppOpenAdValid(): Boolean {
        val ad = appOpenAd ?: return false
        val ageMs = SystemClock.elapsedRealtime() - appOpenLoadedElapsedMs
        return ageMs in 0 until APP_OPEN_EXPIRY_MS && ad.adUnitId.isNotBlank()
    }

    fun preloadAppOpenAd() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { preloadAppOpenAd() }
            return
        }
        if (!shouldShowAds.value) return
        if (!isSdkInitialized.get()) {
            initialize()
            return
        }
        val config = AdConfigManager.getConfig(appContext, AdConfigManager.PLACEMENT_APP_OPEN_SPLASH)
        if (!config.isEnable || config.id.isEmpty()) return

        val nowElapsed = SystemClock.elapsedRealtime()
        if (isPreloadingAppOpen) {
            if (appOpenLoadStartedElapsedMs > 0L &&
                (nowElapsed - appOpenLoadStartedElapsedMs) < APP_OPEN_LOAD_WATCHDOG_MS
            ) {
                return
            }
            isPreloadingAppOpen = false
        }
        if (isAppOpenAdValid()) return

        val appOpenCooldownMs = if (appOpenRetryAttempt > 0) {
            APP_OPEN_FAILED_RETRY_COOLDOWN_MS
        } else {
            MIN_AD_UNIT_RETRY_COOLDOWN_MS
        }
        if (lastAppOpenLoadAttemptElapsedMs > 0L &&
            (nowElapsed - lastAppOpenLoadAttemptElapsedMs) < appOpenCooldownMs
        ) {
            if (!coldStartAppOpenHandled.get()) {
                val remainingMs = (appOpenCooldownMs - (nowElapsed - lastAppOpenLoadAttemptElapsedMs))
                    .coerceAtLeast(1_500L)
                mainHandler.removeCallbacks(appOpenRetryRunnable)
                mainHandler.postDelayed(appOpenRetryRunnable, remainingMs)
            }
            return
        }

        mainHandler.removeCallbacks(appOpenRetryRunnable)
        isPreloadingAppOpen = true
        appOpenLoadStartedElapsedMs = nowElapsed
        lastAppOpenLoadAttemptElapsedMs = nowElapsed
        AppOpenAd.load(
            appContext,
            config.id,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    isPreloadingAppOpen = false
                    appOpenRetryAttempt = 0
                    lastAppOpenLoadAttemptElapsedMs = 0L
                    ad.onPaidEventListener = OnPaidEventListener { adValue: AdValue ->
                        R2Log.d("AdMob AppOpen paid event: ${adValue.valueMicros} ${adValue.currencyCode}")
                    }
                    val loadedAt = SystemClock.elapsedRealtime()
                    appOpenAd = ad
                    appOpenLoadedElapsedMs = loadedAt
                    Log.d(
                        "AdMobDebug",
                        "AppOpen đã sẵn sàng (unit=${config.id}, responseInfo=${ad.responseInfo})",
                    )
                    R2Log.d("AdMob AppOpen preloaded")

                    val timedOutAt = coldStartTimedOutAtElapsedMs
                    if (!coldStartFullScreenShown.get() &&
                        timedOutAt > 0L &&
                        (loadedAt - timedOutAt) <= 2_500L
                    ) {
                        coldStartTimedOutAtElapsedMs = 0L
                        val host = currentHostActivity()
                        if (host != null &&
                            !isShowingFullScreenAd &&
                            !audioPlayerManager.playbackState.value.isPlaying &&
                            coldStartFullScreenShown.compareAndSet(false, true)
                        ) {
                            showAppOpenInternal(
                                activity = host,
                                bypassCooldown = true,
                                onShowed = {},
                                onComplete = {},
                            )
                        }
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    isPreloadingAppOpen = false
                    lastAppOpenLoadAttemptElapsedMs = SystemClock.elapsedRealtime()
                    appOpenAd = null
                    coldStartAppOpenFailed.set(true)
                    appOpenRetryAttempt = (appOpenRetryAttempt + 1).coerceAtMost(3)
                    Log.e(
                        "AdMobDebug",
                        "AppOpen tải lỗi - Mã: ${adError.code} | Domain: ${adError.domain} | Unit: ${config.id} | Lý do: ${adError.message} | ResponseInfo: ${adError.responseInfo}",
                    )
                    Log.e("AdMobDebug", "Thông tin đầy đủ AppOpen: $adError")
                    // Only schedule automatic background retry if cold-start hasn't been handled yet
                    // OR if we don't already have a warm InterstitialAd ready as WelcomeBack fallback.
                    if (!coldStartAppOpenHandled.get() || (!isInterstitialAdValid() && appOpenRetryAttempt <= 1)) {
                        scheduleAppOpenRetry()
                    }
                    R2Log.w("AdMob AppOpen load failed: ${adError.code} ${adError.message}")
                }
            },
        )
    }

    private fun scheduleAppOpenRetry() {
        mainHandler.removeCallbacks(appOpenRetryRunnable)
        val delayMs = (APP_OPEN_FAILED_RETRY_COOLDOWN_MS * appOpenRetryAttempt.coerceAtLeast(1)).coerceAtMost(120_000L)
        mainHandler.postDelayed(appOpenRetryRunnable, delayMs)
    }

    /**
     * Dual Splash Full-Screen Ad Strategy modeled after the reference APK (`inter_splash` + `app_open_splash`):
     * - Waits up to [maxWaitMs] while `CustomSplashScreen` (with `banner_splash` at the bottom) is visible.
     * - Displays `AppOpenAd` as soon as loaded, OR falls back to `InterstitialAd` (`inter_splash`)
     *   if `AppOpenAd` fails or if `inter_splash` is already loaded after `600ms`!
     * - Strictly guarded by [coldStartFullScreenShown] so `AppOpenAd` and `inter_splash` can NEVER both show back-to-back.
     */
    suspend fun awaitAndShowColdStartAppOpen(
        maxWaitMs: Long = 6_000L,
        onAdShowed: () -> Unit = {},
    ): Boolean {
        if (!shouldShowAds.value) return false
        if (!coldStartAppOpenHandled.compareAndSet(false, true)) return false
        if (!isNetworkConnected()) return false

        coldStartAppOpenFailed.set(false)
        coldStartTimedOutAtElapsedMs = 0L
        initialize()
        if (isSdkInitialized.get()) {
            withContext(Dispatchers.Main.immediate) {
                if (!isPreloadingAppOpen && !isAppOpenAdValid()) {
                    preloadAppOpenAd()
                }
                if (!isPreloadingInterstitial && !isInterstitialAdValid()) {
                    preloadInterstitialAd()
                }
            }
        }
        val interSplashEnabled = AdConfigManager.getConfig(
            appContext,
            AdConfigManager.PLACEMENT_INTER_SPLASH,
        ).isEnable

        val startMs = SystemClock.elapsedRealtime()
        while (SystemClock.elapsedRealtime() - startMs < maxWaitMs) {
            if (!shouldShowAds.value || !isNetworkConnected() || coldStartFullScreenShown.get()) {
                return false
            }
            val elapsedMs = SystemClock.elapsedRealtime() - startMs
            val host = currentHostActivity()

            // Priority 1: Show AppOpenAd if ready
            if (isAppOpenAdValid() && host != null && !isShowingFullScreenAd) {
                coldStartFullScreenShown.set(true)
                coldStartTimedOutAtElapsedMs = 0L
                return withContext(Dispatchers.Main.immediate) {
                    suspendCancellableCoroutine { cont ->
                        showAppOpenInternal(
                            activity = host,
                            bypassCooldown = true,
                            onShowed = onAdShowed,
                        ) { shown ->
                            if (cont.isActive) cont.resume(shown)
                        }
                    }
                }
            }

            // Priority 2 (Reference APK `inter_splash` strategy):
            // If AppOpenAd failed OR after 600ms `inter_splash` is already loaded in RAM (after Splash banner hold), present `inter_splash` immediately!
            if (interSplashEnabled &&
                isInterstitialAdValid() &&
                host != null &&
                !isShowingFullScreenAd &&
                (coldStartAppOpenFailed.get() || elapsedMs >= 600L)
            ) {
                return withContext(Dispatchers.Main.immediate) {
                    val splashInterAd = pollNextValidInterstitialAd()
                    if (splashInterAd != null) {
                        coldStartFullScreenShown.set(true)
                        coldStartTimedOutAtElapsedMs = 0L
                        suspendCancellableCoroutine { cont ->
                            presentInterstitialInternal(
                                targetActivity = host,
                                ad = splashInterAd,
                                onShowed = onAdShowed,
                                onFinished = {
                                    if (cont.isActive) cont.resume(true)
                                },
                            )
                        }
                    } else {
                        false
                    }
                }
            }

            if (coldStartAppOpenFailed.get() && interstitialLastLoadFailed) {
                return false
            }

            delay(70L)
        }
        if (isPreloadingAppOpen && !coldStartFullScreenShown.get()) {
            coldStartTimedOutAtElapsedMs = SystemClock.elapsedRealtime()
        }
        return false
    }

    /**
     * Invoked when the user returns from background and `WelcomeBackOverlay` is shown
     * (or when the user taps `"Continue"` on `WelcomeBackOverlay`).
     * Presents `AppOpenAd` if ready, or falls back to `inter_welcome_back` (`InterstitialAd`).
     */
    fun showWelcomeBackAd(activity: Activity? = null, onComplete: () -> Unit = {}) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { showWelcomeBackAd(activity, onComplete) }
            return
        }
        val host = (activity?.findActivity() ?: currentHostActivity())
            ?.takeIf { it !is AdActivity && !it.isFinishing && !it.isDestroyed }
        if (host == null || !shouldShowAds.value || isShowingFullScreenAd || _isLoadingRewarded.value) {
            _welcomeBackVisible.value = false
            onComplete()
            return
        }

        val welcomeConfig = AdConfigManager.getConfig(appContext, AdConfigManager.PLACEMENT_INTER_WELCOME_BACK)

        if (isAppOpenAdValid()) {
            showAppOpenInternal(
                activity = host,
                bypassCooldown = false,
                onShowed = { _welcomeBackVisible.value = false },
            ) {
                _welcomeBackVisible.value = false
                onComplete()
            }
            return
        }

        if (welcomeConfig.isEnable && isInterstitialAdValid()) {
            val interAd = pollNextValidInterstitialAd()
            if (interAd != null) {
                presentInterstitialInternal(
                    targetActivity = host,
                    ad = interAd,
                    onShowed = { _welcomeBackVisible.value = false },
                    onFinished = {
                        _welcomeBackVisible.value = false
                        onComplete()
                    },
                )
                return
            }
        }

        showAppOpenIfAvailable(host) {
            _welcomeBackVisible.value = false
            onComplete()
        }
    }

    fun dismissWelcomeBackOverlay() {
        _welcomeBackVisible.value = false
    }

    fun showAppOpenIfAvailable(activity: Activity, onComplete: () -> Unit = {}) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { showAppOpenIfAvailable(activity, onComplete) }
            return
        }
        if (isAppOpenAdValid()) {
            showAppOpenInternal(
                activity = activity,
                bypassCooldown = false,
                onShowed = { _welcomeBackVisible.value = false },
            ) { onComplete() }
            return
        }
        if (!shouldShowAds.value || !isNetworkConnected() || isShowingFullScreenAd || _isLoadingRewarded.value) {
            onComplete()
            return
        }
        val nowElapsed = SystemClock.elapsedRealtime()
        if (lastAppOpenShownElapsedMs > 0L && nowElapsed - lastAppOpenShownElapsedMs < APP_OPEN_COOLDOWN_MS) {
            onComplete()
            return
        }
        if (lastFullScreenDismissedElapsedMs > 0L && nowElapsed - lastFullScreenDismissedElapsedMs < FULL_SCREEN_MIN_GAP_MS) {
            onComplete()
            return
        }

        if (!isPreloadingAppOpen) {
            preloadAppOpenAd()
        }
        if (!isPreloadingInterstitial && !isInterstitialAdValid()) {
            preloadInterstitialAd()
        }
        appScope.launch(Dispatchers.Main.immediate) {
            val startMs = SystemClock.elapsedRealtime()
            while (SystemClock.elapsedRealtime() - startMs < 2_200L) {
                if (!shouldShowAds.value || !isNetworkConnected() || isShowingFullScreenAd) {
                    break
                }
                val liveHost = currentHostActivity()
                if (isAppOpenAdValid() && liveHost != null) {
                    showAppOpenInternal(
                        activity = liveHost,
                        bypassCooldown = false,
                        onShowed = { _welcomeBackVisible.value = false },
                    ) { onComplete() }
                    return@launch
                }
                if (isInterstitialAdValid() && liveHost != null) {
                    val interAd = pollNextValidInterstitialAd()
                    if (interAd != null) {
                        presentInterstitialInternal(
                            targetActivity = liveHost,
                            ad = interAd,
                            onShowed = { _welcomeBackVisible.value = false },
                            onFinished = { onComplete() },
                        )
                        return@launch
                    }
                }
                if (!isPreloadingAppOpen && !isAppOpenAdValid() && !isPreloadingInterstitial && !isInterstitialAdValid()) {
                    break
                }
                delay(70L)
            }
            onComplete()
        }
    }

    private fun showAppOpenInternal(
        activity: Activity,
        bypassCooldown: Boolean,
        onShowed: () -> Unit,
        onComplete: (Boolean) -> Unit,
    ) {
        if (activity is AdActivity || activity.isFinishing || activity.isDestroyed) {
            onComplete(false)
            return
        }
        if (!shouldShowAds.value || isShowingFullScreenAd || _isLoadingRewarded.value) {
            onComplete(false)
            return
        }
        val nowElapsed = SystemClock.elapsedRealtime()
        if (!bypassCooldown) {
            if (lastAppOpenShownElapsedMs > 0L &&
                nowElapsed - lastAppOpenShownElapsedMs < APP_OPEN_COOLDOWN_MS
            ) {
                onComplete(false)
                return
            }
            if (lastFullScreenDismissedElapsedMs > 0L &&
                nowElapsed - lastFullScreenDismissedElapsedMs < FULL_SCREEN_MIN_GAP_MS
            ) {
                onComplete(false)
                return
            }
        }
        if (!isAppOpenAdValid()) {
            onComplete(false)
            return
        }

        val ad = appOpenAd ?: run {
            onComplete(false)
            return
        }
        appOpenAd = null
        coldStartTimedOutAtElapsedMs = 0L
        hasGoneToBackground = false
        setFullScreenAdShowing(true)
        audioPlayerManager.pauseForFullScreenAd()
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                hasGoneToBackground = false
                val shownAt = SystemClock.elapsedRealtime()
                lastAppOpenShownElapsedMs = shownAt
                lastFullScreenDismissedElapsedMs = shownAt
                // Immediately warm the next Interstitial in RAM while the user views AppOpenAd
                mainHandler.postDelayed({ preloadInterstitialAd() }, 250L)
                onShowed()
            }

            override fun onAdDismissedFullScreenContent() {
                setFullScreenAdShowing(false)
                hasGoneToBackground = false
                val dismissedAt = SystemClock.elapsedRealtime()
                lastAppOpenShownElapsedMs = dismissedAt
                lastFullScreenDismissedElapsedMs = dismissedAt
                audioPlayerManager.resumeAfterFullScreenAd()
                mainHandler.postDelayed({ retryAllFailedBannerChannels() }, 300L)
                mainHandler.postDelayed({ preloadInterstitialAd() }, 600L)
                mainHandler.post { onComplete(true) }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(
                    "AdMobDebug",
                    "Lỗi khi hiển thị màn hình AppOpen: ${adError.code} - ${adError.message} (domain=${adError.domain}, cause=${adError.cause})",
                )
                setFullScreenAdShowing(false)
                hasGoneToBackground = false
                audioPlayerManager.resumeAfterFullScreenAd()
                mainHandler.post { onComplete(false) }
            }
        }
        runCatching {
            ad.show(activity)
        }.onFailure {
            setFullScreenAdShowing(false)
            audioPlayerManager.resumeAfterFullScreenAd()
            mainHandler.post { onComplete(false) }
        }
    }

    // -------------------------------------------------------------------------
    // Interstitial Ad (`inter_splash`, `inter_onboarding`, `inter_home`, `inter_back`, `inter_welcome_back`)
    // -------------------------------------------------------------------------

    private fun pruneExpiredInterstitialAds() {
        val nowElapsed = SystemClock.elapsedRealtime()
        while (interstitialAdPool.isNotEmpty()) {
            val head = interstitialAdPool.first()
            val ageMs = nowElapsed - head.loadedElapsedMs
            if (ageMs !in 0 until INTERSTITIAL_EXPIRY_MS || head.ad.adUnitId.isBlank()) {
                interstitialAdPool.removeFirst()
            } else {
                break
            }
        }
    }

    private fun isInterstitialAdValid(): Boolean {
        pruneExpiredInterstitialAds()
        return interstitialAdPool.isNotEmpty()
    }

    private fun pollNextValidInterstitialAd(): InterstitialAd? {
        pruneExpiredInterstitialAds()
        return interstitialAdPool.removeFirstOrNull()?.ad
    }

    fun preloadInterstitialAd() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { preloadInterstitialAd() }
            return
        }
        if (!shouldShowAds.value) return
        if (!isSdkInitialized.get()) {
            interstitialRequestedBeforeInit = true
            initialize()
            return
        }
        pruneExpiredInterstitialAds()
        if (interstitialAdPool.size >= MAX_INTERSTITIAL_POOL_SIZE) return

        val nowElapsed = SystemClock.elapsedRealtime()
        if (isPreloadingInterstitial) {
            if (interstitialLoadStartedElapsedMs > 0L &&
                (nowElapsed - interstitialLoadStartedElapsedMs) < INTERSTITIAL_LOAD_WATCHDOG_MS
            ) {
                return
            }
            R2Log.w("AdMob Interstitial load exceeded watchdog window; restarting preload")
            isPreloadingInterstitial = false
        }
        val config = AdConfigManager.getConfig(appContext, AdConfigManager.PLACEMENT_INTER_HOME)
        val unitId = config.id.ifBlank { BuildConfig.INTERSTITIAL_ID.trim() }
        if (!config.isEnable || unitId.isEmpty()) return

        if (lastInterstitialLoadAttemptElapsedMs > 0L &&
            (nowElapsed - lastInterstitialLoadAttemptElapsedMs) < MIN_AD_UNIT_RETRY_COOLDOWN_MS
        ) {
            val remainingMs = (MIN_AD_UNIT_RETRY_COOLDOWN_MS - (nowElapsed - lastInterstitialLoadAttemptElapsedMs))
                .coerceAtLeast(1_500L)
            mainHandler.removeCallbacks(interstitialRetryRunnable)
            mainHandler.postDelayed(interstitialRetryRunnable, remainingMs)
            return
        }

        mainHandler.removeCallbacks(interstitialRetryRunnable)
        mainHandler.removeCallbacks(interstitialLoadWatchdogRunnable)
        isPreloadingInterstitial = true
        interstitialLoadStartedElapsedMs = nowElapsed
        lastInterstitialLoadAttemptElapsedMs = nowElapsed
        interstitialLastLoadFailed = false
        mainHandler.postDelayed(interstitialLoadWatchdogRunnable, INTERSTITIAL_LOAD_WATCHDOG_MS)

        InterstitialAd.load(
            appContext,
            unitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    mainHandler.removeCallbacks(interstitialLoadWatchdogRunnable)
                    isPreloadingInterstitial = false
                    interstitialLastLoadFailed = false
                    interstitialRetryAttempt = 0
                    lastInterstitialLoadAttemptElapsedMs = 0L
                    ad.onPaidEventListener = OnPaidEventListener { adValue: AdValue ->
                        R2Log.d("AdMob Interstitial paid event: ${adValue.valueMicros} ${adValue.currencyCode}")
                    }
                    pruneExpiredInterstitialAds()
                    if (interstitialAdPool.size < MAX_INTERSTITIAL_POOL_SIZE) {
                        interstitialAdPool.addLast(
                            LoadedInterstitialSlot(
                                ad = ad,
                                loadedElapsedMs = SystemClock.elapsedRealtime(),
                            ),
                        )
                    }
                    Log.d(
                        "AdMobDebug",
                        "Interstitial đã sẵn sàng (unit=$unitId, responseInfo=${ad.responseInfo})",
                    )
                    R2Log.d(
                        "AdMob Interstitial preloaded (pool=${interstitialAdPool.size}/$MAX_INTERSTITIAL_POOL_SIZE, counter=${interstitialActionCounter.get()}/$INTERSTITIAL_EVENT_THRESHOLD)",
                    )
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mainHandler.removeCallbacks(interstitialLoadWatchdogRunnable)
                    isPreloadingInterstitial = false
                    interstitialLastLoadFailed = true
                    lastInterstitialLoadAttemptElapsedMs = SystemClock.elapsedRealtime()
                    Log.e(
                        "AdMobDebug",
                        "Interstitial tải lỗi - Mã: ${adError.code} | Domain: ${adError.domain} | Unit: $unitId | Lý do: ${adError.message} | ResponseInfo: ${adError.responseInfo}",
                    )
                    Log.e("AdMobDebug", "Thông tin đầy đủ Interstitial: $adError")
                    pruneExpiredInterstitialAds()
                    scheduleInterstitialRetry()
                    R2Log.w("AdMob Interstitial load failed: ${adError.code} ${adError.message}")
                }
            },
        )
    }

    private fun scheduleInterstitialRetry() {
        mainHandler.removeCallbacks(interstitialRetryRunnable)
        interstitialRetryAttempt = (interstitialRetryAttempt + 1).coerceAtMost(3)
        val delayMs = (MIN_AD_UNIT_RETRY_COOLDOWN_MS * interstitialRetryAttempt).coerceAtMost(45_000L)
        mainHandler.postDelayed(interstitialRetryRunnable, delayMs)
    }

    private fun presentInterstitialInternal(
        targetActivity: Activity,
        ad: InterstitialAd,
        onShowed: () -> Unit = {},
        onFinished: () -> Unit,
    ) {
        hasGoneToBackground = false
        setFullScreenAdShowing(true)
        mainHandler.removeCallbacks(pendingForegroundAppOpenRunnable)
        audioPlayerManager.pauseForFullScreenAd()

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                hasGoneToBackground = false
                interstitialActionCounter.set(0)
                val nowElapsed = SystemClock.elapsedRealtime()
                lastFullScreenDismissedElapsedMs = nowElapsed
                lastAppOpenShownElapsedMs = nowElapsed
                // Immediately warm the next Interstitial in RAM while the user views the current Interstitial
                // (critical when `inter_splash` is shown so `inter_onboarding` is ready before dismissal!)
                mainHandler.postDelayed({ preloadInterstitialAd() }, 250L)
                onShowed()
            }

            override fun onAdDismissedFullScreenContent() {
                setFullScreenAdShowing(false)
                hasGoneToBackground = false
                val dismissedAt = SystemClock.elapsedRealtime()
                lastFullScreenDismissedElapsedMs = dismissedAt
                lastAppOpenShownElapsedMs = dismissedAt
                interstitialActionCounter.set(0)
                audioPlayerManager.resumeAfterFullScreenAd()
                mainHandler.postDelayed({ retryAllFailedBannerChannels() }, 300L)
                // Warm the next Interstitial slot in RAM so `inter_home` / `inter_back` is ready in 0ms
                mainHandler.postDelayed({ preloadInterstitialAd() }, 600L)
                mainHandler.post { onFinished() }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(
                    "AdMobDebug",
                    "Lỗi khi hiển thị màn hình Interstitial: ${adError.code} - ${adError.message} (domain=${adError.domain}, cause=${adError.cause})",
                )
                setFullScreenAdShowing(false)
                hasGoneToBackground = false
                audioPlayerManager.resumeAfterFullScreenAd()
                mainHandler.postDelayed({ preloadInterstitialAd() }, 1_000L)
                mainHandler.post { onFinished() }
            }
        }
        runCatching {
            ad.setImmersiveMode(true)
            ad.show(targetActivity)
        }.onFailure {
            setFullScreenAdShowing(false)
            hasGoneToBackground = false
            audioPlayerManager.resumeAfterFullScreenAd()
            mainHandler.post { onFinished() }
        }
    }

    /**
     * Shows a dedicated full-screen Interstitial Ad immediately (`inter_onboarding`),
     * used right after confirming Language Selection on First Open and at Onboarding completion.
     */
    fun showDedicatedInterstitial(
        activity: Activity? = null,
        maxWaitMs: Long = 3_000L,
        onFinished: () -> Unit = {},
    ) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { showDedicatedInterstitial(activity, maxWaitMs, onFinished) }
            return
        }

        val onboardingInterConfig = AdConfigManager.getConfig(
            appContext,
            AdConfigManager.PLACEMENT_INTER_ONBOARDING,
        )
        if (!shouldShowAds.value || !onboardingInterConfig.isEnable) {
            interstitialActionCounter.set(0)
            onFinished()
            return
        }

        val initialActivity = (activity?.findActivity() ?: currentHostActivity())
            ?.takeIf { it !is AdActivity && !it.isFinishing && !it.isDestroyed }
        val readyAd = if (initialActivity != null && !isShowingFullScreenAd) {
            pollNextValidInterstitialAd()
        } else {
            null
        }
        if (initialActivity != null && readyAd != null) {
            presentInterstitialInternal(
                targetActivity = initialActivity,
                ad = readyAd,
                onFinished = onFinished,
            )
            return
        }

        if (!isNetworkConnected() || maxWaitMs <= 0L) {
            onFinished()
            return
        }

        interstitialLastLoadFailed = false
        if (!isSdkInitialized.get()) {
            interstitialRequestedBeforeInit = true
            initialize()
        } else if (!isPreloadingInterstitial && !isInterstitialAdValid()) {
            preloadInterstitialAd()
        }

        appScope.launch(Dispatchers.Main.immediate) {
            val startMs = SystemClock.elapsedRealtime()
            while (SystemClock.elapsedRealtime() - startMs < maxWaitMs) {
                if (!shouldShowAds.value || interstitialLastLoadFailed || !isNetworkConnected()) {
                    break
                }
                val liveActivity = (activity?.findActivity() ?: currentHostActivity())
                    ?.takeIf { it !is AdActivity && !it.isFinishing && !it.isDestroyed }
                val loadedAd = if (liveActivity != null && !isShowingFullScreenAd) {
                    pollNextValidInterstitialAd()
                } else {
                    null
                }
                if (liveActivity != null && loadedAd != null) {
                    presentInterstitialInternal(
                        targetActivity = liveActivity,
                        ad = loadedAd,
                        onFinished = onFinished,
                    )
                    return@launch
                }
                delay(65L)
            }
            onFinished()
        }
    }

    /**
     * Modeled after `inter_back` in the reference APK:
     * Triggered when navigating back from child feature screens (`SceneDetail`, `PodcardDetail`,
     * `Mixer`, `DreamDictionary`, `Breathing`, `NowPlaying`) to the main tabs.
     * Respects a 35-second cooldown after any previous full-screen ad so it never spams the user.
     */
    fun onBackNavigation(
        activity: Activity? = null,
        onFinished: () -> Unit = {},
    ) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { onBackNavigation(activity, onFinished) }
            return
        }
        val backConfig = AdConfigManager.getConfig(appContext, AdConfigManager.PLACEMENT_INTER_BACK)
        if (!shouldShowAds.value || !backConfig.isEnable || isShowingFullScreenAd || _isLoadingRewarded.value) {
            onFinished()
            return
        }
        val nowElapsed = SystemClock.elapsedRealtime()
        if (lastFullScreenDismissedElapsedMs > 0L &&
            (nowElapsed - lastFullScreenDismissedElapsedMs) < INTER_BACK_COOLDOWN_MS
        ) {
            if (!isPreloadingInterstitial && !isInterstitialAdValid()) {
                preloadInterstitialAd()
            }
            onFinished()
            return
        }

        val targetActivity = (activity?.findActivity() ?: currentHostActivity())
            ?.takeIf { it !is AdActivity && !it.isFinishing && !it.isDestroyed }
        val readyAd = if (targetActivity != null) pollNextValidInterstitialAd() else null
        if (targetActivity != null && readyAd != null) {
            presentInterstitialInternal(
                targetActivity = targetActivity,
                ad = readyAd,
                onFinished = onFinished,
            )
            return
        }

        if (!isPreloadingInterstitial && !isInterstitialAdValid()) {
            preloadInterstitialAd()
        }
        onFinished()
    }

    /**
     * Resets the 3-click Interstitial counter back to `0` and warms the next Interstitial slot.
     */
    fun resetClickCounter() {
        interstitialActionCounter.set(0)
        preloadInterstitialAd()
    }

    /**
     * Primary feature navigation Interstitial (`inter_home`):
     * - Keeps the single-slot [InterstitialAd] warm in RAM.
     * - On Click #3 (`currentCount >= 3`), presents the loaded [InterstitialAd] and resets the counter to `0`.
     */
    fun onUserAction(
        activity: Activity? = null,
        onFinished: () -> Unit = {},
    ) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { onUserAction(activity, onFinished) }
            return
        }

        val homeInterConfig = AdConfigManager.getConfig(appContext, AdConfigManager.PLACEMENT_INTER_HOME)
        if (!shouldShowAds.value || !homeInterConfig.isEnable) {
            interstitialActionCounter.set(0)
            onFinished()
            return
        }

        val now = SystemClock.elapsedRealtime()
        if (now - lastActionRecordedElapsedMs < 140L) {
            onFinished()
            return
        }
        lastActionRecordedElapsedMs = now

        val currentCount = interstitialActionCounter.incrementAndGet()
        R2Log.d("AdMob user click count: $currentCount / $INTERSTITIAL_EVENT_THRESHOLD")

        if (interstitialAdPool.size < MAX_INTERSTITIAL_POOL_SIZE && !isPreloadingInterstitial) {
            preloadInterstitialAd()
        }

        if (currentCount < INTERSTITIAL_EVENT_THRESHOLD) {
            onFinished()
            return
        }

        if (isShowingFullScreenAd || _isLoadingRewarded.value) {
            interstitialActionCounter.set(INTERSTITIAL_EVENT_THRESHOLD)
            onFinished()
            return
        }

        val targetActivity = (activity?.findActivity() ?: currentHostActivity())
            ?.takeIf { it !is AdActivity && !it.isFinishing && !it.isDestroyed }
        val readyAd = if (targetActivity != null) {
            pollNextValidInterstitialAd()
        } else {
            null
        }

        if (targetActivity != null && readyAd != null) {
            presentInterstitialInternal(
                targetActivity = targetActivity,
                ad = readyAd,
                onFinished = onFinished,
            )
            return
        }

        if (!isNetworkConnected()) {
            interstitialActionCounter.set(INTERSTITIAL_EVENT_THRESHOLD)
            onFinished()
            return
        }

        interstitialLastLoadFailed = false
        if (!isSdkInitialized.get()) {
            interstitialRequestedBeforeInit = true
            initialize()
        } else if (!isPreloadingInterstitial) {
            preloadInterstitialAd()
        }

        appScope.launch(Dispatchers.Main.immediate) {
            val startMs = SystemClock.elapsedRealtime()
            while (SystemClock.elapsedRealtime() - startMs < 2_500L) {
                if (!shouldShowAds.value || interstitialLastLoadFailed || !isNetworkConnected()) {
                    break
                }
                val liveActivity = (activity?.findActivity() ?: currentHostActivity())
                    ?.takeIf { it !is AdActivity && !it.isFinishing && !it.isDestroyed }
                val loadedAd = if (liveActivity != null && !isShowingFullScreenAd) {
                    pollNextValidInterstitialAd()
                } else {
                    null
                }
                if (liveActivity != null && loadedAd != null) {
                    presentInterstitialInternal(
                        targetActivity = liveActivity,
                        ad = loadedAd,
                        onFinished = onFinished,
                    )
                    return@launch
                }
                delay(60L)
            }
            interstitialActionCounter.set(INTERSTITIAL_EVENT_THRESHOLD)
            onFinished()
        }
    }

    fun maybeShowInterstitial(
        activity: Activity?,
        onFinished: () -> Unit = {},
    ) = onUserAction(activity = activity, onFinished = onFinished)

    // -------------------------------------------------------------------------
    // Rewarded Ad (Single-slot contextual pool, 55m expiry, unlimited frequency)
    // -------------------------------------------------------------------------

    private fun pruneExpiredRewardedAds() {
        val nowElapsed = SystemClock.elapsedRealtime()
        while (rewardedAdPool.isNotEmpty()) {
            val head = rewardedAdPool.first()
            val ageMs = nowElapsed - head.loadedElapsedMs
            if (ageMs !in 0 until REWARDED_EXPIRY_MS || head.ad.adUnitId.isBlank()) {
                rewardedAdPool.removeFirst()
            } else {
                break
            }
        }
        _isRewardedAdReady.value = rewardedAdPool.isNotEmpty()
    }

    private fun pollNextValidRewardedAd(): RewardedAd? {
        pruneExpiredRewardedAds()
        val slot = rewardedAdPool.removeFirstOrNull()
        _isRewardedAdReady.value = rewardedAdPool.isNotEmpty()
        return slot?.ad
    }

    fun ensureRewardedAdLoaded() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { ensureRewardedAdLoaded() }
            return
        }
        if (isPaidVip.value) return
        pruneExpiredRewardedAds()
        if (rewardedAdPool.size >= MAX_REWARDED_POOL_SIZE) {
            return
        }
        val nowElapsed = SystemClock.elapsedRealtime()
        if (isPreloadingRewarded &&
            rewardedLoadStartedElapsedMs > 0L &&
            (nowElapsed - rewardedLoadStartedElapsedMs) >= REWARDED_LOAD_WATCHDOG_MS
        ) {
            isPreloadingRewarded = false
        }
        if (!isPreloadingRewarded) {
            rewardedRetryAttempt = 0
            preloadRewardedAd()
        }
    }

    fun preloadRewardedAd() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { preloadRewardedAd() }
            return
        }
        if (isPaidVip.value) return
        if (!isSdkInitialized.get()) {
            rewardedRequestedBeforeInit = true
            initialize()
            return
        }
        pruneExpiredRewardedAds()
        if (rewardedAdPool.size >= MAX_REWARDED_POOL_SIZE) {
            return
        }
        val nowElapsed = SystemClock.elapsedRealtime()
        if (isPreloadingRewarded) {
            if (rewardedLoadStartedElapsedMs > 0L &&
                (nowElapsed - rewardedLoadStartedElapsedMs) < REWARDED_LOAD_WATCHDOG_MS
            ) {
                return
            }
            R2Log.w("AdMob Rewarded load exceeded watchdog window; restarting preload")
            isPreloadingRewarded = false
        }
        val config = AdConfigManager.getConfig(appContext, AdConfigManager.PLACEMENT_REWARDED_MAIN)
        val unitId = config.id.ifBlank { BuildConfig.REWARDED_ID.trim() }
        if (!config.isEnable || unitId.isEmpty()) return

        if (lastRewardedLoadAttemptElapsedMs > 0L &&
            (nowElapsed - lastRewardedLoadAttemptElapsedMs) < MIN_AD_UNIT_RETRY_COOLDOWN_MS
        ) {
            val remainingMs = (MIN_AD_UNIT_RETRY_COOLDOWN_MS - (nowElapsed - lastRewardedLoadAttemptElapsedMs))
                .coerceAtLeast(1_500L)
            mainHandler.removeCallbacks(rewardedRetryRunnable)
            mainHandler.postDelayed(rewardedRetryRunnable, remainingMs)
            return
        }

        mainHandler.removeCallbacks(rewardedRetryRunnable)
        mainHandler.removeCallbacks(rewardedLoadWatchdogRunnable)
        isPreloadingRewarded = true
        rewardedLastLoadFailed = false
        rewardedLoadStartedElapsedMs = nowElapsed
        lastRewardedLoadAttemptElapsedMs = nowElapsed
        val requestGen = ++rewardedLoadGeneration
        mainHandler.postDelayed(rewardedLoadWatchdogRunnable, REWARDED_LOAD_WATCHDOG_MS)

        RewardedAd.load(
            appContext,
            unitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    mainHandler.removeCallbacks(rewardedLoadWatchdogRunnable)
                    isPreloadingRewarded = false
                    rewardedLastLoadFailed = false
                    rewardedRetryAttempt = 0
                    lastRewardedLoadAttemptElapsedMs = 0L
                    ad.onPaidEventListener = OnPaidEventListener { adValue: AdValue ->
                        R2Log.d("AdMob Rewarded paid event: ${adValue.valueMicros} ${adValue.currencyCode}")
                    }

                    val waiter = pendingRewardedWaiter
                    if (waiter != null) {
                        pendingRewardedWaiter = null
                        R2Log.d("AdMob Rewarded delivered directly to active waiter (gen=$requestGen)")
                        waiter(ad)
                        return
                    }

                    pruneExpiredRewardedAds()
                    if (rewardedAdPool.size < MAX_REWARDED_POOL_SIZE) {
                        rewardedAdPool.addLast(
                            LoadedRewardedSlot(
                                ad = ad,
                                loadedElapsedMs = SystemClock.elapsedRealtime(),
                            ),
                        )
                    }
                    _isRewardedAdReady.value = rewardedAdPool.isNotEmpty()
                    Log.d(
                        "AdMobDebug",
                        "Rewarded đã sẵn sàng (unit=$unitId, responseInfo=${ad.responseInfo})",
                    )
                    R2Log.d("AdMob Rewarded preloaded (pool=${rewardedAdPool.size}/$MAX_REWARDED_POOL_SIZE, gen=$requestGen)")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    if (requestGen != rewardedLoadGeneration) return
                    mainHandler.removeCallbacks(rewardedLoadWatchdogRunnable)
                    isPreloadingRewarded = false
                    rewardedLastLoadFailed = true
                    lastRewardedLoadAttemptElapsedMs = SystemClock.elapsedRealtime()
                    Log.e(
                        "AdMobDebug",
                        "Rewarded tải lỗi - Mã: ${adError.code} | Domain: ${adError.domain} | Unit: $unitId | Lý do: ${adError.message} | ResponseInfo: ${adError.responseInfo}",
                    )
                    Log.e("AdMobDebug", "Thông tin đầy đủ Rewarded: $adError")
                    pruneExpiredRewardedAds()
                    val waiter = pendingRewardedWaiter
                    if (waiter != null) {
                        pendingRewardedWaiter = null
                        waiter(null)
                    } else {
                        scheduleRewardedRetry()
                    }
                    R2Log.w("AdMob Rewarded load failed: ${adError.code} ${adError.message}")
                }
            },
        )
    }

    private fun scheduleRewardedRetry() {
        mainHandler.removeCallbacks(rewardedRetryRunnable)
        rewardedRetryAttempt = (rewardedRetryAttempt + 1).coerceAtMost(3)
        val delayMs = (MIN_AD_UNIT_RETRY_COOLDOWN_MS * rewardedRetryAttempt).coerceAtMost(45_000L)
        mainHandler.postDelayed(rewardedRetryRunnable, delayMs)
    }

    fun showOnboardingRewardedAd(
        activity: Activity? = null,
        maxWaitMs: Long = 3_500L,
        onFinished: (rewardEarned: Boolean, newCount: Int) -> Unit = { _, _ -> },
    ) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { showOnboardingRewardedAd(activity, maxWaitMs, onFinished) }
            return
        }

        if (!shouldShowAds.value) {
            onFinished(false, rewardedAdWatchCount.value)
            return
        }

        val finishedOnce = AtomicBoolean(false)
        fun completeOnce(earned: Boolean, count: Int = rewardedAdWatchCount.value) {
            if (finishedOnce.compareAndSet(false, true)) {
                onFinished(earned, count)
            }
        }

        val initialActivity = (activity?.findActivity() ?: currentHostActivity())
            ?.takeIf { it !is AdActivity && !it.isFinishing && !it.isDestroyed }
        val readyAd = if (initialActivity != null && !isShowingFullScreenAd) {
            pollNextValidRewardedAd()
        } else {
            null
        }

        if (initialActivity != null && readyAd != null) {
            presentRewardedAd(
                activity = initialActivity,
                ad = readyAd,
                unlockItemId = null,
                onRewardEarned = { _, newCount -> completeOnce(true, newCount) },
                onAdUnavailable = { completeOnce(false) },
                onAdDismissedWithoutReward = { completeOnce(false) },
            )
            return
        }

        if (!isNetworkConnected() || maxWaitMs <= 0L) {
            completeOnce(false)
            return
        }

        ensureRewardedAdLoaded()

        appScope.launch(Dispatchers.Main.immediate) {
            val startMs = SystemClock.elapsedRealtime()
            while (SystemClock.elapsedRealtime() - startMs < maxWaitMs) {
                if (!shouldShowAds.value || rewardedLastLoadFailed || !isNetworkConnected()) {
                    break
                }
                val liveActivity = (activity?.findActivity() ?: currentHostActivity())
                    ?.takeIf { it !is AdActivity && !it.isFinishing && !it.isDestroyed }
                val loadedAd = if (liveActivity != null && !isShowingFullScreenAd) {
                    pollNextValidRewardedAd()
                } else {
                    null
                }
                if (liveActivity != null && loadedAd != null) {
                    presentRewardedAd(
                        activity = liveActivity,
                        ad = loadedAd,
                        unlockItemId = null,
                        onRewardEarned = { _, newCount -> completeOnce(true, newCount) },
                        onAdUnavailable = { completeOnce(false) },
                        onAdDismissedWithoutReward = { completeOnce(false) },
                    )
                    return@launch
                }
                delay(75L)
            }
            completeOnce(false)
        }
    }

    fun showRewardedAd(
        activity: Activity?,
        unlockItemId: String? = null,
        onRewardEarned: (activatedThreeHourPass: Boolean, newCount: Int) -> Unit,
        onAdUnavailable: () -> Unit = {},
        onAdDismissedWithoutReward: () -> Unit = {},
    ) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post {
                showRewardedAd(
                    activity = activity,
                    unlockItemId = unlockItemId,
                    onRewardEarned = onRewardEarned,
                    onAdUnavailable = onAdUnavailable,
                    onAdDismissedWithoutReward = onAdDismissedWithoutReward,
                )
            }
            return
        }

        val resolvedActivity = (activity?.findActivity() ?: currentHostActivity())
            ?.takeIf { it !is AdActivity && !it.isFinishing && !it.isDestroyed }
        if (resolvedActivity == null || isShowingFullScreenAd) {
            onAdUnavailable()
            return
        }

        val readyAd = pollNextValidRewardedAd()
        if (readyAd != null) {
            presentRewardedAd(
                activity = resolvedActivity,
                ad = readyAd,
                unlockItemId = unlockItemId,
                onRewardEarned = onRewardEarned,
                onAdUnavailable = onAdUnavailable,
                onAdDismissedWithoutReward = onAdDismissedWithoutReward,
            )
            return
        }

        if (!isNetworkConnected()) {
            onAdUnavailable()
            return
        }

        activeRewardedTimeoutRunnable?.let(mainHandler::removeCallbacks)
        _isLoadingRewarded.value = true
        val completed = AtomicBoolean(false)
        val timeoutRunnable = Runnable {
            if (completed.compareAndSet(false, true)) {
                pendingRewardedWaiter = null
                activeRewardedTimeoutRunnable = null
                _isLoadingRewarded.value = false
                isPreloadingRewarded = false
                R2Log.w("AdMob Rewarded on-demand load timed out")
                onAdUnavailable()
            }
        }
        activeRewardedTimeoutRunnable = timeoutRunnable
        mainHandler.postDelayed(timeoutRunnable, REWARDED_ON_DEMAND_TIMEOUT_MS)

        pendingRewardedWaiter = { loadedAd ->
            mainHandler.removeCallbacks(timeoutRunnable)
            activeRewardedTimeoutRunnable = null
            _isLoadingRewarded.value = false
            if (completed.compareAndSet(false, true)) {
                val liveActivity = (activity?.findActivity() ?: currentHostActivity())
                    ?.takeIf { it !is AdActivity && !it.isFinishing && !it.isDestroyed }
                    ?: resolvedActivity.takeIf { !it.isFinishing && !it.isDestroyed }
                if (loadedAd != null && liveActivity != null && !isShowingFullScreenAd) {
                    presentRewardedAd(
                        activity = liveActivity,
                        ad = loadedAd,
                        unlockItemId = unlockItemId,
                        onRewardEarned = onRewardEarned,
                        onAdUnavailable = onAdUnavailable,
                        onAdDismissedWithoutReward = onAdDismissedWithoutReward,
                    )
                } else {
                    onAdUnavailable()
                }
            }
        }

        val nowElapsed = SystemClock.elapsedRealtime()
        val preloadStale = isPreloadingRewarded &&
            rewardedLoadStartedElapsedMs > 0L &&
            (nowElapsed - rewardedLoadStartedElapsedMs) >= 6_000L
        if (!isPreloadingRewarded || preloadStale) {
            isPreloadingRewarded = false
            rewardedRetryAttempt = 0
            if (!isSdkInitialized.get()) {
                rewardedRequestedBeforeInit = true
                initialize()
            } else {
                preloadRewardedAd()
            }
        }
    }

    private fun presentRewardedAd(
        activity: Activity,
        ad: RewardedAd,
        unlockItemId: String?,
        onRewardEarned: (activatedThreeHourPass: Boolean, newCount: Int) -> Unit,
        onAdUnavailable: () -> Unit,
        onAdDismissedWithoutReward: () -> Unit = {},
    ) {
        pruneExpiredRewardedAds()
        setFullScreenAdShowing(true)
        hasGoneToBackground = false
        mainHandler.removeCallbacks(pendingForegroundAppOpenRunnable)
        audioPlayerManager.pauseForFullScreenAd()

        val userEarnedReward = AtomicBoolean(false)
        val adDismissed = AtomicBoolean(false)
        val rewardDispatched = AtomicBoolean(false)
        var rewardWriteDeferred: CompletableDeferred<Pair<Boolean, Int>>? = null

        fun dispatchRewardWhenReady() {
            if (!userEarnedReward.get() || !adDismissed.get()) return
            if (!rewardDispatched.compareAndSet(false, true)) return
            appScope.launch(Dispatchers.Main) {
                val (activatedPass, updatedCount) = rewardWriteDeferred?.await()
                    ?: (false to rewardedAdWatchCount.value)
                mainHandler.post {
                    onRewardEarned(activatedPass, updatedCount)
                }
            }
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                hasGoneToBackground = false
                val nowElapsed = SystemClock.elapsedRealtime()
                lastFullScreenDismissedElapsedMs = nowElapsed
                lastAppOpenShownElapsedMs = nowElapsed
            }

            override fun onAdDismissedFullScreenContent() {
                setFullScreenAdShowing(false)
                hasGoneToBackground = false
                val dismissedAt = SystemClock.elapsedRealtime()
                lastFullScreenDismissedElapsedMs = dismissedAt
                lastAppOpenShownElapsedMs = dismissedAt
                interstitialActionCounter.set(0)
                audioPlayerManager.resumeAfterFullScreenAd()
                adDismissed.set(true)
                if (userEarnedReward.get()) {
                    dispatchRewardWhenReady()
                } else {
                    mainHandler.post { onAdDismissedWithoutReward() }
                }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(
                    "AdMobDebug",
                    "Lỗi khi hiển thị màn hình Rewarded: ${adError.code} - ${adError.message} (domain=${adError.domain}, cause=${adError.cause})",
                )
                setFullScreenAdShowing(false)
                hasGoneToBackground = false
                audioPlayerManager.resumeAfterFullScreenAd()
                if (!userEarnedReward.get()) {
                    mainHandler.post { onAdUnavailable() }
                } else {
                    adDismissed.set(true)
                    dispatchRewardWhenReady()
                }
            }
        }

        runCatching {
            ad.setImmersiveMode(true)
            ad.show(activity) {
                if (userEarnedReward.compareAndSet(false, true)) {
                    val deferred = CompletableDeferred<Pair<Boolean, Int>>()
                    rewardWriteDeferred = deferred
                    appScope.launch {
                        val activatedPass = userPreferencesRepository.recordRewardedAdEarned(
                            unlockItemId = unlockItemId,
                        )
                        clockTickerMs.value = System.currentTimeMillis()
                        val updatedCount = userPreferencesRepository.rewardedAdWatchCount.first()
                        deferred.complete(activatedPass to updatedCount)
                        dispatchRewardWhenReady()
                    }
                }
            }
        }.onFailure { error ->
            R2Log.w("AdMob Rewarded show exception: ${error.message}")
            setFullScreenAdShowing(false)
            audioPlayerManager.resumeAfterFullScreenAd()
            mainHandler.post { onAdUnavailable() }
        }
    }

    // -------------------------------------------------------------------------
    // Activity Lifecycle Tracking (excludes AdActivity and full-screen ad transitions)
    // -------------------------------------------------------------------------

    private fun isHostAppActivity(activity: Activity): Boolean {
        if (activity is AdActivity) return false
        val className = activity.javaClass.name
        return !className.startsWith("com.google.android.gms.") &&
            !className.startsWith("com.android.billingclient.") &&
            !className.startsWith("com.google.android.play.")
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (isHostAppActivity(activity)) {
            currentActivityRef = WeakReference(activity)
            syncUmpConsentIfAvailable(activity)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        if (!isHostAppActivity(activity)) return
        currentActivityRef = WeakReference(activity)
        val nowElapsed = SystemClock.elapsedRealtime()
        val wasInBackground = startedNonAdActivities == 0 &&
            hasGoneToBackground &&
            !isShowingFullScreenAd &&
            backgroundedAtElapsedMs > 0L &&
            (nowElapsed - backgroundedAtElapsedMs) >= MIN_BACKGROUND_DURATION_FOR_APP_OPEN_MS
        startedNonAdActivities++
        hasGoneToBackground = false
        if (wasInBackground && shouldShowAds.value && !audioPlayerManager.playbackState.value.isPlaying) {
            mainHandler.removeCallbacks(pendingForegroundAppOpenRunnable)
            mainHandler.postDelayed(pendingForegroundAppOpenRunnable, 180L)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        if (isHostAppActivity(activity)) {
            currentActivityRef = WeakReference(activity)
            syncUmpConsentIfAvailable(activity)
            if (isSdkInitialized.get() && shouldShowAds.value) {
                preloadInterstitialAd()
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        if (!isHostAppActivity(activity)) return
        startedNonAdActivities = (startedNonAdActivities - 1).coerceAtLeast(0)
        if (startedNonAdActivities == 0 && !isShowingFullScreenAd && !activity.isChangingConfigurations) {
            hasGoneToBackground = true
            backgroundedAtElapsedMs = SystemClock.elapsedRealtime()
            if (isSdkInitialized.get() && shouldShowAds.value) {
                preloadAppOpenAd()
                preloadInterstitialAd()
            }
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivityRef?.get() === activity) {
            currentActivityRef = null
        }
    }

    /**
     * Mở trình kiểm tra giao diện AdMob Ad Inspector trực tiếp trên thiết bị
     * để chẩn đoán lỗi No Fill (Mã 3), trạng thái Ad Unit và Mediation.
     */
    @Suppress("unused")
    fun openAdInspector(context: Context = currentHostActivity() ?: appContext) {
        MobileAds.openAdInspector(context) { error ->
            if (error != null) {
                Log.e(
                    "AdMobDebug",
                    "Không thể mở Ad Inspector (code=${error.code}, domain=${error.domain}): ${error.message}",
                )
            } else {
                Log.d("AdMobDebug", "Đã đóng Ad Inspector")
            }
        }
    }

    companion object {
        const val INTERSTITIAL_EVENT_THRESHOLD = 3
        private const val MAX_INTERSTITIAL_POOL_SIZE = 1
        private const val MAX_REWARDED_POOL_SIZE = 1

        private const val MIN_AD_UNIT_RETRY_COOLDOWN_MS = 15_000L
        private const val APP_OPEN_FAILED_RETRY_COOLDOWN_MS = 60_000L
        private const val MIN_BACKGROUND_DURATION_FOR_APP_OPEN_MS = 4_000L
        private const val FULL_SCREEN_MIN_GAP_MS = 4_000L
        private const val INTER_BACK_COOLDOWN_MS = 35_000L
        private const val APP_OPEN_EXPIRY_MS = 4L * 60L * 60L * 1000L
        private const val APP_OPEN_LOAD_WATCHDOG_MS = 14_000L
        private const val APP_OPEN_COOLDOWN_MS = 20_000L
        private const val INTERSTITIAL_EXPIRY_MS = 55L * 60L * 1000L
        private const val INTERSTITIAL_LOAD_WATCHDOG_MS = 14_000L
        private const val REWARDED_EXPIRY_MS = 55L * 60L * 1000L
        private const val REWARDED_LOAD_WATCHDOG_MS = 14_000L
        private const val REWARDED_ON_DEMAND_TIMEOUT_MS = 12_000L
    }
}

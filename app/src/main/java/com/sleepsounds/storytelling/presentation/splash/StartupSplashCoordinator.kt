package com.sleepsounds.storytelling.presentation.splash

import android.os.SystemClock
import com.sleepsounds.storytelling.ads.AdConfigManager
import com.sleepsounds.storytelling.ads.AdMobManager
import com.sleepsounds.storytelling.ads.isBannerChannelLoaded
import com.sleepsounds.storytelling.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinates the initial Loading/Splash screen (`CustomSplashScreen`) using the Reference APK's
 * Dual Splash Ad Strategy (`banner_splash` at the bottom + `app_open_splash` / `inter_splash` full-screen)
 * and Look-Ahead Banner Preloading for the next screen (`Language` / `Onboarding` / `Home`).
 *
 * Guarantees that `banner_splash` has sufficient time to load AND remain visible on screen for at least
 * [POST_BANNER_LOAD_DISPLAY_HOLD_MS] (`2,800ms`) while the progress bar completes to 100%, ensuring
 * a 100% valid Google AdMob Active View impression before the full-screen Splash Ad is presented.
 */
@Singleton
class StartupSplashCoordinator @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val adMobManager: AdMobManager,
) {
    private val _splashVisible = MutableStateFlow(true)
    val splashVisible: StateFlow<Boolean> = _splashVisible.asStateFlow()

    /**
     * Real-time progress (`0.06f .. 1.0f`) synchronized with `banner_splash` loading,
     * post-load Viewable Impression hold, and full-screen Splash Ad readiness.
     */
    private val _splashProgress = MutableStateFlow(0.06f)
    val splashProgress: StateFlow<Float> = _splashProgress.asStateFlow()

    /**
     * Becomes `true` once `banner_splash` has loaded (or after `2,000ms`), unlocking Look-Ahead
     * banner loading for the underlying destination (`Language` or `Home`) so `banner_splash`
     * gets 100% exclusive priority during the initial cold-start window.
     */
    private val _nextScreenBannerPreloadUnlocked = MutableStateFlow(false)
    val nextScreenBannerPreloadUnlocked: StateFlow<Boolean> = _nextScreenBannerPreloadUnlocked.asStateFlow()

    /**
     * Becomes `true` only AFTER the cold-start splash AND any cold-start App Open / Interstitial Ad have
     * completely finished/dismissed, preventing system permission dialogs from overlapping the Splash Ad.
     */
    private val _coldStartSequenceCompleted = MutableStateFlow(false)
    val coldStartSequenceCompleted: StateFlow<Boolean> = _coldStartSequenceCompleted.asStateFlow()

    private val _launchSessionId = MutableStateFlow(1)
    val launchSessionId: StateFlow<Int> = _launchSessionId.asStateFlow()

    private val firstContentFrameReady = MutableStateFlow(false)
    private val sequenceRunning = AtomicBoolean(false)
    private val launchedViaNotification = AtomicBoolean(false)
    private var activeSplashJob: Job? = null

    fun markOpenedFromNotification() {
        launchedViaNotification.set(true)
    }

    fun resetForFreshLaunch() {
        activeSplashJob?.cancel()
        activeSplashJob = null
        _splashVisible.value = true
        _splashProgress.value = 0.06f
        _nextScreenBannerPreloadUnlocked.value = false
        _coldStartSequenceCompleted.value = false
        firstContentFrameReady.value = false
        sequenceRunning.set(false)
        _launchSessionId.value += 1
    }

    fun onContentReady() {
        firstContentFrameReady.value = true
    }

    fun begin() {
        if (!sequenceRunning.compareAndSet(false, true)) return

        activeSplashJob?.cancel()
        activeSplashJob = appScope.launch(Dispatchers.Main.immediate) {
            try {
                val isNotificationOpen = launchedViaNotification.getAndSet(false)
                if (isNotificationOpen) {
                    withTimeoutOrNull(NOTIFICATION_REVEAL_TIMEOUT_MS) {
                        firstContentFrameReady.first { it }
                    }
                    _nextScreenBannerPreloadUnlocked.value = true
                    _splashProgress.value = 1f
                    _splashVisible.value = false
                    return@launch
                }

                _splashProgress.value = 0.08f

                // 1. Initialize AdMob SDK; once initialized, `banner_splash` + `AppOpen` + `inter_splash` load first.
                adMobManager.initialize()

                // 2. Phase 1 (Progress 8% -> 68%): Give `banner_splash` exclusive priority first,
                //    then unlock Look-Ahead preloading for the next screen once `banner_splash` loads (or after 2,000ms).
                val phase1StartMs = SystemClock.elapsedRealtime()
                var splashBannerLoadedAtMs = 0L
                while (true) {
                    val elapsedMs = SystemClock.elapsedRealtime() - phase1StartMs
                    val ratio = (elapsedMs.toFloat() / MAX_BANNER_SPLASH_WAIT_MS.toFloat()).coerceIn(0f, 1f)
                    _splashProgress.value = (0.08f + 0.60f * ratio).coerceAtMost(0.68f)

                    val bannerLoadedNow = isBannerChannelLoaded(AdConfigManager.PLACEMENT_BANNER_SPLASH)
                    if (bannerLoadedNow && splashBannerLoadedAtMs == 0L) {
                        splashBannerLoadedAtMs = SystemClock.elapsedRealtime()
                        _nextScreenBannerPreloadUnlocked.value = true
                    } else if (elapsedMs >= 2_000L && !_nextScreenBannerPreloadUnlocked.value) {
                        _nextScreenBannerPreloadUnlocked.value = true
                    }

                    if (!adMobManager.shouldShowAds.value && elapsedMs >= 1_800L) {
                        break
                    }
                    if (bannerLoadedNow && elapsedMs >= MIN_SPLASH_PRELOAD_WINDOW_MS) {
                        break
                    }
                    if (elapsedMs >= MAX_BANNER_SPLASH_WAIT_MS) {
                        break
                    }
                    delay(60L)
                }

                _nextScreenBannerPreloadUnlocked.value = true

                // 3. Phase 2 (Progress 68% -> 96%): Dedicated Viewable Impression Hold (`2,800ms`)
                //    Keeps `banner_splash` clearly visible on the Splash screen after it loads so the user
                //    sees the banner and Google AdMob Active View records a 100% valid Viewable Impression.
                if (adMobManager.shouldShowAds.value) {
                    val holdStartMs = SystemClock.elapsedRealtime()
                    val alreadyVisibleMs = if (splashBannerLoadedAtMs > 0L) {
                        (holdStartMs - splashBannerLoadedAtMs).coerceAtLeast(0L)
                    } else {
                        0L
                    }
                    val remainingHoldMs = (POST_BANNER_LOAD_DISPLAY_HOLD_MS - alreadyVisibleMs)
                        .coerceIn(1_800L, POST_BANNER_LOAD_DISPLAY_HOLD_MS)

                    while (true) {
                        val holdElapsed = SystemClock.elapsedRealtime() - holdStartMs
                        val holdRatio = (holdElapsed.toFloat() / remainingHoldMs.toFloat()).coerceIn(0f, 1f)
                        _splashProgress.value = (0.68f + 0.28f * holdRatio).coerceAtMost(0.96f)
                        if (holdElapsed >= remainingHoldMs) break
                        delay(60L)
                    }
                }

                if (!firstContentFrameReady.value) {
                    withTimeoutOrNull(800L) {
                        firstContentFrameReady.first { it }
                    }
                }

                // 4. Phase 3 (Progress 96% -> 100%): Complete the progress bar and present `AppOpenAd` or `inter_splash`
                _splashProgress.value = 1.0f
                delay(180L)

                adMobManager.awaitAndShowColdStartAppOpen(
                    maxWaitMs = MAX_APP_OPEN_WAIT_MS,
                    onAdShowed = {
                        _splashVisible.value = false
                    },
                )
                _splashVisible.value = false
            } finally {
                _splashProgress.value = 1.0f
                _splashVisible.value = false
                _coldStartSequenceCompleted.value = true
            }
        }
    }

    companion object {
        const val MIN_SPLASH_PRELOAD_WINDOW_MS = 3_200L
        const val MAX_BANNER_SPLASH_WAIT_MS = 5_500L
        const val POST_BANNER_LOAD_DISPLAY_HOLD_MS = 2_800L
        private const val NOTIFICATION_REVEAL_TIMEOUT_MS = 220L
        const val MAX_APP_OPEN_WAIT_MS = 5_000L
    }
}

package com.sleepsounds.storytelling.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.sleepsounds.storytelling.DeepSleepApplication
import com.sleepsounds.storytelling.MainActivity
import com.sleepsounds.storytelling.util.R2Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Handle exposing a preloaded or active [AdView] and its loaded state for a specific Banner Slot,
 * modeled 1:1 after the proven `BannerSlotHandle` / `InlineAdaptiveBannerSlot` architecture.
 */
class BannerSlotHandle(
    val adViewFlow: StateFlow<AdView?>,
    val isLoadedFlow: StateFlow<Boolean>,
)

/**
 * Multi-ID Inline Adaptive Banner Preload Pool Entry (Zero Layout Interference):
 * - Preloads Inline Adaptive Banners (`AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize`)
 *   in parallel across the 5 Production Banner AdUnit IDs using each slot's exact container width.
 * - Never calls `adView.measure()`, `adView.layout()`, or touches `adView.getChildAt(0)`, allowing
 *   Google's native `BaseAdView` and `WebView` to measure and render creatives 100% natively.
 */
private class BannerPoolEntry(
    val channelKey: String,
    val adViewFlow: MutableStateFlow<AdView?> = MutableStateFlow(null),
    val isLoadedFlow: MutableStateFlow<Boolean> = MutableStateFlow(false),
    var widthDp: Int = 0,
    var isStickyCompact: Boolean = false,
    var poolAttemptIndex: Int = 0,
    var isLoadingInFlight: Boolean = false,
    var activeLoadingAdUnitId: String? = null,
    var attachedHostCount: Int = 0,
    var lastLoadStartedElapsedMs: Long = 0L,
    var nextEligibleLoadElapsedMs: Long = 0L,
)

private val bannerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
private val bannerPool = ConcurrentHashMap<String, BannerPoolEntry>()

// Per-AdUnit-ID cooldown map (elapsedRealtime ms) to strictly prevent Error Code 1 ("Too many recently failed requests")
private val adUnitCooldownUntilElapsedMs = ConcurrentHashMap<String, Long>()

private var currentActivityRef: WeakReference<Activity>? = null
private var bannerOwnerActivityHash: Int = 0

@Volatile
private var currentStageKey: String = AdConfigManager.PLACEMENT_BANNER_SPLASH

@Volatile
private var heartbeatStarted: Boolean = false

// Only one-time startup/onboarding screens are destroyed on exit; main tabs & subpages stay cached in RAM
private val oneTimeOnboardingSlots = setOf(
    AdConfigManager.PLACEMENT_BANNER_SPLASH,
    AdConfigManager.PLACEMENT_BANNER_LANGUAGE_1,
    AdConfigManager.PLACEMENT_BANNER_LANGUAGE_1_CLICK,
    AdConfigManager.PLACEMENT_BANNER_ONBOARDING_1,
    AdConfigManager.PLACEMENT_BANNER_ONBOARDING_2,
)

private val mainAppStageKeys = setOf(
    AdConfigManager.PLACEMENT_BANNER_COLLAPSIBLE_HOME,
    AdConfigManager.PLACEMENT_BANNER_HOME,
    AdConfigManager.PLACEMENT_BANNER_ALL,
    "sounds_tab_banner",
    "tracker_tab_banner",
    "journal_tab_banner",
    "products_tab_banner",
    "profile_tab_banner",
)

private fun ensureBannerHeartbeatStarted() {
    if (heartbeatStarted) return
    heartbeatStarted = true
    bannerScope.launch {
        while (true) {
            delay(12_000L)
            val act = resolveValidHostActivity() ?: continue
            val manager = resolveAdMobManager(act) ?: continue
            if (manager.shouldShowAds.value &&
                manager.sdkReady.value &&
                !manager.isShowingFullScreenAdState.value &&
                isActivityStarted(act)
            ) {
                prewarmBannersForStage(currentStageKey, act)
            }
        }
    }
}

private fun resolveValidHostActivity(fallback: Activity? = null): Activity? {
    val candidate = fallback?.takeIf { !it.isFinishing && !it.isDestroyed && it !is AdActivity }
        ?: currentActivityRef?.get()?.takeIf { !it.isFinishing && !it.isDestroyed && it !is AdActivity }
    if (candidate != null) return candidate
    return null
}

private fun isActivityStarted(activity: Activity): Boolean {
    if (activity.isFinishing || activity.isDestroyed) return false
    val lifecycle = (activity as? LifecycleOwner)?.lifecycle?.currentState
    return lifecycle == null || lifecycle.isAtLeast(Lifecycle.State.STARTED)
}

private fun resolveAdMobManager(context: Context): AdMobManager? =
    (context.findActivity() as? MainActivity)?.adMobManager
        ?: (context.applicationContext as? DeepSleepApplication)?.adMobManager

private fun bindBannerHostActivity(activity: Activity) {
    if (activity is AdActivity || activity.isFinishing || activity.isDestroyed) return
    ensureBannerHeartbeatStarted()
    val newHash = System.identityHashCode(activity)
    if (bannerOwnerActivityHash != 0 && bannerOwnerActivityHash != newHash) {
        clearAllCachedBanners()
    }
    bannerOwnerActivityHash = newHash
    currentActivityRef = WeakReference(activity)
}

/**
 * Resolves the singleton [AdMobManager] from the host [MainActivity] or [DeepSleepApplication].
 */
@Composable
fun rememberAdMobManager(): AdMobManager? {
    val context = LocalContext.current
    return remember(context) {
        resolveAdMobManager(context)
    }
}

/**
 * Maps each UI `slotKey` to its dedicated Banner Placement channel:
 * - Each main bottom tab (`Home`, `Sounds`, `Tracker`, `Journal`, `Products`, `Profile`) has its own
 *   persistent [BannerPoolEntry] in RAM so switching tabs NEVER steals or detaches another tab's loaded [AdView].
 * - Within `SoundsTabScreen`, all 7 sub-tabs share `"sounds_tab_banner"` so switching sub-tabs re-attaches
 *   the warm `Sounds` [AdView] in `0ms`.
 */
fun resolveBannerChannelKey(slotKey: String): String = when (slotKey) {
    AdConfigManager.PLACEMENT_BANNER_SPLASH,
    AdConfigManager.PLACEMENT_BANNER_LANGUAGE_1,
    AdConfigManager.PLACEMENT_BANNER_LANGUAGE_1_CLICK,
    AdConfigManager.PLACEMENT_BANNER_ONBOARDING_1,
    AdConfigManager.PLACEMENT_BANNER_ONBOARDING_2,
    AdConfigManager.PLACEMENT_BANNER_COLLAPSIBLE_HOME,
    AdConfigManager.PLACEMENT_BANNER_HOME,
    -> slotKey

    "home_banner_1",
    "default",
    -> AdConfigManager.PLACEMENT_BANNER_HOME

    "home_banner_2" -> AdConfigManager.PLACEMENT_BANNER_ALL

    "sounds_whitenoise_banner",
    "sounds_mixes_banner",
    "sounds_featured_banner",
    "sounds_music_banner",
    "sounds_stories_banner",
    "sounds_meditation_banner",
    "sounds_soundscapes_banner",
    "sounds_tab_banner",
    -> "sounds_tab_banner"

    "tracker_tab_banner",
    "journal_tab_banner",
    "products_tab_banner",
    "profile_tab_banner",
    "paywall_screen_banner",
    "dream_dictionary_banner",
    "mix_detail_banner",
    "mixer_screen_banner",
    "podcard_detail_banner",
    "lesson_player_banner",
    "breathing_screen_banner",
    "terms_screen_banner",
    -> slotKey

    else -> AdConfigManager.PLACEMENT_BANNER_HOME
}

/**
 * Resolves the full screen width in dp consistently across `preloadBannerChannel` and `AdaptiveBannerAdBar`.
 */
fun resolveConsistentScreenWidthDp(context: Context): Int {
    val dm = context.resources.displayMetrics
    if (dm.widthPixels > 0 && dm.density > 0f) {
        return (dm.widthPixels / dm.density).roundToInt().coerceAtLeast(320)
    }
    return context.resources.configuration.screenWidthDp.coerceAtLeast(320)
}

private fun isStickyBottomSlot(channelKey: String, fullBleedSticky: Boolean = false): Boolean =
    fullBleedSticky ||
        channelKey == AdConfigManager.PLACEMENT_BANNER_SPLASH ||
        channelKey == AdConfigManager.PLACEMENT_BANNER_LANGUAGE_1 ||
        channelKey == AdConfigManager.PLACEMENT_BANNER_LANGUAGE_1_CLICK ||
        channelKey == AdConfigManager.PLACEMENT_BANNER_ONBOARDING_1 ||
        channelKey == AdConfigManager.PLACEMENT_BANNER_ONBOARDING_2 ||
        channelKey == AdConfigManager.PLACEMENT_BANNER_COLLAPSIBLE_HOME

/**
 * Computes the exact container width in `dp` for a given [channelKey] when prewarming off-screen or mounting,
 * matching the horizontal padding of each screen's layout (`20.dp` start + `20.dp` end = `40.dp` for inline slots;
 * `0.dp` for full-bleed sticky slots) so the preloaded `AdView` never overflows or gets clipped on left/right edges.
 */
fun defaultWidthDpForSlot(
    context: Context,
    channelKey: String,
    fullBleedSticky: Boolean = false,
): Int {
    val screenWidthDp = resolveConsistentScreenWidthDp(context)
    val horizontalPaddingDp = if (isStickyBottomSlot(channelKey, fullBleedSticky)) {
        0
    } else {
        40
    }
    return (screenWidthDp - horizontalPaddingDp).coerceAtLeast(300)
}

private fun getStagePipelineKeys(stageKey: String): List<String> = when (stageKey) {
    AdConfigManager.PLACEMENT_BANNER_SPLASH -> listOf(
        AdConfigManager.PLACEMENT_BANNER_SPLASH,
        AdConfigManager.PLACEMENT_BANNER_COLLAPSIBLE_HOME,
        AdConfigManager.PLACEMENT_BANNER_HOME,
        "sounds_tab_banner",
    )

    AdConfigManager.PLACEMENT_BANNER_LANGUAGE_1,
    AdConfigManager.PLACEMENT_BANNER_LANGUAGE_1_CLICK,
    -> listOf(
        stageKey,
        AdConfigManager.PLACEMENT_BANNER_LANGUAGE_1_CLICK,
        AdConfigManager.PLACEMENT_BANNER_ONBOARDING_1,
        AdConfigManager.PLACEMENT_BANNER_ONBOARDING_2,
        AdConfigManager.PLACEMENT_BANNER_HOME,
    ).distinct()

    AdConfigManager.PLACEMENT_BANNER_ONBOARDING_1,
    AdConfigManager.PLACEMENT_BANNER_ONBOARDING_2,
    -> listOf(
        stageKey,
        AdConfigManager.PLACEMENT_BANNER_ONBOARDING_2,
        AdConfigManager.PLACEMENT_BANNER_HOME,
        AdConfigManager.PLACEMENT_BANNER_COLLAPSIBLE_HOME,
        "sounds_tab_banner",
    ).distinct()

    else -> listOf(
        stageKey,
        AdConfigManager.PLACEMENT_BANNER_COLLAPSIBLE_HOME,
        AdConfigManager.PLACEMENT_BANNER_HOME,
        "sounds_tab_banner",
        "tracker_tab_banner",
        "journal_tab_banner",
        "products_tab_banner",
        "profile_tab_banner",
        AdConfigManager.PLACEMENT_BANNER_ALL,
    ).distinct()
}

/**
 * Selects an eligible Banner AdUnit ID for [entry] that is:
 * 1. Not currently in a failure/rate-limit cooldown window (`adUnitCooldownUntilElapsedMs`), and
 * 2. Not already being loaded concurrently by another slot (`activeLoadingAdUnitId`),
 * preventing AdMob Error Code 1 ("Too many recently failed requests for ad unit ID").
 * Actively visible slots (`entry.attachedHostCount > 0`) are never starved by background preloads.
 */
private fun selectAvailableBannerAdUnitId(
    context: Context,
    entry: BannerPoolEntry,
    nowElapsedMs: Long,
): String? {
    val config = AdConfigManager.getConfig(context, entry.channelKey)
    val adUnitPool = AdConfigManager.bannerAdUnitPoolForSlot(entry.channelKey, config.id)
    if (adUnitPool.isEmpty()) return null

    val inFlightAdUnitIds = bannerPool.values.mapNotNull { other ->
        if (other !== entry && other.isLoadingInFlight && (nowElapsedMs - other.lastLoadStartedElapsedMs) < 12_000L) {
            other.activeLoadingAdUnitId
        } else {
            null
        }
    }.toSet()

    // Pass 1: Prefer an AdUnit ID that is both out of cooldown and not currently in-flight
    for (offset in adUnitPool.indices) {
        val candidateId = adUnitPool[(entry.poolAttemptIndex + offset) % adUnitPool.size]
        val cooldownUntil = adUnitCooldownUntilElapsedMs[candidateId] ?: 0L
        if (nowElapsedMs >= cooldownUntil && candidateId !in inFlightAdUnitIds) {
            entry.poolAttemptIndex = (entry.poolAttemptIndex + offset) % adUnitPool.size
            return candidateId
        }
    }

    // Pass 2: If this slot is actively mounted on screen (attachedHostCount > 0), never let off-screen
    // preloads block it — pick any candidate ID that is not in an error cooldown.
    if (entry.attachedHostCount > 0) {
        for (offset in adUnitPool.indices) {
            val candidateId = adUnitPool[(entry.poolAttemptIndex + offset) % adUnitPool.size]
            val cooldownUntil = adUnitCooldownUntilElapsedMs[candidateId] ?: 0L
            if (nowElapsedMs >= cooldownUntil) {
                entry.poolAttemptIndex = (entry.poolAttemptIndex + offset) % adUnitPool.size
                return candidateId
            }
        }
    }
    return null
}

/**
 * Releases the `AdView` and its Chromium WebView/video decoder held by an off-screen [entry].
 */
private fun destroyBannerEntry(entry: BannerPoolEntry) {
    val view = entry.adViewFlow.value
    entry.adViewFlow.value = null
    entry.isLoadedFlow.value = false
    entry.isLoadingInFlight = false
    entry.activeLoadingAdUnitId = null
    if (view != null) {
        runCatching {
            view.visibility = View.GONE
            view.pause()
            (view.parent as? ViewGroup)?.removeView(view)
            view.destroy()
        }
    }
}

private fun onBannerStageEntered(stageKey: String, activity: Activity) {
    currentStageKey = stageKey
    // Once the user enters the main app tabs, release one-time onboarding & splash banners
    if (stageKey in mainAppStageKeys) {
        for (onboardingKey in oneTimeOnboardingSlots) {
            val entry = bannerPool[onboardingKey]
            if (entry != null && entry.attachedHostCount == 0 && (entry.adViewFlow.value != null || entry.isLoadingInFlight)) {
                destroyBannerEntry(entry)
            }
        }
    }
    prewarmBannersForStage(stageKey, activity)
}

private fun prewarmBannersForStage(stageKey: String, hostActivity: Activity? = null) {
    bannerScope.launch {
        val act = resolveValidHostActivity(hostActivity) ?: return@launch
        val manager = resolveAdMobManager(act) ?: return@launch
        if (!manager.shouldShowAds.value || !manager.sdkReady.value) {
            return@launch
        }
        val now = SystemClock.elapsedRealtime()
        val pipelineKeys = getStagePipelineKeys(stageKey)

        // 1. Always prioritize any currently visible/attached slot (attachedHostCount > 0),
        //    even while a full-screen Splash or Interstitial ad is covering the screen so the
        //    banner is 100% ready the instant the user closes the full-screen ad.
        for ((_, entry) in bannerPool) {
            if (entry.attachedHostCount > 0 &&
                !entry.isLoadedFlow.value &&
                !entry.isLoadingInFlight &&
                now >= entry.nextEligibleLoadElapsedMs
            ) {
                val widthDp = if (entry.widthDp > 0) {
                    entry.widthDp
                } else {
                    defaultWidthDpForSlot(act, entry.channelKey, entry.isStickyCompact)
                }
                startBannerEntryLoad(entry, act, widthDp)
            }
        }

        // Do not start off-screen speculative preloads while a full-screen ad is showing
        if (manager.isShowingFullScreenAdState.value) {
            return@launch
        }

        // 2. Preload up to 1 unloaded off-screen slot at a time in the current stage pipeline
        //    so background preloads never saturate WebView/MediaCodec resources or compete with active UI.
        var inFlightCount = bannerPool.values.count {
            it.isLoadingInFlight && (now - it.lastLoadStartedElapsedMs) < 12_000L
        }

        for (key in pipelineKeys) {
            if (inFlightCount >= 1) break
            val sticky = isStickyBottomSlot(key)
            val expectedWidthDp = defaultWidthDpForSlot(act, key, sticky)
            val entry = bannerPool.getOrPut(key) {
                BannerPoolEntry(
                    channelKey = key,
                    widthDp = expectedWidthDp,
                    isStickyCompact = sticky,
                )
            }
            if (!entry.isLoadedFlow.value &&
                !entry.isLoadingInFlight &&
                now >= entry.nextEligibleLoadElapsedMs
            ) {
                val targetWidth = entry.widthDp.takeIf { it > 0 } ?: expectedWidthDp
                if (startBannerEntryLoad(entry, act, targetWidth)) {
                    inFlightCount++
                }
            }
        }
    }
}

private fun obtainBannerSlot(
    uiContext: Context,
    channelKey: String,
    requestedWidthDp: Int,
    fullBleedSticky: Boolean,
): BannerSlotHandle {
    val resolvedAct = resolveValidHostActivity(uiContext.findActivity())
    if (resolvedAct != null) {
        bindBannerHostActivity(resolvedAct)
    }
    val hostCtx = resolvedAct ?: uiContext
    val isSticky = isStickyBottomSlot(channelKey, fullBleedSticky)
    val targetWidthDp = if (requestedWidthDp > 0) {
        requestedWidthDp.coerceAtLeast(300)
    } else {
        defaultWidthDpForSlot(hostCtx, channelKey, isSticky)
    }

    val entry = bannerPool.getOrPut(channelKey) {
        BannerPoolEntry(
            channelKey = channelKey,
            widthDp = targetWidthDp,
            isStickyCompact = isSticky,
        )
    }
    entry.isStickyCompact = isSticky

    val manager = resolveAdMobManager(hostCtx)
    val adsAllowed = manager?.shouldShowAds?.value == true && manager.sdkReady.value

    // Keep already-loaded banners intact when returning to a tab! Only update width if not yet loaded.
    if (!entry.isLoadedFlow.value && entry.widthDp > 0 && abs(entry.widthDp - targetWidthDp) > 6) {
        entry.widthDp = targetWidthDp
        entry.isLoadingInFlight = false
        entry.activeLoadingAdUnitId = null
        if (resolvedAct != null && adsAllowed) {
            bannerScope.launch {
                startBannerEntryLoad(entry, resolvedAct, targetWidthDp)
            }
        }
    } else if (adsAllowed) {
        if (entry.widthDp <= 0) {
            entry.widthDp = targetWidthDp
        }
        if (!entry.isLoadedFlow.value && !entry.isLoadingInFlight && resolvedAct != null) {
            bannerScope.launch {
                startBannerEntryLoad(entry, resolvedAct, entry.widthDp)
            }
        }
    }

    return BannerSlotHandle(
        adViewFlow = entry.adViewFlow.asStateFlow(),
        isLoadedFlow = entry.isLoadedFlow.asStateFlow(),
    )
}

private fun onBannerSlotAttached(
    channelKey: String,
    hostActivity: Activity?,
    fullBleedSticky: Boolean,
) {
    val act = resolveValidHostActivity(hostActivity)
    if (act != null) {
        bindBannerHostActivity(act)
    }
    val isSticky = isStickyBottomSlot(channelKey, fullBleedSticky)
    val entry = bannerPool.getOrPut(channelKey) {
        val initialWidth = if (act != null) defaultWidthDpForSlot(act, channelKey, isSticky) else 0
        BannerPoolEntry(
            channelKey = channelKey,
            widthDp = initialWidth,
            isStickyCompact = isSticky,
        )
    }
    entry.isStickyCompact = isSticky
    entry.attachedHostCount += 1

    val manager = if (act != null) resolveAdMobManager(act) else null
    if (manager?.isShowingFullScreenAdState?.value != true) {
        entry.adViewFlow.value?.let { adView ->
            runCatching { adView.resume() }
        }
    }

    if (act != null && manager?.shouldShowAds?.value == true && manager.sdkReady.value) {
        if (!entry.isLoadedFlow.value && !entry.isLoadingInFlight) {
            val widthDp = entry.widthDp.takeIf { it > 0 } ?: defaultWidthDpForSlot(act, channelKey, isSticky)
            entry.nextEligibleLoadElapsedMs = 0L
            bannerScope.launch {
                startBannerEntryLoad(entry, act, widthDp)
            }
        }
        onBannerStageEntered(channelKey, act)
    }
}

private fun onBannerSlotDetached(channelKey: String) {
    val entry = bannerPool[channelKey] ?: return
    entry.attachedHostCount = (entry.attachedHostCount - 1).coerceAtLeast(0)
    if (entry.attachedHostCount == 0) {
        // Immediately destroy one-time cold-start/onboarding banners when their screen closes
        // so their Chromium WebView never runs background VSYNC frame-rate callbacks.
        if (channelKey == AdConfigManager.PLACEMENT_BANNER_SPLASH ||
            (channelKey in oneTimeOnboardingSlots && currentStageKey in mainAppStageKeys)
        ) {
            destroyBannerEntry(entry)
            return
        }
        // Keep main tab & screen banners cached in memory so switching back to any tab
        // shows its banner immediately without disappearing; hide, pause & detach from old container.
        entry.adViewFlow.value?.let { adView ->
            runCatching {
                if (adView.measuredHeight > 0) {
                    adView.minimumHeight = adView.measuredHeight
                }
                adView.visibility = View.GONE
                adView.pause()
                (adView.parent as? ViewGroup)?.removeView(adView)
            }
        }
    }
}

private fun startBannerEntryLoad(
    entry: BannerPoolEntry,
    activity: Activity,
    adWidthDp: Int,
): Boolean {
    val manager = resolveAdMobManager(activity) ?: return false
    if (!manager.shouldShowAds.value || !manager.sdkReady.value) return false
    if (entry.isLoadedFlow.value) return false

    val config = AdConfigManager.getConfig(activity, entry.channelKey)
    if (!config.isEnable) return false

    val now = SystemClock.elapsedRealtime()
    if (entry.isLoadingInFlight && (now - entry.lastLoadStartedElapsedMs) < 12_000L) return false
    if (now < entry.nextEligibleLoadElapsedMs && entry.attachedHostCount == 0) return false

    val targetAdUnitId = selectAvailableBannerAdUnitId(activity, entry, now)
    if (targetAdUnitId == null) {
        // All candidate Banner AdUnit IDs are either in-flight or cooling down; schedule a gentle check after cooldown
        val earliestReady = adUnitCooldownUntilElapsedMs.values.minOrNull() ?: (now + 4_500L)
        val waitMs = (earliestReady - now).coerceIn(1_500L, 12_000L)
        entry.nextEligibleLoadElapsedMs = now + waitMs
        bannerScope.launch {
            delay(waitMs)
            val validAct = resolveValidHostActivity()
            if (validAct != null &&
                isActivityStarted(validAct) &&
                !entry.isLoadedFlow.value &&
                !entry.isLoadingInFlight
            ) {
                startBannerEntryLoad(entry, validAct, entry.widthDp.takeIf { it > 0 } ?: adWidthDp)
            }
        }
        return false
    }

    entry.isLoadingInFlight = true
    entry.activeLoadingAdUnitId = targetAdUnitId
    entry.lastLoadStartedElapsedMs = now
    entry.widthDp = adWidthDp

    val adSize = if (entry.isStickyCompact) {
        AdSize.getInlineAdaptiveBannerAdSize(adWidthDp, 60)
    } else {
        AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(activity, adWidthDp)
    }

    val newAdView = AdView(activity).apply {
        adUnitId = targetAdUnitId
        setAdSize(adSize)
        if (entry.attachedHostCount == 0) {
            visibility = View.GONE
        }
    }

    newAdView.onPaidEventListener = OnPaidEventListener { adValue: AdValue ->
        R2Log.i("Banner [${entry.channelKey}] paid event: ${adValue.valueMicros} ${adValue.currencyCode}")
    }

    newAdView.adListener = object : AdListener() {
        override fun onAdLoaded() {
            // Ignore stale callback if a newer AdView has already replaced this instance
            if (entry.adViewFlow.value !== newAdView) {
                runCatching { newAdView.destroy() }
                return
            }
            if (entry.channelKey in oneTimeOnboardingSlots &&
                entry.attachedHostCount == 0 &&
                currentStageKey in mainAppStageKeys
            ) {
                destroyBannerEntry(entry)
                return
            }
            entry.isLoadingInFlight = false
            entry.activeLoadingAdUnitId = null
            entry.poolAttemptIndex = 0
            entry.nextEligibleLoadElapsedMs = 0L
            adUnitCooldownUntilElapsedMs.remove(targetAdUnitId)
            entry.isLoadedFlow.value = true

            // Hide and pause off-screen preloaded AdViews immediately so their ExoPlayer/WebView
            // does not decode video, consume Chromium tile memory, or spam setRequestedFrameRate(NaN).
            val fullScreenShowing = manager.isShowingFullScreenAdState.value
            if (entry.attachedHostCount == 0) {
                runCatching {
                    newAdView.visibility = View.GONE
                    newAdView.pause()
                }
            } else if (fullScreenShowing || !isActivityStarted(activity)) {
                runCatching { newAdView.pause() }
            }
            Log.d(
                "AdMobDebug",
                "Banner [${entry.channelKey}] tải thành công! (unit=$targetAdUnitId, width=${adWidthDp}dp, adSize=${newAdView.adSize}, responseInfo=${newAdView.responseInfo})",
            )
            R2Log.i(
                "InlineBanner [${entry.channelKey}] READY (id=$targetAdUnitId, width=${adWidthDp}dp, adSize=${newAdView.adSize})",
            )
            prewarmBannersForStage(currentStageKey, activity)
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
            if (entry.adViewFlow.value !== newAdView) {
                runCatching { newAdView.destroy() }
                return
            }
            entry.isLoadingInFlight = false
            entry.activeLoadingAdUnitId = null
            val failNow = SystemClock.elapsedRealtime()

            val errorCode = adError.code
            val errorMessage = adError.message
            val domain = adError.domain
            val responseInfo = adError.responseInfo

            Log.e(
                "AdMobDebug",
                "Banner [${entry.channelKey}] tải thất bại - Mã lỗi: $errorCode | Domain: $domain | Unit: $targetAdUnitId | Chi tiết: $errorMessage | ResponseInfo: $responseInfo",
            )
            Log.e("AdMobDebug", "Thông tin đầy đủ Banner [${entry.channelKey}]: $adError")

            // Apply per-AdUnit-ID cooldown so AdMob never triggers Code 1 ("Too many recently failed requests")
            val unitCooldownMs = if (errorCode == 1) 12_000L else 4_500L
            adUnitCooldownUntilElapsedMs[targetAdUnitId] = failNow + unitCooldownMs

            R2Log.w(
                "InlineBanner [${entry.channelKey}] failed on id=$targetAdUnitId (code=$errorCode): $errorMessage",
            )
            // Clean up failed AdView WebView immediately so we don't leak or trigger #004
            entry.adViewFlow.value = null
            runCatching {
                (newAdView.parent as? ViewGroup)?.removeView(newAdView)
                newAdView.destroy()
            }

            if (manager.shouldShowAds.value && !entry.isLoadedFlow.value) {
                val adUnitPool = AdConfigManager.bannerAdUnitPoolForSlot(entry.channelKey, config.id)
                entry.poolAttemptIndex = (entry.poolAttemptIndex + 1) % adUnitPool.size.coerceAtLeast(1)
                val retryDelayMs = if (errorCode == 1) 5_500L else 1_800L
                entry.nextEligibleLoadElapsedMs = failNow + retryDelayMs
                bannerScope.launch {
                    delay(retryDelayMs)
                    val validAct = resolveValidHostActivity()
                    if (validAct != null &&
                        isActivityStarted(validAct) &&
                        manager.shouldShowAds.value &&
                        !entry.isLoadedFlow.value &&
                        !entry.isLoadingInFlight
                    ) {
                        startBannerEntryLoad(entry, validAct, entry.widthDp.takeIf { it > 0 } ?: adWidthDp)
                    }
                }
            }
        }

        override fun onAdImpression() {
            R2Log.i("InlineBanner [${entry.channelKey}] impression recorded (id=$targetAdUnitId)")
        }
    }

    val previousAdView = entry.adViewFlow.value
    entry.adViewFlow.value = newAdView
    if (previousAdView != null && previousAdView !== newAdView) {
        runCatching {
            previousAdView.pause()
            (previousAdView.parent as? ViewGroup)?.removeView(previousAdView)
            previousAdView.destroy()
        }
    }

    runCatching {
        newAdView.loadAd(AdRequest.Builder().build())
        R2Log.i(
            "InlineBanner [${entry.channelKey}] loadAd fired (id=$targetAdUnitId, width=${adWidthDp}dp, sticky=${entry.isStickyCompact})",
        )
    }.onFailure {
        entry.isLoadingInFlight = false
        entry.activeLoadingAdUnitId = null
        return false
    }
    return true
}

/**
 * Returns `true` if the specified Banner placement channel has finished loading its creative in RAM.
 */
fun isBannerChannelLoaded(slotKey: String): Boolean {
    val channelKey = resolveBannerChannelKey(slotKey)
    return bannerPool[channelKey]?.isLoadedFlow?.value == true
}

/**
 * Retries failed or unrequested banner channels that are currently mounted/active on screen
 * or in the current stage pipeline.
 */
fun retryAllFailedBannerChannels() {
    if (Looper.myLooper() != Looper.getMainLooper()) {
        Handler(Looper.getMainLooper()).post { retryAllFailedBannerChannels() }
        return
    }
    val act = resolveValidHostActivity() ?: return
    prewarmBannersForStage(currentStageKey, act)
}

fun clearAllCachedBanners() {
    if (Looper.myLooper() != Looper.getMainLooper()) {
        Handler(Looper.getMainLooper()).post { clearAllCachedBanners() }
        return
    }
    val entries = bannerPool.values.toList()
    bannerPool.clear()
    adUnitCooldownUntilElapsedMs.clear()
    entries.forEach { entry ->
        destroyBannerEntry(entry)
    }
}

/**
 * Preloads a specific Banner placement channel into memory ahead of time (Look-Ahead Preloading)
 * for immediate transitions (`banner_language_1_click` -> `banner_onboarding_1`, and main bottom tabs).
 */
fun preloadBannerChannel(
    context: Context,
    adMobManager: AdMobManager?,
    slotKey: String,
    screenWidthDp: Int = resolveConsistentScreenWidthDp(context),
) {
    if (Looper.myLooper() != Looper.getMainLooper()) {
        Handler(Looper.getMainLooper()).post {
            preloadBannerChannel(context, adMobManager, slotKey, screenWidthDp)
        }
        return
    }
    val manager = adMobManager ?: return
    if (!manager.shouldShowAds.value || !manager.sdkReady.value) return

    val activity = resolveValidHostActivity(context.findActivity() ?: manager.currentHostActivity()) ?: return
    bindBannerHostActivity(activity)
    val channelKey = resolveBannerChannelKey(slotKey)
    val config = AdConfigManager.getConfig(activity, channelKey)
    if (!config.isEnable || config.id.isBlank()) return

    val isSticky = isStickyBottomSlot(channelKey)
    val targetWidthDp = defaultWidthDpForSlot(activity, channelKey, isSticky)
    val entry = bannerPool.getOrPut(channelKey) {
        BannerPoolEntry(
            channelKey = channelKey,
            widthDp = targetWidthDp,
            isStickyCompact = isSticky,
        )
    }
    if (!entry.isLoadedFlow.value && !entry.isLoadingInFlight) {
        bannerScope.launch {
            startBannerEntryLoad(entry, activity, targetWidthDp)
        }
    }
}

/**
 * Persistent Sticky Adaptive Banner bar placed at the bottom of `DeepSleepNavHost`.
 */
@Composable
fun StickyCollapsibleBannerAdBar(
    modifier: Modifier = Modifier,
    adMobManager: AdMobManager? = rememberAdMobManager(),
    visible: Boolean = true,
) {
    AdaptiveBannerAdBar(
        modifier = modifier,
        adMobManager = adMobManager,
        slotKey = AdConfigManager.PLACEMENT_BANNER_COLLAPSIBLE_HOME,
        visible = visible,
        fullBleedSticky = true,
    )
}

/**
 * 100% Native Google AdMob Inline Adaptive Banner Slot (`AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize`)
 * ported directly from the proven `InlineAdaptiveBannerSlot` architecture:
 * - Backed by the 5-ID parallel preload pool so preloaded banners appear immediately (`0ms`).
 * - Zero custom `onMeasure` / `onLayout` / `Modifier.layout` intervention: Google's `AdView` (`BaseAdView`)
 *   measures and lays out its own internal creative natively inside a standard `FrameLayout`.
 * - Renders nothing (`0dp` height, zero empty widget frame, zero black background) while `!isLoaded`,
 *   and once loaded, renders within the exact available container width without horizontal clipping.
 */
@Composable
fun AdaptiveBannerAdBar(
    modifier: Modifier = Modifier,
    adMobManager: AdMobManager? = rememberAdMobManager(),
    slotKey: String = "default",
    visible: Boolean = true,
    allowDuringSplash: Boolean = false,
    allowDuringOnboarding: Boolean = false,
    fullBleedSticky: Boolean = false,
) {
    val manager = adMobManager ?: return
    val shouldShowAds by manager.shouldShowAds.collectAsStateWithLifecycle()
    val sdkReady by manager.sdkReady.collectAsStateWithLifecycle()
    val isFullScreenAdShowing by manager.isShowingFullScreenAdState.collectAsStateWithLifecycle()
    val networkTick by manager.networkAvailableTick.collectAsStateWithLifecycle()

    val uiContext = LocalContext.current
    val channelKey = remember(slotKey) { resolveBannerChannelKey(slotKey) }
    val adConfig = remember(uiContext, channelKey) {
        AdConfigManager.getConfig(uiContext, channelKey)
    }

    LaunchedEffect(shouldShowAds) {
        if (!shouldShowAds) {
            clearAllCachedBanners()
        }
    }

    val hostActivity = LocalActivity.current
        ?: remember(uiContext) { uiContext.findActivity() }
        ?: manager.currentHostActivity()
        ?: return

    val mainActivity = hostActivity as? MainActivity
    val splashVisibleFlow = remember(mainActivity) {
        mainActivity?.startupSplashCoordinator?.splashVisible
    }
    val nextScreenPreloadUnlockedFlow = remember(mainActivity) {
        mainActivity?.startupSplashCoordinator?.nextScreenBannerPreloadUnlocked
    }
    val isSplashCovering by (splashVisibleFlow?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(value = false) })
    val isNextScreenPreloadUnlocked by (nextScreenPreloadUnlockedFlow?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(value = true) })

    val waitingForSplashBannerPriority = !allowDuringSplash && isSplashCovering && !isNextScreenPreloadUnlocked
    if (!visible || !adConfig.isEnable || !shouldShowAds || !sdkReady || adConfig.id.isEmpty() || waitingForSplashBannerPriority) {
        return
    }

    val effectiveFullBleedSticky = fullBleedSticky || allowDuringOnboarding
    val handle: BannerSlotHandle = remember(channelKey, effectiveFullBleedSticky, hostActivity) {
        val targetWidthDp = defaultWidthDpForSlot(hostActivity, channelKey, effectiveFullBleedSticky)
        obtainBannerSlot(
            uiContext = hostActivity,
            channelKey = channelKey,
            requestedWidthDp = targetWidthDp,
            fullBleedSticky = effectiveFullBleedSticky,
        )
    }

    DisposableEffect(channelKey, slotKey, hostActivity) {
        onBannerSlotAttached(
            channelKey = channelKey,
            hostActivity = hostActivity,
            fullBleedSticky = effectiveFullBleedSticky,
        )
        onDispose {
            onBannerSlotDetached(channelKey)
        }
    }

    val adView: AdView? by handle.adViewFlow.collectAsStateWithLifecycle()
    val isLoaded: Boolean by handle.isLoadedFlow.collectAsStateWithLifecycle()
    val currentAdView = adView

    LaunchedEffect(networkTick) {
        if (networkTick > 0L && !isLoaded) {
            prewarmBannersForStage(channelKey, hostActivity)
        }
    }

    val coveredBySplashOverlay = !allowDuringSplash && isSplashCovering
    if (isLoaded && currentAdView != null && !coveredBySplashOverlay) {
        androidx.compose.runtime.key(currentAdView) {
            AndroidView(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { ctx ->
                    FrameLayout(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                        (currentAdView.parent as? ViewGroup)?.removeView(currentAdView)
                        currentAdView.visibility = View.VISIBLE
                        addView(
                            currentAdView,
                            FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                Gravity.CENTER_HORIZONTAL,
                            ),
                        )
                        if (isFullScreenAdShowing) {
                            runCatching { currentAdView.pause() }
                        } else {
                            runCatching { currentAdView.resume() }
                        }
                    }
                },
                update = { container ->
                    if (currentAdView.parent !== container) {
                        (currentAdView.parent as? ViewGroup)?.removeView(currentAdView)
                        container.removeAllViews()
                        currentAdView.visibility = View.VISIBLE
                        container.addView(
                            currentAdView,
                            FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                Gravity.CENTER_HORIZONTAL,
                            ),
                        )
                    }
                    if (currentAdView.measuredHeight > 0 && currentAdView.minimumHeight != currentAdView.measuredHeight) {
                        currentAdView.minimumHeight = currentAdView.measuredHeight
                    }
                    if (isFullScreenAdShowing) {
                        runCatching { currentAdView.pause() }
                    } else {
                        runCatching { currentAdView.resume() }
                    }
                },
                onRelease = { container ->
                    runCatching {
                        if (currentAdView.measuredHeight > 0) {
                            currentAdView.minimumHeight = currentAdView.measuredHeight
                        }
                        if (currentAdView.parent === container) {
                            currentAdView.visibility = View.GONE
                            currentAdView.pause()
                            container.removeView(currentAdView)
                        }
                    }
                },
            )
        }
    }
}

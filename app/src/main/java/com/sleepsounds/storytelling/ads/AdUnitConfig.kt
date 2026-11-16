package com.sleepsounds.storytelling.ads

import android.content.Context
import androidx.annotation.Keep
import com.sleepsounds.storytelling.BuildConfig
import com.sleepsounds.storytelling.util.R2Log
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/**
 * Placement-level configuration modeled after `assets/ad_config.json`,
 * supporting multi-ID Banner distribution across screens and categories + Interstitial + AppOpen + Rewarded.
 */
@Keep
data class AdUnitConfig(
    val key: String = "",
    val isEnable: Boolean = true,
    val id: String = "",
    val reloadIntervalSeconds: Int = 30,
    val collapsible: Boolean = false,
    val enableUaCheck: Boolean = false,
    val colorBg: String = "#141B30",
    val colorCTA: String = "#5D4BF4",
)

/**
 * Loads and caches placement configurations from `assets/ad_config.json`.
 * Maps Debug builds to Google's official Test Ad Unit IDs and distributes fallback Banner placements
 * across the 5 configured production Banner Ad Unit IDs.
 */
object AdConfigManager {
    private val configs = ConcurrentHashMap<String, AdUnitConfig>()
    @Volatile
    private var isLoaded = false
    @Volatile
    var globalEnableCheckUa: Boolean = false
        private set

    const val BANNER_ID_SPLASH_STICKY = "ca-app-pub-7072665289567101/1577095611"
    const val BANNER_ID_LANGUAGE_ONBOARDING = "ca-app-pub-7072665289567101/1657410522"
    const val BANNER_ID_HOME_INLINE = "ca-app-pub-7072665289567101/2671109294"
    const val BANNER_ID_SOUNDS_TRACKER = "ca-app-pub-7072665289567101/6718165510"
    const val BANNER_ID_JOURNAL_FEATURE = "ca-app-pub-7072665289567101/3373157732"

    private val productionBannerUnitIds = listOf(
        BANNER_ID_SPLASH_STICKY,
        BANNER_ID_LANGUAGE_ONBOARDING,
        BANNER_ID_HOME_INLINE,
        BANNER_ID_SOUNDS_TRACKER,
        BANNER_ID_JOURNAL_FEATURE,
    )

    private val debugTestBannerUnitIds = listOf(
        "ca-app-pub-3940256099942544/9214589741",
    )

    /**
     * Returns the ordered Banner waterfall pool for a specific placement [channelKey].
     * Adjacent screens and simultaneous banner slots use different starting Banner AdUnit IDs
     * so AdMob never throttles concurrent requests, and if the primary ID has no fill,
     * the slot automatically falls back to the remaining Banner IDs.
     */
    fun bannerAdUnitPoolForSlot(channelKey: String, primaryUnitId: String = ""): List<String> {
        if (BuildConfig.DEBUG) {
            // Always prioritize Google's official Adaptive Banner test unit ID (9214589741) first in Debug
            // so Inline Adaptive Banners never receive tiny 320x50 creatives or unrequested collapsible overlays.
            return debugTestBannerUnitIds
        }

        val id1 = BANNER_ID_SPLASH_STICKY        // ca-app-pub-7072665289567101/1577095611
        val id2 = BANNER_ID_LANGUAGE_ONBOARDING  // ca-app-pub-7072665289567101/1657410522
        val id3 = BANNER_ID_HOME_INLINE          // ca-app-pub-7072665289567101/2671109294
        val id4 = BANNER_ID_SOUNDS_TRACKER       // ca-app-pub-7072665289567101/6718165510
        val id5 = BANNER_ID_JOURNAL_FEATURE      // ca-app-pub-7072665289567101/3373157732

        val ordered = when (channelKey) {
            PLACEMENT_BANNER_SPLASH,
            PLACEMENT_BANNER_COLLAPSIBLE_HOME,
            -> listOf(id1, id3, id4, id2, id5)

            PLACEMENT_BANNER_LANGUAGE_1,
            PLACEMENT_BANNER_ONBOARDING_1,
            "tracker_tab_banner",
            "profile_tab_banner",
            -> listOf(id2, id3, id1, id4, id5)

            PLACEMENT_BANNER_LANGUAGE_1_CLICK,
            PLACEMENT_BANNER_ONBOARDING_2,
            PLACEMENT_BANNER_HOME,
            "products_tab_banner",
            -> listOf(id3, id1, id4, id5, id2)

            "sounds_tab_banner",
            "mix_detail_banner",
            "mixer_screen_banner",
            "podcard_detail_banner",
            "lesson_player_banner",
            -> listOf(id4, id3, id1, id5, id2)

            PLACEMENT_BANNER_ALL,
            "journal_tab_banner",
            "dream_dictionary_banner",
            "breathing_screen_banner",
            "terms_screen_banner",
            "paywall_screen_banner",
            -> listOf(id5, id1, id3, id4, id2)

            else -> listOf(id3, id1, id4, id2, id5)
        }

        if (primaryUnitId.isBlank() || ordered.firstOrNull() == primaryUnitId) {
            return ordered.distinct()
        }
        val result = ArrayList<String>(ordered.size + 1)
        result.add(primaryUnitId)
        for (unit in ordered) {
            if (unit != primaryUnitId) {
                result.add(unit)
            }
        }
        return result.distinct()
    }

    fun getCandidateBannerUnitIds(primaryUnitId: String): List<String> =
        bannerAdUnitPoolForSlot(channelKey = "", primaryUnitId = primaryUnitId)

    fun ensureLoaded(context: Context) {
        if (isLoaded) return
        synchronized(this) {
            if (isLoaded) return
            runCatching {
                val jsonText = context.assets.open("ad_config.json")
                    .bufferedReader()
                    .use { it.readText() }
                val root = JSONObject(jsonText)
                globalEnableCheckUa = root.optBoolean("enable_Check_UA", false)
                val keys = root.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val obj = root.optJSONObject(key) ?: continue
                    val rawId = obj.optString("id", "").trim()
                    val resolvedId = resolveUnitId(key, rawId)
                    configs[key] = AdUnitConfig(
                        key = key,
                        isEnable = obj.optBoolean("isEnable", true),
                        id = resolvedId,
                        reloadIntervalSeconds = obj.optInt("reloadIntervalSeconds", 30).coerceAtLeast(15),
                        collapsible = obj.optBoolean("collapsible", false),
                        enableUaCheck = obj.optBoolean("enable_Check_UA", false),
                        colorBg = obj.optString("colorBg", "#141B30"),
                        colorCTA = obj.optString("colorCTA", "#5D4BF4"),
                    )
                }
                isLoaded = true
                R2Log.d("AdConfigManager loaded ${configs.size} placements from assets/ad_config.json")
            }.onFailure { error ->
                R2Log.w("AdConfigManager fallback to default BuildConfig IDs: ${error.message}")
                isLoaded = true
            }
        }
    }

    fun getConfig(context: Context, key: String): AdUnitConfig {
        ensureLoaded(context)
        return configs[key] ?: createDefaultConfig(key)
    }

    private fun resolveFallbackBannerUnitId(key: String): String {
        if (BuildConfig.DEBUG) {
            return debugTestBannerUnitIds.first()
        }
        val index = abs(key.hashCode()) % productionBannerUnitIds.size
        return productionBannerUnitIds[index]
    }

    private fun resolveUnitId(key: String, configuredId: String): String {
        if (BuildConfig.DEBUG) {
            return when {
                key.startsWith("app_open") -> BuildConfig.APP_OPEN_ID.trim()
                key.startsWith("inter_") -> BuildConfig.INTERSTITIAL_ID.trim()
                key.startsWith("rewarded") -> BuildConfig.REWARDED_ID.trim()
                else -> resolveFallbackBannerUnitId(key)
            }
        }
        if (configuredId.isNotEmpty()) return configuredId
        return when {
            key.startsWith("app_open") -> BuildConfig.APP_OPEN_ID.trim()
            key.startsWith("inter_") -> BuildConfig.INTERSTITIAL_ID.trim()
            key.startsWith("rewarded") -> BuildConfig.REWARDED_ID.trim()
            else -> resolveFallbackBannerUnitId(key)
        }
    }

    private fun createDefaultConfig(key: String): AdUnitConfig = AdUnitConfig(
        key = key,
        isEnable = true,
        id = resolveUnitId(key, ""),
        reloadIntervalSeconds = 30,
        collapsible = false,
        enableUaCheck = false,
    )

    const val PLACEMENT_APP_OPEN_SPLASH = "app_open_splash"
    const val PLACEMENT_INTER_SPLASH = "inter_splash"
    const val PLACEMENT_BANNER_SPLASH = "banner_splash"
    const val PLACEMENT_BANNER_LANGUAGE_1 = "banner_language_1"
    const val PLACEMENT_BANNER_LANGUAGE_1_CLICK = "banner_language_1_click"
    const val PLACEMENT_BANNER_ONBOARDING_1 = "banner_onboarding_1"
    const val PLACEMENT_BANNER_ONBOARDING_2 = "banner_onboarding_2"
    const val PLACEMENT_INTER_ONBOARDING = "inter_onboarding"
    const val PLACEMENT_BANNER_COLLAPSIBLE_HOME = "banner_collapsible_home"
    const val PLACEMENT_BANNER_HOME = "banner_home"
    const val PLACEMENT_INTER_HOME = "inter_home"
    const val PLACEMENT_INTER_BACK = "inter_back"
    const val PLACEMENT_INTER_WELCOME_BACK = "inter_welcome_back"
    const val PLACEMENT_BANNER_ALL = "banner_all"
    const val PLACEMENT_REWARDED_MAIN = "rewarded_main"
}

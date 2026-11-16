package com.sleepsounds.storytelling.util

import androidx.annotation.StringRes
import com.sleepsounds.storytelling.R

/**
 * Registry of all 16 bundled ambient background wallpapers (all unlocked for free).
 */
object AppBackgrounds {
    data class Option(
        val id: String,
        val assetPath: String,
        @StringRes val labelRes: Int,
    )

    const val DEFAULT_ID: String = "free_starry"

    private val legacyBackgroundAliases: Map<String, String> = mapOf(
        "starry_sky" to "free_starry",
        "night_sky" to "free_moon",
        "wall_01" to "free_ocean",
        "wall_02" to "free_forest",
        "mixer" to "free_starry",
    )

    val all: List<Option> = listOf(
        Option("free_starry", "images/backgrounds/bg_free_starry.jpg", R.string.bg_free_starry),
        Option("free_moon", "images/backgrounds/bg_free_moon.jpg", R.string.bg_free_moon),
        Option("free_ocean", "images/backgrounds/bg_free_ocean.jpg", R.string.bg_free_ocean),
        Option("free_forest", "images/backgrounds/bg_free_forest.jpg", R.string.bg_free_forest),
        Option("premium_aurora", "images/backgrounds/bg_premium_aurora.jpg", R.string.bg_premium_aurora),
        Option("premium_nebula", "images/backgrounds/bg_premium_nebula.jpg", R.string.bg_premium_nebula),
        Option("premium_rain", "images/backgrounds/bg_premium_rain.jpg", R.string.bg_premium_rain),
        Option("premium_embers", "images/backgrounds/bg_premium_embers.jpg", R.string.bg_premium_embers),
        Option("premium_alpine", "images/backgrounds/bg_premium_alpine.jpg", R.string.bg_premium_alpine),
        Option("premium_desert", "images/backgrounds/bg_premium_desert.jpg", R.string.bg_premium_desert),
        Option("premium_bokeh", "images/backgrounds/bg_premium_bokeh.jpg", R.string.bg_premium_bokeh),
        Option("premium_wave", "images/backgrounds/bg_premium_wave.jpg", R.string.bg_premium_wave),
        Option("premium_depth", "images/backgrounds/bg_premium_depth.jpg", R.string.bg_premium_depth),
        Option("premium_mist", "images/backgrounds/bg_premium_mist.jpg", R.string.bg_premium_mist),
        Option("premium_aurora_lake", "images/backgrounds/bg_premium_aurora_lake.jpg", R.string.bg_premium_aurora_lake),
        Option("premium_dusk", "images/backgrounds/bg_premium_dusk.jpg", R.string.bg_premium_dusk),
    )

    fun normalizeId(id: String?): String {
        if (id.isNullOrBlank()) return DEFAULT_ID
        val canonicalId = legacyBackgroundAliases[id] ?: id
        return all.firstOrNull { it.id == canonicalId }?.id ?: DEFAULT_ID
    }

    fun optionFor(id: String): Option {
        val targetId = normalizeId(id)
        return all.firstOrNull { it.id == targetId } ?: all.first()
    }

    fun uriFor(id: String): String =
        BundledAssets.bundledUri(optionFor(id).assetPath)

    fun isAccessible(id: String, isPremium: Boolean = true): Boolean = true

    fun resolvedId(storedId: String?, isPremium: Boolean = true): String =
        normalizeId(storedId)

    fun uriForUser(storedId: String?, isPremium: Boolean = true): String =
        uriFor(normalizeId(storedId))
}

package com.sleepsounds.storytelling.util

/**
 * Maps soundscape and mixer category keys to their bundled APK icon assets.
 */
object CategoryIcons {
    const val CUSTOM_KEY: String = "CUSTOM"

    data class CategoryItem(
        val key: String,
        val label: String,
        val assetPath: String,
    )

    val all: List<CategoryItem> = listOf(
        CategoryItem("NATURE", "Nature", "images/icons/ic_category_nature.png"),
        CategoryItem("RAIN", "Rain", "images/icons/ic_category_water.png"),
        CategoryItem("WATER", "Water", "images/icons/ic_category_water.png"),
        CategoryItem("ANIMALS", "Animals", "images/icons/ic_category_wildlife.png"),
        CategoryItem("WHITE_NOISE", "White Noise", "images/icons/ic_category_urban.png"),
        CategoryItem("CITY", "City", "images/icons/ic_category_urban.png"),
        CategoryItem("INSTRUMENT", "Instruments", "images/icons/ic_category_instruments.png"),
        CategoryItem("MEDITATE", "Meditate", "images/icons/ic_category_meditate.png"),
    )

    val custom: CategoryItem = CategoryItem(
        key = CUSTOM_KEY,
        label = "Custom",
        assetPath = "images/icons/ic_category_custom.png",
    )

    fun forCategory(category: String): String? =
        if (category.equals(CUSTOM_KEY, ignoreCase = true)) {
            custom.assetPath
        } else {
            all.firstOrNull { it.key.equals(category, ignoreCase = true) }?.assetPath
        }

    fun resolveForMix(categories: List<String>): String =
        categories.firstOrNull { candidate ->
            !candidate.equals(CUSTOM_KEY, ignoreCase = true) &&
                all.any { it.key.equals(candidate, ignoreCase = true) }
        } ?: CUSTOM_KEY
}

package com.sleepsounds.storytelling.util

/**
 * Category filtering helpers for Home, Sounds, and Podcard browse tabs.
 */
object HomeBrowseCategories {
    fun matches(categories: List<String>, filterKey: String?): Boolean {
        if (filterKey.isNullOrBlank()) return true
        return categories.any { itemCategory -> itemCategory.equals(filterKey, ignoreCase = true) }
    }

    fun idsWithItems(
        categoryIds: List<String>,
        itemCategories: List<List<String>>,
    ): List<String> = categoryIds.filter { categoryId ->
        itemCategories.any { tags -> matches(tags, categoryId) }
    }
}

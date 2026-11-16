package com.sleepsounds.storytelling.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests verifying category filtering in [HomeBrowseCategories].
 */
class HomeBrowseCategoriesTest {

    @Test
    fun idsWithItemsRetainsOnlyPopulatedCategoriesInCatalogOrder() {
        val populated = HomeBrowseCategories.idsWithItems(
            categoryIds = listOf("animals", "sleep", "city", "white_noise", "rain"),
            itemCategories = listOf(
                listOf("SLEEP", "FOCUS"),
                listOf("RAIN", "NATURE"),
            ),
        )
        assertEquals(listOf("sleep", "rain"), populated)
    }

    @Test
    fun idsWithItemsPerformsCaseInsensitiveMatching() {
        val populated = HomeBrowseCategories.idsWithItems(
            categoryIds = listOf("Meditate"),
            itemCategories = listOf(listOf("MEDITATE")),
        )
        assertEquals(listOf("Meditate"), populated)
    }
}

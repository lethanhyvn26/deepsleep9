package com.sleepsounds.storytelling.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Unit tests verifying Firebase store node mapping and session randomization in [ShopCatalogMapper].
 */
class ShopCatalogMapperTest {

    @Test
    fun mapperFiltersDisabledCategoriesAndKeepsFirebaseOrder() {
        val mappedStore = ShopCatalogMapper.map(
            enabled = true,
            categoryChildren = mapOf(
                "b" to mapOf("label" to "Beta", "enabled" to true),
                "a" to mapOf("label" to "Alpha", "enabled" to true),
                "hidden" to mapOf("label" to "Hidden", "enabled" to false),
            ),
            productChildren = mapOf(
                "p2" to mapOf("category_id" to "b", "name" to "Second", "link" to "https://x", "enabled" to true),
                "p1" to mapOf("category_id" to "a", "name" to "First", "link" to "https://x", "enabled" to true),
                "off" to mapOf("category_id" to "a", "name" to "Off", "link" to "https://x", "enabled" to false),
                "orphan" to mapOf("category_id" to "hidden", "name" to "Orphan", "link" to "https://x", "enabled" to true),
            ),
        )

        assertTrue(mappedStore.isTabVisible)
        assertEquals(listOf("b", "a"), mappedStore.visibleCategories().map { it.id })
        assertEquals(listOf("p2", "p1"), mappedStore.enabledProducts.map { it.id })
    }

    @Test
    fun randomizedForSessionKeepsAllTabPinnedFirstAndPermutesProducts() {
        val cats = listOf(
            ShopCategory(id = "all", label = "All", enabled = true),
            ShopCategory(id = "c1", label = "Cat 1", enabled = true),
            ShopCategory(id = "c2", label = "Cat 2", enabled = true),
            ShopCategory(id = "c3", label = "Cat 3", enabled = true),
            ShopCategory(id = "c4", label = "Cat 4", enabled = true),
            ShopCategory(id = "c5", label = "Cat 5", enabled = true),
        )
        val items = (1..12).map { index ->
            ShopProduct(
                id = "p$index",
                categoryId = "c${((index - 1) % 5) + 1}",
                name = "Product $index",
                shortDescription = "",
                imageUrl = "",
                link = "https://amazon.com/dp/$index",
                enabled = true,
            )
        }
        val baseCatalog = ShopCatalog(enabled = true, categories = cats, products = items)

        val shuffleA = baseCatalog.randomizedForSession(Random(42L))
        val shuffleB = baseCatalog.randomizedForSession(Random(999L))

        assertEquals("all", shuffleA.categories.first().id)
        assertEquals("all", shuffleB.categories.first().id)
        assertNotEquals(
            shuffleA.enabledProducts.map { it.id },
            shuffleB.enabledProducts.map { it.id },
        )
    }

    @Test
    fun disabledStoreMasterFlagHidesShopTab() {
        val mappedStore = ShopCatalogMapper.map(
            enabled = false,
            categoryChildren = mapOf("a" to mapOf("label" to "A")),
            productChildren = mapOf(
                "p" to mapOf("category_id" to "a", "name" to "N", "link" to "https://x"),
            ),
            baseUrl = "https://pub-new.r2.dev/",
        )
        assertFalse(mappedStore.isTabVisible)
        assertTrue(mappedStore.products.isEmpty())
        assertEquals("https://pub-new.r2.dev/", mappedStore.baseUrl)
    }
}

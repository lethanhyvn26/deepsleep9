package com.sleepsounds.storytelling.data.remote

import kotlin.random.Random

data class ShopCatalog(
    val enabled: Boolean = false,
    val baseUrl: String = "",
    val categories: List<ShopCategory> = emptyList(),
    val products: List<ShopProduct> = emptyList(),
) {
    val enabledCategories: List<ShopCategory>
        get() = categories.filter { it.enabled }

    val enabledProducts: List<ShopProduct>
        get() {
            val activeCategoryKeys = enabledCategories.map { it.id.lowercase() }.toSet()
            return products.filter { it.enabled && it.categoryId.lowercase() in activeCategoryKeys }
        }

    val isTabVisible: Boolean
        get() = enabled && enabledProducts.isNotEmpty()

    fun visibleCategories(): List<ShopCategory> {
        val populatedCategoryKeys = enabledProducts.map { it.categoryId.lowercase() }.toSet()
        return enabledCategories.filter { it.id.lowercase() in populatedCategoryKeys }
    }

    fun randomizedForSession(random: Random = Random.Default): ShopCatalog {
        if (categories.isEmpty() && products.isEmpty()) return this
        val (allTabs, dynamicTabs) = categories.partition { it.isAllCategory }
        return copy(
            categories = allTabs + dynamicTabs.shuffled(random),
            products = products.shuffled(random),
        )
    }
}
data class ShopProduct(
    val id: String,
    val categoryId: String,
    val name: String,
    val shortDescription: String,
    val imageUrl: String,
    val link: String,
    val enabled: Boolean,
    val price: String = "",
    val compareAtPrice: String = "",
)
/**
 * Domain models representing the `/store` product catalog in Firebase Realtime Database.
 */
data class ShopCategory(
    val id: String,
    val label: String,
    val enabled: Boolean = true,
) {
    val isAllCategory: Boolean
        get() = id.equals("all", ignoreCase = true) || label.equals("all", ignoreCase = true)
}

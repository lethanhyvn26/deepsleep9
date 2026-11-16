package com.sleepsounds.storytelling.data.remote

import com.google.firebase.database.DataSnapshot

private fun Any?.asBoolOrDefault(default: Boolean): Boolean = when (this) {
    is Boolean -> this
    is Number -> toInt() != 0
    is String -> when (trim().lowercase()) {
        "true", "1", "yes" -> true
        "false", "0", "no" -> false
        else -> default
    }
    else -> default
}
private fun Any?.asCleanString(): String = when (this) {
    null -> ""
    is String -> trim()
    else -> toString().trim()
}
private fun DataSnapshot.asChildMap(): Map<String, Any?> = buildMap {
    if (!hasChildren()) return@buildMap
    for (prop in children) {
        val propName = prop.key ?: continue
        put(propName, prop.value)
    }
}
private fun DataSnapshot.orderedChildEntries(): List<Pair<String, Map<String, Any?>>> = buildList {
    for (node in children) {
        val nodeKey = node.key?.trim().orEmpty()
        if (nodeKey.isNotEmpty()) {
            add(nodeKey to node.asChildMap())
        }
    }
}
/**
 * Maps the Firebase Realtime Database `/store` snapshot into [ShopCatalog] domain models.
 */
object ShopCatalogMapper {

    fun fromSnapshot(snapshot: DataSnapshot): ShopCatalog {
        if (!snapshot.exists()) return ShopCatalog(enabled = false)
        val cdnBaseUrl = snapshot.child(KEY_BASE_URL).value.asCleanString()
        val storeEnabled = snapshot.child(KEY_ENABLED).value.asBoolOrDefault(default = true)
        if (!storeEnabled) return ShopCatalog(enabled = false, baseUrl = cdnBaseUrl)

        return map(
            enabled = true,
            categoryChildren = snapshot.child(KEY_CATEGORIES).orderedChildEntries(),
            productChildren = snapshot.child(KEY_PRODUCTS).orderedChildEntries(),
            baseUrl = cdnBaseUrl,
        )
    }

    fun map(
        enabled: Boolean,
        categoryChildren: List<Pair<String, Map<String, Any?>>>,
        productChildren: List<Pair<String, Map<String, Any?>>>,
        baseUrl: String = "",
    ): ShopCatalog {
        if (!enabled) return ShopCatalog(enabled = false, baseUrl = baseUrl)
        val parsedCategories = categoryChildren.mapNotNull { (catId, rawMap) ->
            buildCategory(catId, rawMap)
        }
        val parsedProducts = productChildren.mapNotNull { (prodId, rawMap) ->
            buildProduct(prodId, rawMap, parsedCategories)
        }
        return ShopCatalog(
            enabled = true,
            baseUrl = baseUrl,
            categories = parsedCategories,
            products = parsedProducts,
        )
    }

    fun map(
        enabled: Boolean,
        categoryChildren: Map<String, Map<String, Any?>>,
        productChildren: Map<String, Map<String, Any?>>,
        baseUrl: String = "",
    ): ShopCatalog = map(
        enabled = enabled,
        categoryChildren = categoryChildren.entries.map { it.key to it.value },
        productChildren = productChildren.entries.map { it.key to it.value },
        baseUrl = baseUrl,
    )

    private fun buildCategory(
        rawId: String,
        fields: Map<String, Any?>,
    ): ShopCategory? {
        val cleanId = rawId.trim()
        if (cleanId.isEmpty()) return null
        val displayLabel = fields[KEY_LABEL].asCleanString().ifBlank { cleanId }
        return ShopCategory(
            id = cleanId,
            label = displayLabel,
            enabled = fields[KEY_ENABLED].asBoolOrDefault(default = true),
        )
    }

    private fun buildProduct(
        rawId: String,
        fields: Map<String, Any?>,
        knownCategories: List<ShopCategory>,
    ): ShopProduct? {
        val cleanId = rawId.trim()
        val title = fields[KEY_NAME].asCleanString()
        val externalLink = fields[KEY_LINK].asCleanString()
        if (cleanId.isEmpty() || title.isEmpty() || externalLink.isEmpty()) return null

        val rawCatId = fields[KEY_CATEGORY_ID].asCleanString()
        val rawCatLabel = fields[KEY_CATEGORY].asCleanString()

        return ShopProduct(
            id = cleanId,
            categoryId = matchCategoryId(rawCatId, rawCatLabel, knownCategories),
            name = title,
            shortDescription = fields[KEY_SHORT_DESCRIPTION].asCleanString(),
            imageUrl = fields[KEY_IMAGE_URL].asCleanString(),
            link = externalLink,
            enabled = fields[KEY_ENABLED].asBoolOrDefault(default = true),
            price = fields[KEY_PRICE].asCleanString(),
            compareAtPrice = fields[KEY_COMPARE_AT_PRICE].asCleanString(),
        )
    }

    private fun matchCategoryId(
        categoryId: String,
        categoryLabel: String,
        categories: List<ShopCategory>,
    ): String {
        if (categoryId.isNotBlank()) {
            return categories.firstOrNull { it.id.equals(categoryId, ignoreCase = true) }?.id
                ?: categoryId
        }
        if (categoryLabel.isBlank()) return ""
        categories.firstOrNull { it.id.equals(categoryLabel, ignoreCase = true) }?.let { return it.id }
        categories.firstOrNull { it.label.equals(categoryLabel, ignoreCase = true) }?.let { return it.id }
        return categoryLabel
    }

    const val KEY_BASE_URL = "base_url"
    const val KEY_ENABLED = "enabled"
    const val KEY_CATEGORIES = "categories"
    const val KEY_PRODUCTS = "products"
    const val KEY_LABEL = "label"
    const val KEY_NAME = "name"
    const val KEY_LINK = "link"
    const val KEY_CATEGORY_ID = "category_id"
    const val KEY_CATEGORY = "category"
    const val KEY_SHORT_DESCRIPTION = "short_description"
    const val KEY_IMAGE_URL = "image_url"
    const val KEY_PRICE = "price"
    const val KEY_COMPARE_AT_PRICE = "compare_at_price"
}

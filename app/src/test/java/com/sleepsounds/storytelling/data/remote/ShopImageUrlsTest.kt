package com.sleepsounds.storytelling.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests verifying shop product image URL resolution across Firebase RTDB and Cloudflare R2/Worker.
 */
class ShopImageUrlsTest {

    private val primaryR2Root = "https://pub-new.r2.dev/"
    private val fallbackR2Root = "https://pub-old.r2.dev/"

    @Test
    fun relativeImagePathResolvesAgainstCatalogBaseUrl() {
        val actual = ShopImageUrls.resolve(
            raw = "products/melatonin_gummies.jpg",
            rtdbBase = primaryR2Root,
            rcBase = fallbackR2Root,
        )
        assertEquals("https://pub-new.r2.dev/products/melatonin_gummies.jpg", actual)
    }

    @Test
    fun relativeImagePathUsesRemoteConfigFallbackWhenCatalogBaseBlank() {
        val actual = ShopImageUrls.resolve(
            raw = "products/mask.jpg",
            rtdbBase = "",
            rcBase = fallbackR2Root,
        )
        assertEquals("https://pub-old.r2.dev/products/mask.jpg", actual)
    }

    @Test
    fun fullHttpsImageUrlIsPreservedVerbatim() {
        val directHttps = "https://images.unsplash.com/photo-x?w=200"
        assertEquals(directHttps, ShopImageUrls.resolve(directHttps, primaryR2Root, fallbackR2Root))
    }

    @Test
    fun baseUrlWithoutTrailingSlashIsNormalizedAutomatically() {
        val actual = ShopImageUrls.resolve(
            raw = "products/a.jpg",
            rtdbBase = "https://pub-new.r2.dev",
            rcBase = fallbackR2Root,
        )
        assertEquals("https://pub-new.r2.dev/products/a.jpg", actual)
    }

    @Test
    fun workerRoutingAddsFilesSegmentWhenCatalogBaseIsEmpty() {
        val actual = ShopImageUrls.resolve(
            raw = "products/mask.jpg",
            rtdbBase = "",
            rcBase = "https://deepsleep-media.example.workers.dev/",
            useWorkerRoutes = true,
        )
        assertEquals(
            "https://deepsleep-media.example.workers.dev/files/products/mask.jpg",
            actual,
        )
    }

    @Test
    fun resolveCatalogUpdatesRelativeProductImagesAndLeavesAbsoluteOnesIntact() {
        val rawCatalog = ShopCatalog(
            enabled = true,
            baseUrl = primaryR2Root,
            products = listOf(
                buildTestProduct("rel", "products/a.jpg"),
                buildTestProduct("abs", "https://images.unsplash.com/x"),
            ),
        )
        val updatedCatalog = ShopImageUrls.resolveCatalog(rawCatalog, fallbackR2Root)
        assertEquals("https://pub-new.r2.dev/products/a.jpg", updatedCatalog.products[0].imageUrl)
        assertEquals("https://images.unsplash.com/x", updatedCatalog.products[1].imageUrl)
    }

    private fun buildTestProduct(productId: String, rawImage: String): ShopProduct = ShopProduct(
        id = productId,
        categoryId = "sleep_accessories",
        name = productId,
        shortDescription = "",
        imageUrl = rawImage,
        link = "https://example.com/$productId",
        enabled = true,
    )
}

package com.sleepsounds.storytelling.data.remote

/**
 * Resolves relative product image paths in [ShopCatalog] to full Cloudflare R2 / CDN URLs.
 */
object ShopImageUrls {

    fun resolve(
        raw: String,
        rtdbBase: String,
        rcBase: String,
        useWorkerRoutes: Boolean = false,
    ): String = CdnUrlResolver.resolve(
        pathOrUrl = raw,
        rtdbBase = rtdbBase,
        rcBase = rcBase,
        useWorkerRoutes = useWorkerRoutes,
    )

    fun resolveCatalog(
        catalog: ShopCatalog,
        rcBase: String,
        useWorkerRoutes: Boolean = false,
    ): ShopCatalog {
        if (catalog.products.isEmpty()) return catalog
        val updatedProducts = catalog.products.map { item ->
            item.copy(
                imageUrl = resolve(
                    raw = item.imageUrl,
                    rtdbBase = catalog.baseUrl,
                    rcBase = rcBase,
                    useWorkerRoutes = useWorkerRoutes,
                ),
            )
        }
        return catalog.copy(products = updatedProducts)
    }

    fun normalizeBase(url: String): String = CdnUrlResolver.normalizeBase(url)
}

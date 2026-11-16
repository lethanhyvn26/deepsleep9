package com.sleepsounds.storytelling.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests verifying Cloudflare R2 and Worker route resolution in [CdnUrlResolver].
 */
class CdnUrlResolverTest {

    private val realtimeDbRoot = "https://pub-new.r2.dev/"
    private val remoteConfigR2Root = "https://pub-old.r2.dev/"
    private val workerGatewayRoot = "https://deepsleep-media.example.workers.dev/"

    @Test
    fun realtimeDbBaseUrlTakesPrecedenceOverWorkerMode() {
        val resolved = CdnUrlResolver.resolve(
            pathOrUrl = "sounds/rain/audio.ogg",
            rtdbBase = realtimeDbRoot,
            rcBase = workerGatewayRoot,
            useWorkerRoutes = true,
        )
        assertEquals("https://pub-new.r2.dev/sounds/rain/audio.ogg", resolved)
    }

    @Test
    fun blankRealtimeDbBaseUrlWithWorkerEnabledPrependsFilesPrefix() {
        val resolved = CdnUrlResolver.resolve(
            pathOrUrl = "sounds/rain/audio.ogg",
            rtdbBase = "",
            rcBase = workerGatewayRoot,
            useWorkerRoutes = true,
        )
        assertEquals(
            "https://deepsleep-media.example.workers.dev/files/sounds/rain/audio.ogg",
            resolved,
        )
    }

    @Test
    fun blankRealtimeDbBaseUrlWithWorkerDisabledUsesDirectR2Url() {
        val resolved = CdnUrlResolver.resolve(
            pathOrUrl = "products/mask.jpg",
            rtdbBase = "",
            rcBase = remoteConfigR2Root,
            useWorkerRoutes = false,
        )
        assertEquals("https://pub-old.r2.dev/products/mask.jpg", resolved)
    }

    @Test
    fun absoluteHttpsUrlIsReturnedWithoutModification() {
        val externalUrl = "https://images.unsplash.com/x"
        val resolved = CdnUrlResolver.resolve(
            pathOrUrl = externalUrl,
            rtdbBase = realtimeDbRoot,
            rcBase = workerGatewayRoot,
            useWorkerRoutes = true,
        )
        assertEquals(externalUrl, resolved)
    }
}

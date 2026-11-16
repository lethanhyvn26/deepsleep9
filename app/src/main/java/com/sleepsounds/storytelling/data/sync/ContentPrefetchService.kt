package com.sleepsounds.storytelling.data.sync

import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import com.sleepsounds.storytelling.data.repository.ContentRepository
import com.sleepsounds.storytelling.di.ApplicationScope
import com.sleepsounds.storytelling.util.MediaAssetResolver
import com.sleepsounds.storytelling.util.R2Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Asynchronously warms the Coil disk cache with scene, sound, and podcard thumbnails.
 */
@Singleton
class ContentPrefetchService @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val contentRepository: ContentRepository,
    private val mediaAssetResolver: MediaAssetResolver,
    @ApplicationScope private val appScope: CoroutineScope,
) {
    @Volatile
    private var hasScheduledPrefetch = false

    fun prefetchListMetadata(presetLimit: Int = 20, soundLimit: Int = 24) {
        if (hasScheduledPrefetch) return
        hasScheduledPrefetch = true
        appScope.launch {
            runCatching {
                val loader = SingletonImageLoader.get(appContext)
                val sceneCoverUrls = contentRepository.observeSleepPresetTracks().first()
                    .take(presetLimit)
                    .mapNotNull { it.imagePath?.takeIf(::isHttpUri) }
                val soundIconUrls = contentRepository.observeSounds().first()
                    .take(soundLimit)
                    .mapNotNull { track ->
                        mediaAssetResolver.resolveSoundSymbolUri(track.id, track.imagePath)
                            ?.takeIf(::isHttpUri)
                    }
                val storyCoverUrls = contentRepository.observePodcasts().first()
                    .take(12)
                    .mapNotNull { it.imagePath?.takeIf(::isHttpUri) }

                val batchUrls = (sceneCoverUrls + soundIconUrls + storyCoverUrls).distinct()
                batchUrls.forEachIndexed { idx, remoteUrl ->
                    enqueueThumbnailRequest(loader, remoteUrl)
                    if (idx % 4 == 3) delay(80L)
                }
            }.onFailure { err ->
                R2Log.w("Thumbnail prefetch skipped: ${err.message}")
            }
        }
    }

    private fun enqueueThumbnailRequest(loader: ImageLoader, url: String) {
        val req = ImageRequest.Builder(appContext)
            .data(url)
            .size(THUMBNAIL_PREFETCH_SIZE_PX)
            .build()
        loader.enqueue(req)
    }

    private fun isHttpUri(candidate: String): Boolean =
        candidate.startsWith("http://") || candidate.startsWith("https://")

    private companion object {
        const val THUMBNAIL_PREFETCH_SIZE_PX = 320
    }
}

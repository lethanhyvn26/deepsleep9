package com.sleepsounds.storytelling.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads and caches the bundled high-definition notification artwork for MediaSession notifications.
 */
@Singleton
class NotificationArtworkLoader @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    @Volatile
    private var inMemoryArtworkBitmap: Bitmap? = null

    @Volatile
    private var inMemoryArtworkBytes: ByteArray? = null

    fun getSharedHdBitmapSync(): Bitmap? {
        val current = inMemoryArtworkBitmap
        if (current != null && !current.isRecycled) return current
        return synchronized(this) {
            val rechecked = inMemoryArtworkBitmap
            if (rechecked != null && !rechecked.isRecycled) {
                rechecked
            } else {
                decodeSharedHdAsset(appContext)?.also { inMemoryArtworkBitmap = it }
            }
        }
    }

    fun getSharedHdBytesSync(): ByteArray? {
        val current = inMemoryArtworkBytes
        if (current != null) return current
        return synchronized(this) {
            inMemoryArtworkBytes ?: runCatching {
                appContext.assets.open(SHARED_HD_NOTIFICATION_ASSET).use { it.readBytes() }
            }.getOrNull()?.also { inMemoryArtworkBytes = it }
        }
    }

    suspend fun loadForPlayback(): Bitmap? = withContext(Dispatchers.IO) {
        getSharedHdBitmapSync()
    }

    fun clearCache() = Unit

    companion object {
        const val SHARED_HD_NOTIFICATION_ASSET = "images/notification/notif_hd_cover.jpg"

        fun decodeSharedHdAsset(context: Context): Bitmap? = runCatching {
            val decodeOptions = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inScaled = false
            }
            context.assets.open(SHARED_HD_NOTIFICATION_ASSET).use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            }
        }.getOrNull()
    }
}

package com.sleepsounds.storytelling.di

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import coil3.EventListener
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.allowRgb565
import coil3.request.crossfade
import coil3.request.maxBitmapSize
import coil3.size.Precision
import coil3.size.Size
import com.sleepsounds.storytelling.util.R2Log
import okhttp3.OkHttpClient
import okio.Path.Companion.toPath
import java.util.concurrent.TimeUnit

/**
 * Configures the singleton Coil 3 [ImageLoader] with memory-aware cache limits and OkHttp transport.
 */
object CoilImageLoaderFactory : SingletonImageLoader.Factory {

    private const val STANDARD_DISK_CACHE_BYTES = 200L * 1024L * 1024L
    private const val LOW_RAM_DISK_CACHE_BYTES = 96L * 1024L * 1024L

    override fun newImageLoader(context: Context): ImageLoader {
        val appCtx = context.applicationContext
        val activityMgr = appCtx.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val lowRamDevice = activityMgr?.isLowRamDevice == true

        val maxBitmapDimension = if (lowRamDevice) 1280 else 2048
        val ramCacheFraction = if (lowRamDevice) 0.14 else 0.20
        val diskQuotaBytes = if (lowRamDevice) LOW_RAM_DISK_CACHE_BYTES else STANDARD_DISK_CACHE_BYTES

        val imageHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        return ImageLoader.Builder(appCtx)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { imageHttpClient }))
            }
            .maxBitmapSize(Size(maxBitmapDimension, maxBitmapDimension))
            .precision(Precision.INEXACT)
            .crossfade(150)
            .allowRgb565(true)
            .allowHardware(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(appCtx, percent = ramCacheFraction)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(appCtx.cacheDir.resolve("image_cache").absolutePath.toPath())
                    .maxSizeBytes(diskQuotaBytes)
                    .build()
            }
            .eventListener(
                object : EventListener() {
                    override fun onError(request: ImageRequest, result: ErrorResult) {
                        R2Log.w("Image load failed for ${request.data}: ${result.throwable.message}")
                    }
                },
            )
            .build()
    }
}

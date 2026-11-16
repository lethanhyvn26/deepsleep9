@file:OptIn(UnstableApi::class)

package com.sleepsounds.storytelling.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

/**
 * Media3 ExoPlayer caching and network data source bindings for streaming Cloudflare R2 audio.
 */
@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    private const val MAX_AUDIO_DISK_CACHE_BYTES = 150L * 1024L * 1024L
    private const val AUDIO_CACHE_SUBDIR = "audio_cache"
    private const val HTTP_TIMEOUT_MS = 15_000

    @Provides
    @Singleton
    fun provideMediaDatabaseProvider(
        @ApplicationContext appContext: Context,
    ): StandaloneDatabaseProvider = StandaloneDatabaseProvider(appContext)

    @Provides
    @Singleton
    fun provideAudioSimpleCache(
        @ApplicationContext appContext: Context,
        dbProvider: StandaloneDatabaseProvider,
    ): SimpleCache {
        val directory = File(appContext.cacheDir, AUDIO_CACHE_SUBDIR)
        val evictor = LeastRecentlyUsedCacheEvictor(MAX_AUDIO_DISK_CACHE_BYTES)
        return SimpleCache(directory, evictor, dbProvider)
    }

    @Provides
    @Singleton
    fun provideCachedDataSourceFactory(
        @ApplicationContext appContext: Context,
        diskCache: SimpleCache,
    ): CacheDataSource.Factory {
        val httpFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(HTTP_TIMEOUT_MS)
            .setReadTimeoutMs(HTTP_TIMEOUT_MS)
        val upstreamFactory = DefaultDataSource.Factory(appContext, httpFactory)

        return CacheDataSource.Factory()
            .setCache(diskCache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    @Provides
    @Singleton
    fun provideLocalDataSourceFactory(
        @ApplicationContext appContext: Context,
    ): DefaultDataSource.Factory = DefaultDataSource.Factory(appContext)
}

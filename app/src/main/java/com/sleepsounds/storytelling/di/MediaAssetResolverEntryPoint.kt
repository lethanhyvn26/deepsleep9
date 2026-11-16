package com.sleepsounds.storytelling.di

import com.sleepsounds.storytelling.util.MediaAssetResolver
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint allowing Compose UI components to obtain the singleton [MediaAssetResolver].
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface MediaAssetResolverEntryPoint {
    fun mediaAssetResolver(): MediaAssetResolver
}

package com.sleepsounds.storytelling.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.sleepsounds.storytelling.di.MediaAssetResolverEntryPoint
import com.sleepsounds.storytelling.util.MediaAssetResolver
import dagger.hilt.android.EntryPointAccessors

/**
 * Remembers the application-scoped [MediaAssetResolver] inside a Compose hierarchy.
 */
@Composable
fun rememberMediaAssetResolver(): MediaAssetResolver {
    val appCtx = LocalContext.current.applicationContext
    return remember(appCtx) {
        val entryPoint = EntryPointAccessors.fromApplication(
            appCtx,
            MediaAssetResolverEntryPoint::class.java,
        )
        entryPoint.mediaAssetResolver()
    }
}

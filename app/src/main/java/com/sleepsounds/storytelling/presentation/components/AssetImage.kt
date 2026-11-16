package com.sleepsounds.storytelling.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Precision
import com.sleepsounds.storytelling.ui.theme.PastelPurple
import com.sleepsounds.storytelling.ui.theme.SurfaceBlueLight

@Composable
private fun ArtworkFallbackBox(
    modifier: Modifier = Modifier,
    displayMusicGlyph: Boolean = false,
) {
    Box(
        modifier = modifier.background(SurfaceBlueLight),
        contentAlignment = Alignment.Center,
    ) {
        if (displayMusicGlyph) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = PastelPurple.copy(alpha = 0.6f),
                modifier = Modifier.size(28.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(PastelPurple.copy(alpha = 0.15f), MaterialTheme.shapes.small),
            )
        }
    }
}
/**
 * Renders local asset or Cloudflare R2 remote images with disk/memory caching and fallback placeholders.
 */
@Composable
fun AssetImage(
    assetUri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
) {
    if (assetUri.isBlank()) {
        ArtworkFallbackBox(modifier = modifier, displayMusicGlyph = true)
        return
    }
    val context = LocalContext.current
    val imageRequest = remember(assetUri, context) {
        ImageRequest.Builder(context)
            .data(assetUri)
            .crossfade(false)
            .precision(Precision.INEXACT)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }
    SubcomposeAsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        colorFilter = colorFilter,
        loading = {
            ArtworkFallbackBox(modifier = Modifier.matchParentSize(), displayMusicGlyph = false)
        },
        error = {
            ArtworkFallbackBox(modifier = Modifier.matchParentSize(), displayMusicGlyph = true)
        },
    )
}

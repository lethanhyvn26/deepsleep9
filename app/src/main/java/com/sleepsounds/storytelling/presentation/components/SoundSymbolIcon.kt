package com.sleepsounds.storytelling.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale

/**
 * Renders the vector/WebP symbol icon for a sound track, falling back to an equalizer icon.
 */
@Composable
fun SoundSymbolIcon(
    soundId: String,
    symbolPath: String?,
    contentDescription: String?,
    tint: Color = Color.White,
    modifier: Modifier = Modifier,
) {
    val resolver = rememberMediaAssetResolver()
    val resolvedSymbolUri = remember(soundId, symbolPath) {
        val explicit = symbolPath?.takeIf { it.isNotBlank() }
        explicit ?: resolver.resolveSoundSymbolUri(soundId, null)
    }

    if (resolvedSymbolUri != null) {
        AssetImage(
            assetUri = resolvedSymbolUri,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(tint),
        )
    } else {
        Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = contentDescription,
            tint = tint,
            modifier = modifier,
        )
    }
}

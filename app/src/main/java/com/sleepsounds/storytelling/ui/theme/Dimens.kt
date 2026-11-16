package com.sleepsounds.storytelling.ui.theme

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

object Dimens {
    val screenPadding: Dp = 20.dp
    val cardRadius: Dp = 20.dp
    val tileRadius: Dp = 16.dp
    val chipRadius: Dp = 24.dp
    val gridSpacing: Dp = 12.dp
    val bottomBarHeight: Dp = 72.dp
    val bottomNavItemMinSize: Dp = 56.dp
    val miniPlayerHeight: Dp = 64.dp
    const val glassAlpha: Float = 0.4f
    const val glassAlphaStrong: Float = 0.55f
}
fun Modifier.responsiveContentWidth(maxWidth: Dp = 680.dp): Modifier =
    widthIn(max = maxWidth).fillMaxWidth()
@Composable
fun rememberResponsiveLayoutInfo(): ResponsiveLayoutInfo {
    val containerSize = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current.density.coerceAtLeast(1f)
    val wDp = (containerSize.width / density).roundToInt().coerceAtLeast(320)
    val hDp = (containerSize.height / density).roundToInt().coerceAtLeast(480)

    return remember(wDp, hDp) {
        val ultraCompactW = wDp < 360
        val compactW = wDp < 600
        val mediumW = wDp in 600..839
        val expandedW = wDp >= 840
        val compactH = hDp < 560
        val landscapeMode = wDp > hDp

        val horizontalPad = when {
            ultraCompactW -> 14.dp
            compactW -> 20.dp
            mediumW -> 28.dp
            else -> 36.dp
        }

        val contentCapWidth = when {
            expandedW -> 1120.dp
            mediumW -> 840.dp
            else -> Dp.Unspecified
        }

        ResponsiveLayoutInfo(
            screenWidthDp = wDp,
            screenHeightDp = hDp,
            isUltraCompactWidth = ultraCompactW,
            isCompactWidth = compactW,
            isMediumWidth = mediumW,
            isExpandedWidth = expandedW,
            isCompactHeight = compactH,
            isLandscape = landscapeMode,
            screenHorizontalPadding = horizontalPad,
            maxContentWidth = contentCapWidth,
            cardGridColumns = when {
                wDp >= 960 -> 4
                wDp >= 600 -> 3
                else -> 2
            },
            soundGridColumns = when {
                wDp >= 960 -> 6
                wDp >= 720 -> 5
                wDp < 320 -> 3
                else -> 4
            },
            productGridColumns = when {
                wDp >= 900 -> 3
                wDp >= 600 -> 2
                else -> 1
            },
            maxSoundTileSize = when {
                ultraCompactW -> 82.dp
                compactW -> 92.dp
                mediumW -> 104.dp
                else -> 112.dp
            },
            mixerColumns = when {
                wDp >= 840 -> 5
                wDp >= 580 -> 4
                else -> 3
            },
            mixerRows = if (hDp < 480) 2 else 3,
        )
    }
}
/**
 * Adaptive screen metrics and grid column counts computed from current window dimensions.
 */
@Immutable
data class ResponsiveLayoutInfo(
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val isUltraCompactWidth: Boolean,
    val isCompactWidth: Boolean,
    val isMediumWidth: Boolean,
    val isExpandedWidth: Boolean,
    val isCompactHeight: Boolean,
    val isLandscape: Boolean,
    val screenHorizontalPadding: Dp,
    val maxContentWidth: Dp,
    val cardGridColumns: Int,
    val soundGridColumns: Int,
    val productGridColumns: Int,
    val maxSoundTileSize: Dp,
    val mixerColumns: Int,
    val mixerRows: Int,
) {
    val isTabletOrFoldable: Boolean
        get() = isMediumWidth || isExpandedWidth

    val mixerSoundsPerPage: Int
        get() = mixerColumns * mixerRows
}

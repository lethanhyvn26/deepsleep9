package com.sleepsounds.storytelling.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NightSleepColorPalette = darkColorScheme(
    primary = DeepSleepBlue,
    onPrimary = Color.White,
    primaryContainer = SurfaceBlueLight,
    onPrimaryContainer = TextPrimary,
    secondary = AccentTeal,
    onSecondary = MidnightBlue,
    secondaryContainer = SurfaceBlueLight,
    onSecondaryContainer = TextPrimary,
    tertiary = SunrisePink,
    background = MidnightBlue,
    onBackground = TextPrimary,
    surface = SurfaceBlue,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceBlueLight,
    onSurfaceVariant = TextSecondary,
    outline = TextSecondary.copy(alpha = 0.25f),
)

/**
 * Root Material3 dark theme for Deep Sleep with edge-to-edge transparent system bars.
 */
@Composable
fun DeepSleepTheme(
    content: @Composable () -> Unit,
) {
    ImmersiveSystemBars()

    MaterialTheme(
        colorScheme = NightSleepColorPalette,
        typography = Typography,
        shapes = DeepSleepShapes,
        content = content,
    )
}

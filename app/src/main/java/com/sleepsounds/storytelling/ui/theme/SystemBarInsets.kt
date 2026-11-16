package com.sleepsounds.storytelling.ui.theme

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ColumnScope.BottomNavigationBarInsetSpacer(modifier: Modifier = Modifier) {
    Spacer(modifier = modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
}
@Composable
fun Modifier.contentSafePadding(area: ContentSafeArea): Modifier = when (area) {
    ContentSafeArea.None -> this
    ContentSafeArea.TopAndSides -> statusAndCutoutPadding()
    ContentSafeArea.SystemBars -> systemSafePadding()
}
@Composable
fun Modifier.statusAndCutoutPadding(): Modifier {
    val statusAndCutoutInsets = WindowInsets.statusBars
        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    return windowInsetsPadding(statusAndCutoutInsets)
}
@Composable
fun Modifier.systemSafePadding(): Modifier {
    val topAndHorizontalInsets = WindowInsets.systemBars
        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    return windowInsetsPadding(topAndHorizontalInsets).navigationBarsPadding()
}
/**
 * Safe-area inset modes for full-bleed background screens.
 */
enum class ContentSafeArea {
    None,
    TopAndSides,
    SystemBars,
}

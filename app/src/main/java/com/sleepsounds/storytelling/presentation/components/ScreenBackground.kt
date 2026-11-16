package com.sleepsounds.storytelling.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sleepsounds.storytelling.ui.theme.ContentSafeArea
import com.sleepsounds.storytelling.ui.theme.MidnightBlue
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.contentSafePadding
import com.sleepsounds.storytelling.ui.theme.rememberResponsiveLayoutInfo

private val DeepNightCanvasBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF080C1C),
        MidnightBlue,
        Color(0xFF070A16),
    ),
)
@Composable
fun TabScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    val responsive = rememberResponsiveLayoutInfo()
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = TextPrimary,
        modifier = modifier.padding(
            start = responsive.screenHorizontalPadding,
            end = responsive.screenHorizontalPadding,
            top = 12.dp,
            bottom = 8.dp,
        ),
    )
}
/**
 * Standard deep-night gradient backdrop with configurable system safe-area insets.
 */
@Composable
fun ScreenBackground(
    modifier: Modifier = Modifier,
    contentSafeArea: ContentSafeArea = ContentSafeArea.TopAndSides,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNightCanvasBrush),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .contentSafePadding(contentSafeArea),
        ) {
            content()
        }
    }
}

package com.sleepsounds.storytelling.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.sleepsounds.storytelling.R

/**
 * Bundled Euclid Circular font family used across all Deep Sleep screens.
 */
val AppFontFamily: FontFamily = FontFamily(
    Font(resId = R.font.euclid_light, weight = FontWeight.Light),
    Font(resId = R.font.euclid_regular, weight = FontWeight.Normal),
    Font(resId = R.font.euclid_medium, weight = FontWeight.Medium),
    Font(resId = R.font.euclid_semibold, weight = FontWeight.SemiBold),
    Font(resId = R.font.euclid_bold, weight = FontWeight.Bold),
)

private fun euclidStyle(
    weight: FontWeight,
    size: TextUnit,
    lineHeight: TextUnit = TextUnit.Unspecified,
): TextStyle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight,
)

val Typography: Typography = Typography(
    displayLarge = euclidStyle(FontWeight.Bold, 32.sp),
    headlineLarge = euclidStyle(FontWeight.Bold, 24.sp),
    headlineMedium = euclidStyle(FontWeight.SemiBold, 20.sp),
    titleLarge = euclidStyle(FontWeight.SemiBold, 18.sp),
    titleMedium = euclidStyle(FontWeight.Medium, 16.sp),
    bodyLarge = euclidStyle(FontWeight.Normal, 16.sp, lineHeight = 24.sp),
    bodyMedium = euclidStyle(FontWeight.Normal, 14.sp, lineHeight = 20.sp),
    labelLarge = euclidStyle(FontWeight.Medium, 14.sp),
    labelMedium = euclidStyle(FontWeight.Medium, 12.sp),
)

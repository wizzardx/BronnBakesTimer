package com.example.bronnbakestimer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with

/**
 * Defines the typography styles for the BronnBakesTimer application.
 *
 * This object `Typography` sets up a collection of text styles based on the Material Design guidelines.
 * It is used throughout the app to ensure consistent styling of text elements. The typography styles
 * defined here can be easily applied to text components across the app for uniformity and coherence in design.
 *
 * Currently, the `bodyLarge` style is defined, which is suitable for primary content text. It uses the default
 * font family, normal font weight, and sets specific values for font size, line height, and letter spacing.
 * These settings aim to enhance readability and provide a pleasant user experience.
 *
 * Additional text styles can be defined and customized as per the application's design requirements.
 *
 * Example styles commented out include `titleLarge` for larger title text, and `labelSmall` for smaller label text.
 * These can be uncommented and adjusted as needed.
 *
 * Usage:
 * Apply these styles in composables to maintain consistency in text appearance throughout the app.
 */
val Typography =
    Typography(
        bodyLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
     */
    )

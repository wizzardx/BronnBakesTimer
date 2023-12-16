package com.example.bronnbakestimer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme =
    darkColorScheme(
        primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40,
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
     */
    )

/**
 * Applies a theme for the BronnBakesTimer app, supporting both light and dark color schemes.
 *
 * The function `BronnBakesTimerTheme` sets the color scheme of the app based on the system settings
 * and Android version. It supports dynamic coloring on devices running Android 12 and above,
 * allowing the app to adapt its color scheme based on the current wallpaper. For devices running
 * older versions of Android, it defaults to a predefined light or dark color scheme.
 *
 * @param darkTheme A Boolean flag indicating if the dark theme should be applied.
 *                  It defaults to the system's dark theme setting.
 * @param dynamicColor A Boolean flag indicating if dynamic coloring is enabled.
 *                     It defaults to `true` and is only effective on Android 12+.
 * @param content A lambda expression representing the composable content that
 *                will be displayed within the themed layout.
 *
 * The function applies the appropriate color scheme to the status bar and adjusts
 * the status bar icons for better visibility against the background color. It also
 * sets the overall color scheme and typography for the app's UI components.
 */
@Composable
@Suppress("FunctionName")
fun BronnBakesTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> {
                DarkColorScheme
            }
            else -> {
                LightColorScheme
            }
        }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

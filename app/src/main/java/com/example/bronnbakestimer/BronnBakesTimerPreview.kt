package com.example.bronnbakestimer

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.bronnbakestimer.ui.theme.BronnBakesTimerTheme
import org.koin.core.context.startKoin

/**
 * Composable function for previewing the main user interface of the BronnBakesTimer app.
 *
 * This function displays a preview of the app's main user interface using the BronnBakesTimerTheme.
 * It is intended for use in development and testing to visualize how the UI components appear in a Compose preview.
 *
 * @see BronnBakesTimerTheme
 */
@Preview(showSystemUi = true)
@Composable
fun BronnBakesTimerPreview() {
    startKoin {
        modules(appModule)
    }

    BronnBakesTimerTheme {
        BronnBakesTimer()
    }
}

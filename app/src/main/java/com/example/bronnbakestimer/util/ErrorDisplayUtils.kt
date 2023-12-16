package com.example.bronnbakestimer.util

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

/**
 * Generates a Composable function for displaying an error message and a flag indicating if there is an error.
 *
 * This function takes an optional error message string as input and a UI creator interface.
 * If the error message is not null, it returns a pair consisting of a Composable function and a true flag.
 * The Composable function, when invoked, uses the provided UI creator interface to display the error
 * message in a predefined style (e.g., red color, bold font). If the error message is null, indicating no error,
 * it returns a pair with a null Composable function and a false flag.
 *
 * This approach allows for separation of the UI logic from the business logic, making the function
 * more testable and flexible in terms of UI rendering.
 *
 * @param error An optional error message string. If null, indicates no error.
 * @param uiCreator An instance of ErrorUiCreator used to create the UI element for displaying the error.
 * @return A Pair of a nullable Composable function and a boolean flag. The Composable function,
 *         when not null, can be used to display the error message. The boolean flag indicates
 *         the presence of an error.
 */
fun getErrorInfoFor(
    error: String?,
    uiCreator: ErrorUiCreator = defaultErrorUiCreator,
): Pair<(@Composable (() -> Unit))?, Boolean> {
    return error?.let {
        Pair(
            { uiCreator.Create(it) },
            true,
        )
    } ?: Pair(null, false)
}

// Default implementation of the UI creation, used in the actual app
// To manually check this, just get an error to show in the UI.
private val defaultErrorUiCreator =
    ErrorUiCreator { error ->
        // Regular unit tests can't get good coverage for this, so we'll just manually check it
        Text(
            text = error,
            color = Color.Red,
            fontWeight = FontWeight.Bold,
        )
    }

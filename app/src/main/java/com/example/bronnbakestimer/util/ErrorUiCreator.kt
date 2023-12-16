package com.example.bronnbakestimer.util

import androidx.compose.runtime.Composable

/**
 * Functional interface for creating UI elements to display errors.
 *
 * This interface provides a way to abstract the creation of UI components
 * that are used to display error messages. It allows for different
 * implementations of error message display, which can be useful for testing
 * or for changing the UI presentation in different contexts.
 *
 * The interface contains a single method `create` which takes an error
 * message as a string and returns a Composable function. This Composable
 * function, when invoked, displays the error message in the UI.
 *
 * Example Usage:
 * ```
 * val defaultErrorUiCreator = ErrorUiCreator { error ->
 *     Text(
 *         text = error,
 *         color = Color.Red,
 *         fontWeight = FontWeight.Bold
 *     )
 * }
 * ```
 */
fun interface ErrorUiCreator {
    /**
     * Creates a Composable UI element for displaying an error message.
     *
     * This method is responsible for defining how an error message should be displayed in the UI.
     * It is a part of the ErrorUiCreator functional interface and is intended to be implemented
     * with a Composable function that takes an error message as input and displays it according
     * to the desired UI specifications.
     *
     * The method takes a single String parameter, 'error', which is the error message to be displayed.
     * It returns a Composable function. When this function is invoked, it will render the error message
     * on the UI with the specified style and layout.
     *
     * Implementations of this method can vary based on the desired UI look and feel. For example, an
     * implementation might display the error message in red text, with a bold font weight, indicating
     * that it's an error.
     *
     * @param error The error message to be displayed in the UI. This should be a descriptive message
     *              that can be easily understood by the user.
     * @return A Composable function which, when invoked, displays the provided error message in the UI.
     */
    @Composable
    @Suppress("FunctionName")
    fun Create(error: String)
}

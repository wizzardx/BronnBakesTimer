package com.example.bronnbakestimer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import java.util.Locale

/**
 * Formats a total number of seconds into a minutes:seconds string.
 *
 * This function converts an integer representing a total duration in seconds
 * into a formatted string displaying minutes and remaining seconds. The format
 * used is MM:SS, where MM is minutes and SS is seconds, both padded with zeros
 * if necessary. This is useful for displaying time durations in a user-friendly
 * format.
 *
 * @param totalSeconds The total duration in seconds to be formatted.
 * @return A string formatted as MM:SS representing the input duration.
 */
fun formatMinSec(totalSeconds: Int): String {
    val minutes = totalSeconds / Constants.SecondsPerMinute
    val seconds = totalSeconds % Constants.SecondsPerMinute
    return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
}

/**
 * Normalizes a string input to remove non-numeric characters and leading zeros.
 *
 * This function processes a string input, typically representing a number, and
 * removes any non-digit characters. It also removes leading zeros while
 * preserving the numerical value of the string. If the string does not contain
 * any digits, or only contains zeros, the result is "0". This function is
 * useful for sanitizing and standardizing numerical user inputs, ensuring they
 * are in a format suitable for further processing or conversion to an integer.
 *
 * @param s The string input to be normalized.
 * @return A normalized string with non-numeric characters removed and leading zeros stripped.
 */
fun normaliseIntInput(s: String): String {
    // Go through the string, and remove any non-numeric characters.
    // Don't allow 0 at the start of the number. But if there is no number at the
    // end, then our result is 0
    var result = ""
    var foundNonZero = false
    for (c in s) {
        if (c.isDigit()) {
            if (c != '0') {
                foundNonZero = true
            }
            if (foundNonZero) {
                result += c
            }
        }
    }
    return result
}

/**
 * Validates a string input to ensure it represents a valid integer within specified bounds.
 *
 * This function checks if the input string can be converted to an integer and
 * whether it falls within the predefined bounds of 1 and `Constants.MaxUserInputNum`
 * (inclusive). It is primarily used to validate user inputs for settings like
 * cycle duration, work intervals, or rest intervals in the application. If the
 * input is not a valid integer or falls outside the acceptable range, an
 * appropriate error message is returned. Otherwise, the function returns null,
 * indicating a valid input.
 *
 * @param value The string input to be validated.
 * @return A string containing an error message if the input is invalid, or null if the input is valid.
 */
fun validateIntInput(value: String): String? {
    val intVal = value.toIntOrNull()
    return when {
        intVal == null -> "Invalid number"
        intVal < 1 -> "Must be at least 1"
        intVal > Constants.MaxUserInputNum -> "Must be at most ${Constants.MaxUserInputNum}"
        else -> null // valid input
    }
}

/**
 * Generates a Composable function for displaying an error message and a flag indicating if there is an error.
 *
 * This function takes an optional error message string as input. If the error message is not null,
 * it returns a pair consisting of a Composable function that, when invoked, displays the error
 * message in a predefined style (e.g., red color, bold font), and a boolean flag set to true
 * indicating the presence of an error. If the error message is null, indicating no error,
 * it returns a pair with a null Composable function and a false flag. This function is useful
 * for UI components that need to conditionally display error messages based on various
 * states or validations in the app.
 *
 * @param error An optional error message string. If null, indicates no error.
 * @return A Pair of a nullable Composable function and a boolean flag. The Composable function,
 *         when not null, can be used to display the error message. The boolean flag indicates
 *         the presence of an error.
 */
fun getErrorInfoFor(error: String?): Pair<(@Composable (() -> Unit))?, Boolean> {
    return if (error == null) {
        Pair(null, false)
    } else {
        Pair(
            {
                Text(
                    text = error,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            },
            true
        )
    }
}
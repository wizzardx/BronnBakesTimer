package com.example.bronnbakestimer

import android.util.Log
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
fun formatMinSec(totalSeconds: Long): String =
    String.format(
        Locale.ROOT,
        "%02d:%02d",
        totalSeconds / Constants.SecondsPerMinute,
        totalSeconds % Constants.SecondsPerMinute
    )

/**
 * Normalises a string input to remove non-numeric characters and leading zeros.
 *
 * This function processes a string input, typically representing a number, and
 * removes any non-digit characters. It also removes leading zeros while
 * preserving the numerical value of the string. If the string does not contain
 * any digits, or only contains zeros, the result is "0". This function is
 * useful for sanitizing and standardizing numerical user inputs, ensuring they
 * are in a format suitable for further processing or conversion to an integer.
 *
 * @param s The string input to be normalised.
 * @return A normalised string with non-numeric characters removed and leading zeros stripped.
 */
fun normaliseIntInput(s: String): String {
    // Filter out non-digit characters
    val numericPart = s.filter { it.isDigit() }

    // Remove leading zeros while preserving at least one zero if the string is empty
    val normalised = numericPart.dropWhile { it == '0' }.let {
        it.ifEmpty { "0" }
    }

    return normalised
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
    val intVal = value.toIntOrNull() ?: return "Invalid number"
    return when {
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
    return error?.let {
        Pair(
            {
                Text(
                    text = it,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            },
            true
        )
    } ?: Pair(null, false)
}

/**
 * Return the version number of our app.
 */
fun getAppVersion(): String = BuildConfig.VERSION_NAME

/**
 * Return the text to display on the Start/Pause/Resume button.
 */
fun getStartPauseResumeButtonText(timerData: TimerData?): String {
    return timerData?.let {
        if (it.isPaused) "Resume" else "Pause"
    } ?: "Start"
}

/**
 * Reports an error to the ErrorRepository and logs it using Android's Log.e().
 *
 * This function takes an exception and updates the ErrorRepository with its error message.
 * Additionally, it logs the error using the Android logging system with the "Error" level.
 * The error message is tagged with "BronnBakesTimer" for easy identification in logs.
 *
 * @param exception The Exception containing information about the error.
 */
fun logException(exception: Throwable) {
    ErrorRepository.updateData(exception.message)
    val tag = "BronnBakesTimer"
    Log.e(tag, "Error occurred: ", exception)
}

/**
 * Logs an error message and reports it to the ErrorRepository.
 *
 * This function takes an error message as input and updates the ErrorRepository with the provided message.
 * Additionally, it logs the error message using the Android logging system with the "Error" level.
 * The error message is tagged with "BronnBakesTimer" for easy identification in logs.
 *
 * @param msg The error message to be logged and reported.
 */
fun logError(msg: String) {
    ErrorRepository.updateData(msg)
    val tag = "BronnBakesTimer"
    Log.e(tag, msg)
}

fun userInputToSeconds(input: String, units: UserInputTimeUnitType = Constants.UserInputTimeUnit): Long {
    // TODO: Unit tests for this function
    // TODO: Docstrings for this function
    val i = input.toLongOrNull() ?: 0 // Empty (or none-numeric) user input gets converted to 0 by default
    return when (units) {
        UserInputTimeUnitType.MINUTES -> i * Constants.SecondsPerMinute
        UserInputTimeUnitType.SECONDS -> i
    }
}

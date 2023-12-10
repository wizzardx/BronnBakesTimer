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
    fun Create(error: String)
}

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
// The refactored getErrorInfoFor function
fun getErrorInfoFor(
    error: String?,
    uiCreator: ErrorUiCreator = defaultErrorUiCreator
): Pair<(@Composable (() -> Unit))?, Boolean> {
    return error?.let {
        Pair(
            { uiCreator.Create(it) },
            true
        )
    } ?: Pair(null, false)
}

// Default implementation of the UI creation, used in the actual app
// To manually check this, just get an error to show in the UI.
private val defaultErrorUiCreator = ErrorUiCreator { error ->
    Text(
        text = error,
        color = Color.Red,
        fontWeight = FontWeight.Bold
    )
}

/**
 * Return the text to display on the Start/Pause/Resume button.
 */
fun getStartPauseResumeButtonText(timerData: TimerData?): String {
    return timerData?.let {
        if (it.isPaused) "Resume" else "Pause"
    } ?: "Start"
}

/**
 * Reports an exception to the ErrorRepository and logs its details using Android's Log.e().
 *
 * This function is designed to handle exceptions by performing two key actions:
 * 1. Updating the ErrorRepository with the exception's message, thus making it available for observation and response.
 * 2. Logging the exception using Android's logging system at the "Error" level for diagnostics. The log is tagged with
 *    "BronnBakesTimer" for easy tracking in the log system.
 *
 * Usage of this function ensures consistent handling of exceptions throughout the application, aiding in both
 * debugging and user feedback mechanisms.
 *
 * @param exception The Throwable exception containing details about the error encountered.
 * @param errorRepository The IErrorRepository instance where the error message will be reported.
 */
fun logException(exception: Throwable, errorRepository: IErrorRepository) {
    errorRepository.updateData(exception.message)
    val tag = "BronnBakesTimer"
    Log.e(tag, "Error occurred: ", exception)
}

/**
 * Logs an error message and reports it to the ErrorRepository.
 *
 * This function simplifies error handling by:
 * 1. Reporting the provided error message to the ErrorRepository. This enables other components of the application
 *    to react to the error state as needed.
 * 2. Logging the error message at the "Error" level using Android's Log.e(), aiding in troubleshooting. The log
 *    is tagged with "BronnBakesTimer" to distinguish it in the application's log output.
 *
 * It's a useful utility for uniform error reporting and logging across the application, promoting consistency and ease
 * of debugging.
 *
 * @param msg The string containing the error message to be logged and reported.
 * @param errorRepository The IErrorRepository instance where the error message will be reported.
 */
fun logError(msg: String, errorRepository: IErrorRepository) {
    errorRepository.updateData(msg)
    val tag = "BronnBakesTimer"
    Log.e(tag, msg)
}

/**
 * Converts user input into seconds based on the specified time unit.
 *
 * This function processes a string input representing a time duration and converts it into
 * an equivalent duration in seconds. It supports different units of time (minutes or seconds)
 * and handles the conversion accordingly. The function is robust against non-numeric inputs,
 * converting them to 0 by default.
 *
 * @param input The user input string representing the time duration. This should be a numeric
 *              string. Non-numeric input is treated as 0.
 * @param units An optional parameter of type UserInputTimeUnitType, which specifies the unit of
 *              time represented by the input. It defaults to a standard unit defined in
 *              Constants.UserInputTimeUnit (e.g., minutes or seconds).
 * @return The equivalent duration in seconds as a Long. If the input is non-numeric or empty,
 *         the function returns 0.
 */
fun userInputToSeconds(input: String, units: UserInputTimeUnitType = Constants.UserInputTimeUnit): Long {
    val i = input.toLongOrNull() ?: 0 // Empty (or none-numeric) user input gets converted to 0 by default
    return when (units) {
        UserInputTimeUnitType.MINUTES -> i * Constants.SecondsPerMinute
        UserInputTimeUnitType.SECONDS -> i
    }
}

/**
 * Formats the total time remaining as a string based on timer data and user-inputted timer duration.
 *
 * This function calculates and formats the total time remaining as a string, taking into account the timer data
 * and the user-inputted timer duration. If timer data is available (not null), it uses the milliseconds remaining
 * from the timer data. Otherwise, it calculates the total time remaining based on the user-inputted timer duration.
 * The formatted string is in the format MM:SS (minutes:seconds), where MM and SS are padded with zeros if necessary.
 *
 * @param timerData The timer data representing the main timer. If null, the user-inputted timer duration will be used.
 * @param timerDurationInput The user-inputted timer duration as a string.
 * @return A string representing the total time remaining in MM:SS format.
 */
fun formatTotalTimeRemainingString(timerData: TimerData?, timerDurationInput: String): String {
    val secondsRemaining = if (timerData == null) {
        userInputToSeconds(timerDurationInput)
    } else {
        timerData.millisecondsRemaining / Constants.MillisecondsPerSecond
    }
    return formatMinSec(secondsRemaining)
}

/**
 * Converts a user-inputted time duration from a string to milliseconds.
 *
 * This function takes a string input representing a time duration and converts it into
 * an equivalent duration in milliseconds. It first converts the input into seconds using
 * the `userInputToSeconds` function, then multiplies the result by the number of milliseconds
 * per second (defined in `Constants.MillisecondsPerSecond`) to get the duration in milliseconds.
 *
 * This function is useful for converting user-inputted time durations into a format suitable
 * for use with timer functions or other operations that require time durations in milliseconds.
 *
 * @param input The user input string representing the time duration. This should be a numeric
 *              string. Non-numeric input is treated as 0 by the `userInputToSeconds` function.
 * @return The equivalent duration in milliseconds as a Long. If the input is non-numeric or empty,
 *         the function returns 0.
 */
fun userInputToMillis(input: String): Long {
    val seconds = userInputToSeconds(input)
    return seconds * Constants.MillisecondsPerSecond
}

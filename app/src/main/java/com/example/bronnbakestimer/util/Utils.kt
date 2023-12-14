package com.example.bronnbakestimer.util

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.bronnbakestimer.logic.Constants
import com.example.bronnbakestimer.provider.IErrorLoggerProvider
import com.example.bronnbakestimer.repository.IErrorRepository
import com.example.bronnbakestimer.service.TimerData
import com.example.bronnbakestimer.logic.UserInputTimeUnitType
import com.example.bronnbakestimer.logic.ValidationResult
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
fun formatMinSec(totalSeconds: Seconds): String {
    val seconds = totalSeconds.value
    return String.format(
        Locale.ROOT,
        "%02d:%02d",
        seconds / Constants.SECONDS_PER_MINUTE,
        seconds % Constants.SECONDS_PER_MINUTE
    )
}

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
 * Validates a string input as an integer within a specified range.
 *
 * This function takes a string input and attempts to convert it to an integer. If the conversion is successful,
 * it checks whether the integer is within a specified range (1 to Constants.MaxUserInputNum, inclusive).
 *
 * The function returns a ValidationResult object. If the input is valid, it returns ValidationResult.Valid.
 * If the input is invalid, it returns ValidationResult.Invalid with a reason for the invalidity.
 *
 * This function is useful for validating user inputs in scenarios where an integer within a specific range is expected.
 *
 * @param value The string input to be validated.
 * @return A ValidationResult object representing the result of the validation. If the input is valid, it returns
 * ValidationResult.Valid. If the input is invalid, it returns ValidationResult.Invalid with a reason for the
 * invalidity.
 */
fun validateIntInput(value: String): ValidationResult {
    val intVal = value.toIntOrNull()
    return when {
        intVal == null -> ValidationResult.Invalid("Invalid number")
        intVal < 1 -> ValidationResult.Invalid("Must be at least 1")
        intVal > Constants.MAX_USER_INPUT_NUM ->
            ValidationResult.Invalid("Must be at most ${Constants.MAX_USER_INPUT_NUM}")
        else -> ValidationResult.Valid
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
 * Logs an exception and reports its message to the ErrorRepository.
 *
 * This function takes an exception as input and performs two actions:
 * 1. It updates the ErrorRepository with the message of the exception. This allows other components of the application
 *    to react to the error state as needed.
 * 2. It logs the exception at the "Error" level using the provided ErrorLoggerProvider. The log is tagged with
 *    "BronnBakesTimer" and prefixed with "Error occurred: " to distinguish it in the application's log output.
 *
 * This function is a useful utility for uniform exception handling across the application, promoting consistency and
 * ease of debugging.
 *
 * @param exception The exception to be logged and reported.
 * @param errorRepository The IErrorRepository instance where the exception message will be reported.
 * @param logger The ErrorLoggerProvider used for logging the exception.
 */
fun logException(exception: Throwable, errorRepository: IErrorRepository, logger: IErrorLoggerProvider) {
    errorRepository.updateData(exception.message)
    logger.logError("BronnBakesTimer", "Error occurred: ", exception)
}

/**
 * Logs an error message and reports it to the ErrorRepository.
 *
 * This function takes an error message as input and performs two actions:
 * 1. It updates the ErrorRepository with the error message. This allows other components of the application
 *    to react to the error state as needed.
 * 2. It logs the error message at the "Error" level using the provided ErrorLoggerProvider. The log is tagged with
 *    "BronnBakesTimer" and the error message is used as the log message.
 *
 * This function is a useful utility for uniform error handling across the application, promoting consistency and
 * ease of debugging.
 *
 * @param msg The error message to be logged and reported.
 * @param errorRepository The IErrorRepository instance where the error message will be reported.
 * @param logger The ErrorLoggerProvider used for logging the error.
 */
fun logError(msg: String, errorRepository: IErrorRepository, logger: IErrorLoggerProvider) {
    errorRepository.updateData(msg)
    logger.logError("BronnBakesTimer", msg)
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
 * @return The equivalent duration in seconds as a Int. If the input is non-numeric or empty,
 *         the function returns 0.
 */
fun userInputToSeconds(input: String, units: UserInputTimeUnitType = Constants.UserInputTimeUnit): Seconds {
    val i = input.toIntOrNull() ?: 0 // Empty (or none-numeric) user input gets converted to 0 by default
    return when (units) {
        UserInputTimeUnitType.MINUTES -> Seconds(i * Constants.SECONDS_PER_MINUTE)
        UserInputTimeUnitType.SECONDS -> Seconds(i)
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
        Seconds(timerData.millisecondsRemaining / Constants.MILLISECONDS_PER_SECOND)
    }
    return formatMinSec(secondsRemaining)
}

/**
 * Overloaded version of formatTotalTimeRemainingString that accepts Seconds? and a timer duration string.
 *
 * @param seconds The remaining time in Seconds, nullable. If null, uses the timerDurationInput for calculation.
 * @param timerDurationInput The user-inputted timer duration as a string.
 * @return A string representing the total time remaining in MM:SS format.
 */
fun formatTotalTimeRemainingString(seconds: Seconds?, timerDurationInput: String): String {
    val totalSeconds = seconds ?: userInputToSeconds(timerDurationInput)
    return formatMinSec(totalSeconds)
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
 * @return The equivalent duration in milliseconds as a Int. If the input is non-numeric or empty,
 *         the function returns 0.
 */
fun userInputToMillis(input: String): Int {
    val seconds = userInputToSeconds(input)
    return seconds.value * Constants.MILLISECONDS_PER_SECOND
}

/**
 * Provides a runtime implementation of the ErrorLoggerProvider interface.
 *
 * This variable is an instance of ErrorLoggerProvider that uses Android's Log.e method to log error messages.
 * It is intended to be used in a runtime environment, where logs are written to the Android log output.
 *
 * The ErrorLoggerProvider interface takes three parameters: a tag, a message, and a Throwable. The tag is used
 * to identify the source of the log message. The message is the actual content to be logged. The Throwable is
 * the exception that caused the error.
 *
 * In this implementation, the tag, message, and Throwable are passed directly to Log.e. This writes an error
 * message to the Android log output, which can be viewed and filtered in the Logcat window in Android Studio.
 */
val runtimeErrorLoggerProvider = IErrorLoggerProvider { tag, message, throwable ->
    Log.e(tag, message, throwable)
}

/**
 * Provides a runtime implementation of the ErrorLoggerProvider interface.
 *
 * This variable is an instance of ErrorLoggerProvider that uses Android's Log.e method to log error messages.
 * It is intended to be used in a runtime environment, where logs are written to the Android log output.
 *
 * The ErrorLoggerProvider interface takes three parameters: a tag, a message, and a Throwable. The tag is used
 * to identify the source of the log message. The message is the actual content to be logged. The Throwable is
 * the exception that caused the error.
 *
 * In this implementation, the tag, message, and Throwable are passed directly to Log.e. This writes an error
 * message to the Android log output, which can be viewed and filtered in the Logcat window in Android Studio.
 */
val testErrorLoggerProvider = IErrorLoggerProvider { tag, message, throwable ->
    // Custom implementation for testing, e.g., print to console or use a mock logger
    println("[$tag] $message - ${throwable.message}")
}

/**
 * Represents a time duration in seconds.
 *
 * This value class wraps an integer value representing a duration in seconds. It ensures that the
 * seconds value is non-negative and provides a structured way to work with time durations in the
 * application. The class also implements `Comparable` to allow comparison between different `Seconds`
 * instances.
 *
 * @property value The duration in seconds as an integer. Must not be negative.
 * @throws IllegalArgumentException if the value is negative.
 */
@JvmInline
value class Seconds(val value: Int) : Comparable<Seconds> {
    init {
        require(value >= 0) { "Seconds must not be negative" }
    }

    override fun compareTo(other: Seconds): Int = value.compareTo(other.value)
}
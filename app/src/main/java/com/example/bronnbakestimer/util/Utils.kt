package com.example.bronnbakestimer.util

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.bronnbakestimer.logic.Constants
import com.example.bronnbakestimer.logic.UserInputTimeUnit
import com.example.bronnbakestimer.provider.IErrorLoggerProvider
import com.example.bronnbakestimer.repository.IErrorRepository
import com.example.bronnbakestimer.service.TimerData
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
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
    // Regular unit tests can't get good coverage for this, so we'll just manually check it
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
 * @param logger The ErrorLoggerProvider used for logging the error. If null then no exception to log
 */
fun logError(msg: String, errorRepository: IErrorRepository, logger: IErrorLoggerProvider) {
    errorRepository.updateData(msg)
    logger.logError("BronnBakesTimer", msg, null)
}

/**
 * Converts user input to seconds based on the specified time unit.
 *
 * @param input The user input as a String.
 * @param units The time unit type (MINUTES or SECONDS).
 * @return A Result containing the converted value in seconds (Ok) or an error message (Err).
 */

// TODO: Unit test the below
// TODO: Refactor the below, get GPT4-assistance, etc.
// TODO: Make checkRange arg optional?
fun userInputToSeconds(
    input: String,
    units: UserInputTimeUnit = Constants.USER_INPUT_TIME_UNIT,
    checkRange: Boolean,
): Result<Seconds, String> {
    return input.toIntOrNull()?.let { integerValue ->
        // Keep the input within the allowed range.
        if (checkRange) {
            // Range check required, so do that here:
            when {
                integerValue <= 0 -> {
                    Err("Must be at least 1")
                }

                integerValue > Constants.MAX_USER_INPUT_NUM -> {
                    Err("Must be at most ${Constants.MAX_USER_INPUT_NUM}")
                }

                else -> {
                    // Calculate the result based on the selected time unit.
                    val resultSeconds = when (units) {
                        UserInputTimeUnit.MINUTES -> integerValue * Constants.SECONDS_PER_MINUTE
                        UserInputTimeUnit.SECONDS -> integerValue
                    }
                    Ok(Seconds(resultSeconds))
                }
            }
        } else {
            // Not checking the range, so calculate the range based on the selected time unit:
            val resultSeconds = when (units) {
                UserInputTimeUnit.MINUTES -> integerValue * Constants.SECONDS_PER_MINUTE
                UserInputTimeUnit.SECONDS -> integerValue
            }
            // Before we return the result, make sure that we're not going to send a negative
            // value into the Seconds constructor (since we're not checking the range). It
            // doesn't like that.
            if (resultSeconds < 0) {
                Err("Negative seconds not allowed")
            } else {
                Ok(Seconds(resultSeconds))
            }
        }
    } ?: Err("Invalid number")
}

/**
 * Formats the total time remaining into a minutes:seconds string.
 *
 * This function takes a TimerData object and a timer duration input as input parameters.
 * It calculates the time remaining based on the TimerData or the user input and formats it into
 * a string in the MM:SS (minutes:seconds) format. The function returns a Result containing the
 * formatted string (Ok) or an error message (Err) if there's an issue with the input.
 *
 * @param timerData The TimerData object representing the timer state.
 * @param timerDurationInput The user input for the timer duration as a String.
 * @return A Result containing the formatted time remaining string (Ok) or an error message (Err).
 */

// TODO: Get high coverage on the two versions of formatTotalTimeRemainingString, and then refactor more,
//       factor out common logic, etc.

fun formatTotalTimeRemainingString(timerData: TimerData?, timerDurationInput: String): Result<String, String> {
    val maybeSecondsRemaining = if (timerData == null) {
        userInputToSeconds(timerDurationInput, checkRange = false)
    } else {
        Ok(Seconds(timerData.millisecondsRemaining / Constants.MILLISECONDS_PER_SECOND))
    }
    if (maybeSecondsRemaining is Err) {
        return Err(maybeSecondsRemaining.error)
    }
    return Ok(formatMinSec((maybeSecondsRemaining as Ok).value))
}

/**
 * Formats the total time remaining into a minutes:seconds string.
 *
 * This function takes a Seconds object and a timer duration input as input parameters.
 * It calculates the time remaining based on the Seconds object or the user input and formats it into
 * a string in the MM:SS (minutes:seconds) format. The function returns a Result containing the
 * formatted string (Ok) or an error message (Err) if there's an issue with the input.
 *
 * @param seconds The duration in seconds as a Seconds object.
 * @param timerDurationInput The user input for the timer duration as a String.
 * @return A Result containing the formatted time remaining string (Ok) or an error message (Err).
 */
fun formatTotalTimeRemainingString(seconds: Seconds?, timerDurationInput: String): Result<String, String> {
    val maybeSecondsRemaining = if (seconds == null) {
        userInputToSeconds(timerDurationInput, checkRange = false)
    } else {
        Ok(seconds)
    }
    if (maybeSecondsRemaining is Err) {
        return Err(maybeSecondsRemaining.error)
    }
    return Ok(formatMinSec((maybeSecondsRemaining as Ok).value))
}

/**
 * Converts user input to milliseconds.
 *
 * @param input The user input as a String.
 * @return A Result containing the converted value in milliseconds (Ok) or an error message (Err).
 */
// TODO: Update docstring above? TODO: Make sure that unit testing here is good?
fun userInputToMillis(input: String): Result<Int, String> {
    // Validate input: It should be a valid integer.
    val maybeSeconds = userInputToSeconds(input, checkRange = true)
    if (maybeSeconds is Err) {
        return Err(maybeSeconds.error)
    }

    // Calculate milliseconds based on the seconds value.
    val secondsValue = (maybeSeconds as Ok).value.value
    val milliseconds = secondsValue * Constants.MILLISECONDS_PER_SECOND

    // Return the result.
    return Ok(milliseconds)
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
    // It's hard to unit test this, since Log.e is an android internal, so we can't mock it.
    // We can manually check it though, by getting an error to show in the UI.
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
    var msg = "[$tag] $message"
    if (throwable != null) {
        msg += " - ${throwable.message}"
    }
    println(msg)
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

/**
 * Exception thrown when the timer duration input is invalid.
 *
 * This exception is used within the timer management system to signify
 * that the provided input for the timer's duration does not meet the required
 * criteria or format. This could occur in situations where the input is
 * non-numeric, out of acceptable range, or in an improper format.
 *
 * The exception includes a descriptive message that provides specific details
 * about the nature of the invalid input, aiding in troubleshooting and informing
 * the user or developer about the nature of the error.
 *
 * @param message A string detailing the specific reason why the timer duration
 *                input is considered invalid. This message is intended to be
 *                descriptive enough to diagnose the specific nature of the
 *                invalid input.
 */
class InvalidTimerDurationException(message: String) : Exception(message)

/**
 * Measures the average execution time of a given block of code over a specified number of iterations.
 *
 * This function executes a block of code multiple times (defined by the `times` parameter) and calculates
 * the average execution time. It's designed to help in profiling code to identify performance bottlenecks.
 * Dependency injection for time measurement and output functions is utilized to enhance testability.
 *
 * @param times The number of times the block of code should be executed. Defaults to 1.
 *              If a non-positive value is provided, it defaults to 1 execution.
 * @param block The block of code to be profiled. This is a lambda function with no arguments and no return value.
 * @param timeProvider A function that provides the current time in milliseconds. By default, it uses
 *                      `System.currentTimeMillis`.
 *                     This can be replaced with a custom function in testing environments.
 * @param printFunction A function to output the results. By default, it uses `println`.
 *                      This can be replaced with a custom function in testing environments.
 *
 * Usage:
 *   myProfiler(times = 5) {
 *       // Code to be profiled
 *   }
 *
 * Note:
 *   This function is designed for simplicity and basic profiling scenarios. For more complex performance
 *   testing, consider using dedicated profiling tools or libraries.
 */
fun myProfiler(
    times: Int = 1,
    timeProvider: () -> Long = System::currentTimeMillis,
    printFunction: (String) -> Unit = ::println,
    block: () -> Unit,
) {
    val totalTimes = if (times > 0) times else 1 // Ensure at least one execution
    val startTime = timeProvider()

    repeat(totalTimes) {
        block()
    }

    val endTime = timeProvider()
    val averageTime = (endTime - startTime) / totalTimes.toFloat()
    printFunction("Average Execution Time for $totalTimes runs: $averageTime ms")
}

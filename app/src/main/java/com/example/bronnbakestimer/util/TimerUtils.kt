package com.example.bronnbakestimer.util

import com.example.bronnbakestimer.logic.Constants
import com.example.bronnbakestimer.logic.UserInputTimeUnit
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result

/**
 * Converts user input to seconds based on the specified time unit.
 *
 * This function interprets a string representing a time duration and converts it into seconds.
 * It supports two types of time units: minutes and seconds, as defined in `UserInputTimeUnit`.
 * The input string is first converted to an integer. If the conversion is successful and the
 * value is within a specified range (if range checking is enabled), it's then converted to seconds
 * based on the provided time unit. The function returns a `Result` type, encapsulating either the
 * calculated seconds in a `Seconds` object (Ok) or an error message (Err) in case of invalid input
 * or out-of-range values.
 *
 * The function allows for optional range checking of the input value. When enabled, the input must
 * be a positive number and not exceed a predefined maximum. When range checking is disabled, the
 * input is still validated for being a valid integer but can include larger values.
 *
 * @param input The user input as a string, representing the time duration.
 * @param units The time unit for conversion, either minutes or seconds. Defaults to the unit defined in
 *              `Constants.USER_INPUT_TIME_UNIT`.
 * @param checkRange Boolean flag to enable or disable range checking of the input value. Defaults to true.
 * @return A `Result<Seconds, String>` encapsulating either the successful conversion result (Ok) or an error message
 *         (Err).
 */
fun userInputToSeconds(
    input: String,
    units: UserInputTimeUnit = Constants.USER_INPUT_TIME_UNIT,
    checkRange: Boolean = true,
): Result<Seconds, String> {
    val integerValue = input.toIntOrNull()
    val errorMessage =
        when {
            integerValue == null -> "Invalid input"
            checkRange && integerValue <= 0 -> "Input must be greater than 0"
            checkRange && integerValue > Constants.MAX_USER_INPUT_NUM -> "Input exceeds maximum limit"
            else -> null
        }

    return if (errorMessage != null) {
        Err(errorMessage)
    } else {
        // At this point, integerValue is guaranteed to be non-null.
        val resultSeconds =
            when (units) {
                UserInputTimeUnit.MINUTES -> integerValue!! * Constants.SECONDS_PER_MINUTE
                UserInputTimeUnit.SECONDS -> integerValue
            }

        // Since integerValue is non-null and Constants.SECONDS_PER_MINUTE is a non-null constant,
        // resultSeconds is guaranteed to be non-null.
        if (resultSeconds!! < 0) Err("Negative seconds not allowed") else Ok(Seconds(resultSeconds))
    }
}

/**
 * Converts user input representing time duration to milliseconds.
 *
 * This function takes a string input, interprets it as a time duration based on user input format,
 * and converts it into milliseconds. The input is first passed to the `userInputToSeconds` function
 * to convert it into seconds. If this conversion is successful, the result is then multiplied by
 * the number of milliseconds in a second to obtain the equivalent duration in milliseconds.
 * The function returns a `Result` type, encapsulating either the calculated milliseconds (Ok)
 * or an error message (Err) in case of invalid input.
 *
 * The function is useful for scenarios where time duration needs to be represented in milliseconds
 * for further processing, such as setting timers or performing time calculations.
 *
 * @param input The user input as a string, representing the time duration.
 * @return A `Result<Int, String>` encapsulating either the successful conversion result in milliseconds (Ok)
 *         or an error message (Err).
 */
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

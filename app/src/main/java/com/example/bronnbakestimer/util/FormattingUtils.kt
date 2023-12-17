package com.example.bronnbakestimer.util

import com.example.bronnbakestimer.logic.Constants
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
        seconds % Constants.SECONDS_PER_MINUTE,
    )
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
fun formatTotalTimeRemainingString(
    timerData: TimerData?,
    timerDurationInput: String,
): Result<String, String> {
    val maybeSecondsRemaining =
        if (timerData == null) {
            userInputToSeconds(timerDurationInput, checkRange = false)
        } else {
            Ok(timerData.nanosRemaining.toSeconds())
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
fun formatTotalTimeRemainingString(
    seconds: Seconds?,
    timerDurationInput: String,
): Result<String, String> {
    val maybeSecondsRemaining =
        if (seconds == null) {
            userInputToSeconds(timerDurationInput, checkRange = false)
        } else {
            Ok(seconds)
        }
    if (maybeSecondsRemaining is Err) {
        return Err(maybeSecondsRemaining.error)
    }
    return Ok(formatMinSec((maybeSecondsRemaining as Ok).value))
}

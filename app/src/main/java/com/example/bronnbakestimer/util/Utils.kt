package com.example.bronnbakestimer.util

import com.example.bronnbakestimer.service.TimerData

/**
 * Return the text to display on the Start/Pause/Resume button.
 */
fun getStartPauseResumeButtonText(timerData: TimerData?): String {
    return timerData?.let {
        if (it.isPaused) "Resume" else "Pause"
    } ?: "Start"
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

package com.example.bronnbakestimer.service

/**
 * Represents the state of a countdown timer.
 *
 * @property millisecondsRemaining The number of milliseconds remaining in the countdown. Should be
 *                                 a non-negative value.
 * @property isPaused A flag indicating whether the timer is currently paused. True if paused, false otherwise.
 * @property isFinished A flag indicating whether the timer has finished its countdown. True if the countdown has
 *                      reached zero, false otherwise.
 * @property beepTriggered A flag indicating whether a beep has been triggered. Typically used to signal the end of
 *                         the countdown. True if beep is triggered, false otherwise.
 */
data class TimerData(
    val millisecondsRemaining: Int = 0,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    val beepTriggered: Boolean = false
    // TODO: Some validations during construction
)

package com.example.bronnbakestimer

import java.util.UUID

/**
 * Represents extra timer data, including the timer's data, inputs, and a unique identifier.
 *
 * @property data The data associated with the timer.
 * @property inputs The inputs for the timer.
 * @property id The unique identifier for this timer instance.
 */
data class ExtraTimerData(
    val data: TimerData,
    val inputs: ExtraTimerInputsData,
    val id: UUID = UUID.randomUUID(),
)

/**
 * Extension function to calculate the total seconds remaining for the timer.
 *
 * @receiver ExtraTimerData The extra timer data instance.
 * @param mainTimerActive Indicates if the main timer is active.
 * @return The total seconds remaining.
 */
fun ExtraTimerData.getTotalSeconds(mainTimerActive: Boolean): Long {
    return if (mainTimerActive) {
        // Timer is active, so use the current remaining time
        data.millisecondsRemaining / Constants.MillisecondsPerSecond
    } else {
        // Timer is not active, so use a value from the users input
        val userInput = inputs.timerDurationInput.value
        userInputToSeconds(userInput)
    }
}

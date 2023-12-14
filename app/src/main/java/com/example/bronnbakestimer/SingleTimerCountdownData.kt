package com.example.bronnbakestimer

/**
 * Data class representing the countdown data for a single timer.
 *
 * @property data The [TimerData] associated with the single timer.
 * @property useInputTimerId The [TimerUserInputDataId] that this countdown data is associated with.
 */
data class SingleTimerCountdownData(
    val data: TimerData,
    val useInputTimerId: TimerUserInputDataId,
)

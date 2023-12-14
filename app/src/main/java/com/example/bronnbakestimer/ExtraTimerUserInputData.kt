package com.example.bronnbakestimer

/**
 * Data class representing user input data for extra timers.
 *
 * @property inputs The [ExtraTimerInputsData] containing input data for the timer.
 * @property id The unique identifier [TimerUserInputDataId] for this timer input data.
 */
data class ExtraTimerUserInputData(
    val inputs: ExtraTimerInputsData = ExtraTimerInputsData(),
    val id: TimerUserInputDataId = TimerUserInputDataId.randomId()
)

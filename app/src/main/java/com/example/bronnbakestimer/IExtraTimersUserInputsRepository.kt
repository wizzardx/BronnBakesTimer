package com.example.bronnbakestimer

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining the operations for a repository managing user inputs for extra timers.
 */
interface IExtraTimersUserInputsRepository {
    /**
     * A read-only [StateFlow] that emits the current state of user input data for extra timers.
     * The data is a list of [ExtraTimerUserInputData].
     */
    val timerData: StateFlow<List<ExtraTimerUserInputData>>

    /**
     * Updates the user input data for extra timers with new data.
     *
     * @param newData The new list of [ExtraTimerUserInputData] to be used.
     */
    fun updateData(newData: List<ExtraTimerUserInputData>)
}

package com.example.bronnbakestimer

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining the operations for a repository managing extra timers.
 */
interface IExtraTimersRepository {
    /**
     * A read-only [StateFlow] that emits the current state of the extra timers.
     */
    val timerData: StateFlow<List<ExtraTimerData>>

    /**
     * Updates the timer data with new data.
     *
     * @param newData The new list of ExtraTimerData to be used.
     */
    fun updateData(newData: List<ExtraTimerData>)
}

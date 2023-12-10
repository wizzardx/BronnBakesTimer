package com.example.bronnbakestimer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository for managing extra timer data.
 * This repository handles the state and operations related to extra timers in the application.
 */
class DefaultExtraTimersRepository : IExtraTimersRepository {
    // MutableStateFlow for internal updates
    private val _timerData = MutableStateFlow<List<ExtraTimerData>>(listOf())

    /**
     * Publicly exposed read-only StateFlow of timer data.
     * It provides a way to observe changes to the extra timers data.
     */
    override val timerData: StateFlow<List<ExtraTimerData>> = _timerData

    /**
     * Updates the list of extra timers with new data.
     * This function ensures that the millisecondsRemaining in all timers are non-negative
     * and updates the state flow with the new list.
     *
     * @param newData The new list of ExtraTimerData to update.
     * @throws IllegalArgumentException if any timer has negative millisecondsRemaining.
     */
    override fun updateData(newData: List<ExtraTimerData>) {
        // Ensure the millisecondsRemaining in all the Timers are none-negative:
        for (timer in newData) {
            val msRemaining = timer.data.millisecondsRemaining
            require(msRemaining >= 0) {
                "Time remaining cannot be negative"
            }
        }

        _timerData.value = newData // Atomic and thread-safe update
    }
}

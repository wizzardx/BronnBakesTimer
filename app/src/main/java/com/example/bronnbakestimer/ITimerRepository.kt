package com.example.bronnbakestimer

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining the contract for a repository managing timer data.
 * It provides a flow of timer data and a method to update this data.
 */
interface ITimerRepository {

    /**
     * A [StateFlow] of [TimerData] representing the current state of the timer.
     * It emits the latest timer data whenever there are any updates.
     * The flow can emit `null` if the timer state has not been initialized.
     */
    val timerData: StateFlow<TimerData?>

    /**
     * Updates the current state of the timer with the provided [newData].
     * If [newData] is `null`, it signifies that the timer state is not initialized.
     *
     * @param newData The new [TimerData] state to be emitted.
     *                `null` indicates an uninitialized timer state.
     */
    fun updateData(newData: TimerData?)

    /**
     * Pauses the timer by updating its state.
     *
     * This method is responsible for pausing the timer by modifying its state. It checks if the timer data is not null
     * and if the timer is not already paused. If both conditions are met, it updates the timer data to mark it as
     * paused. If any of the conditions are not met, an exception is thrown.
     *
     * @throws IllegalStateException if the timer data is null or if the timer is already paused.
     */
    fun pauseTimer() {
        val timerData = timerData.value
        check(timerData != null) { "Timer data must not be null" }
        check(!timerData.isPaused) { "Timer must not be paused" }
        updateData(timerData.copy(isPaused = true))
    }

    /**
     * Resumes the timer by updating its state.
     *
     * This method is responsible for resuming the timer by modifying its state. It checks if the timer data is not null
     * and if the timer is not already paused. If both conditions are met, it updates the timer data to mark it as not
     * paused (i.e., resumed). If any of the conditions are not met, an exception is thrown.
     *
     * @throws IllegalStateException if the timer data is null or if the timer is not paused.
     */
    fun resumeTimer() {
        val timerData = timerData.value
        check(timerData != null) { "Timer data must not be null" }
        check(timerData.isPaused) { "Timer must be paused" }
        updateData(timerData.copy(isPaused = false))
    }
}

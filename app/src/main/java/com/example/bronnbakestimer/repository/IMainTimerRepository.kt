package com.example.bronnbakestimer.repository

import com.example.bronnbakestimer.service.TimerData
import com.example.bronnbakestimer.util.Seconds
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining the contract for a repository managing timer data.
 * It provides a flow of timer data and a method to update this data.
 */
interface IMainTimerRepository {
    /**
     * A [StateFlow] of [TimerData] representing the current state of the timer.
     * It emits the latest timer data whenever there are any updates.
     * The flow can emit `null` if the timer state has not been initialized.
     */
    val timerData: StateFlow<TimerData?>

    /**
     * A read-only [StateFlow] of [Seconds] representing the remaining time of the timer.
     *
     * This property emits the latest remaining time in seconds for the current timer. It is continuously
     * updated to reflect the countdown of the timer in real-time. The flow can emit `null` if the timer's
     * remaining time has not been initialized or if the timer is reset. This property is part of the
     * [IMainTimerRepository] interface and is essential for any UI components or logic that require tracking
     * the timer's countdown.
     */
    val secondsRemaining: StateFlow<Seconds?>

    /**
     * A [StateFlow] of [Boolean] indicating whether the timer has completed its countdown.
     *
     * This property emits a `true` value when the timer finishes its countdown, and `false` otherwise.
     * It serves as a real-time indicator for the completion status of the timer, essential for UI components
     * and other logic that need to respond to the timer's completion. The flow may emit `null` if the timer's
     * completion status is uninitialized, such as when the timer is reset or paused.
     * This StateFlow is updated by the TimerService class upon the completion of the timer, ensuring that
     * all observing components receive consistent and up-to-date information about the timer's status.
     */
    val timerCompleted: StateFlow<Boolean?>

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

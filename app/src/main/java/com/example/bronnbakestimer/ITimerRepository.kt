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
}

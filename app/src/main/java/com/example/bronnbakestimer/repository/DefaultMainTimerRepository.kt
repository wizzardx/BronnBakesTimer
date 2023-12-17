package com.example.bronnbakestimer.repository

import com.example.bronnbakestimer.service.TimerData
import com.example.bronnbakestimer.util.Nanos
import com.example.bronnbakestimer.util.Seconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * `DefaultTimerRepository` is a concrete implementation of the `ITimerRepository` interface,
 * providing functionality for managing and updating a timer's state in a thread-safe manner.
 * This class encapsulates the state of a timer using a `MutableStateFlow` for internal updates,
 * and exposes a read-only `StateFlow` for external observers.
 *
 * The primary responsibility of this repository is to maintain the state of a timer, represented
 * by `TimerData`, and to ensure the atomicity and thread-safety of state updates. It allows
 * observers to subscribe to timer state changes and ensures that the timer's milliseconds remaining
 * is always a non-negative value.
 *
 * Usage:
 * - Instantiate `DefaultTimerRepository` to manage timer states.
 * - Use the `timerData` property to observe the current state of the timer.
 * - Call `updateData(newData: TimerData?)` to update the timer's state. This method is thread-safe
 *   and ensures atomic updates. The `newData` parameter can be `null` to indicate that the timer state
 *   is not initialized.
 * - The `updateData` method will throw an `IllegalArgumentException` if the `millisecondsRemaining`
 *   in `TimerData` is negative, enforcing the invariant that time remaining cannot be negative.
 *
 * Example:
 * ```
 * val timerRepository = DefaultTimerRepository()
 * timerRepository.timerData.collect { timerData ->
 *     // React to timer data changes
 * }
 * timerRepository.updateData(TimerData(millisecondsRemaining = 60000))
 * ```
 */
class DefaultMainTimerRepository : IMainTimerRepository {
    // MutableStateFlow for internal updates
    private val _timerData = MutableStateFlow<TimerData?>(null)

    private val _secondsRemaining = MutableStateFlow<Seconds?>(null)

    private val _timerCompleted = MutableStateFlow<Boolean?>(null)

    override val timerData: StateFlow<TimerData?> = _timerData

    override val secondsRemaining: StateFlow<Seconds?> = _secondsRemaining

    override val timerCompleted: StateFlow<Boolean?> = _timerCompleted

    /**
     * Updates the current state of the timer.
     *
     * This function is thread-safe and can be called from any coroutine context to update the timer state.
     * The update is atomic, ensuring that all observers see a consistent state.
     *
     * @param newData The new [TimerData] state to be emitted. If `null`, it indicates that the timer state is not
     *                initialized.
     */
    override fun updateData(newData: TimerData?) {
        // Ensure the millisecondsRemaining in TimerData is non-negative
        val nsRemaining = newData?.nanosRemaining ?: Nanos(0)
        require(nsRemaining >= Nanos(0L)) {
            "Time remaining cannot be negative"
        }

        // Update the main data:
        _timerData.value = newData // Atomic and thread-safe update

        // Also update _secondsRemaining, based on data in newData.
        val updatedSecondsRemaining = calcUpdatedSecsRemain(newData)
        if (updatedSecondsRemaining != _secondsRemaining.value) {
            _secondsRemaining.value = updatedSecondsRemaining
        }

        // Update "timer completed" stateflow, based on data in newData.
        val updatedTimerCompleted = isTimerCompleted(newData)
        if (updatedTimerCompleted != _timerCompleted.value) {
            _timerCompleted.value = updatedTimerCompleted
        }
    }

    // Function to calculate updated seconds remaining
    private fun calcUpdatedSecsRemain(newData: TimerData?): Seconds? = newData?.nanosRemaining?.toSeconds()

    // Function to determine if the timer is completed
    private fun isTimerCompleted(newData: TimerData?): Boolean? = newData?.isFinished
}

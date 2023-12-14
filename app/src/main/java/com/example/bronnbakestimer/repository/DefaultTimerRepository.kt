package com.example.bronnbakestimer.repository

import com.example.bronnbakestimer.logic.Constants
import com.example.bronnbakestimer.service.TimerData
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
class DefaultTimerRepository : ITimerRepository {
    // MutableStateFlow for internal updates
    private val _timerData = MutableStateFlow<TimerData?>(null)

    private val _secondsRemaining = MutableStateFlow<Seconds?>(null)

    /**
     * A read-only [StateFlow] that emits the current state of the timer.
     * It can be `null` if the timer state has not been initialized.
     */
    override val timerData: StateFlow<TimerData?> = _timerData

    override val secondsRemaining: StateFlow<Seconds?> = _secondsRemaining

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
        val msRemaining = newData?.millisecondsRemaining ?: 0
        require(msRemaining >= 0) {
            "Time remaining cannot be negative"
        }

        // Update the main data:
        _timerData.value = newData // Atomic and thread-safe update

        // Also update _secondsRemaining, based on data in newData.
        val newValue = newData?.let { Seconds(it.millisecondsRemaining / Constants.MILLISECONDS_PER_SECOND) }
        if (newValue != _secondsRemaining.value) {
            _secondsRemaining.value = newValue
        }
    }
}

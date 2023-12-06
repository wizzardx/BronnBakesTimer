package com.example.bronnbakestimer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Represents the state of a countdown timer.
 *
 * @property millisecondsRemaining The number of milliseconds remaining in the countdown. Should be
 *                                 a non-negative value.
 * @property isPaused A flag indicating whether the timer is currently paused. True if paused, false otherwise.
 * @property isFinished A flag indicating whether the timer has finished its countdown. True if the countdown has
 *                      reached zero, false otherwise.
 * @property beepTriggered A flag indicating whether a beep has been triggered. Typically used to signal the end of
 *                         the countdown. True if beep is triggered, false otherwise.
 */
data class TimerData(
    val millisecondsRemaining: Long,
    val isPaused: Boolean,
    val isFinished: Boolean,
    val beepTriggered: Boolean
)

/**
 * Singleton repository for managing timer state across the application.
 */
object TimerRepository : ITimerRepository {
    // MutableStateFlow for internal updates
    private val _timerData = MutableStateFlow<TimerData?>(null)

    /**
     * A read-only [StateFlow] that emits the current state of the timer.
     * It can be `null` if the timer state has not been initialized.
     */
    override val timerData: StateFlow<TimerData?> = _timerData

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

        _timerData.value = newData // Atomic and thread-safe update
    }
}

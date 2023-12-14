package com.example.bronnbakestimer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Repository implementation for managing the countdown of extra timers in the BronnBakesTimer app.
 *
 * This repository handles the storage and updates of countdown data for extra timers. It uses a
 * [ConcurrentHashMap] to store [SingleTimerCountdownData] for each timer, ensuring thread safety for
 * concurrent operations. The class provides functionalities to update timer data, track remaining seconds
 * for each timer, and emit changes through [StateFlow].
 *
 * Functions:
 * - [updateData]: Updates the countdown data for all extra timers with new information.
 * - [extraTimerSecsFlow]: Provides a [StateFlow] of [Seconds] representing the
 *   remaining time for a specific extra timer.
 *
 * Properties:
 * - [timerData]: A read-only [StateFlow] that emits the current countdown data for all extra timers.
 */
class DefaultExtraTimersCountdownRepository : IExtraTimersCountdownRepository {
    // MutableStateFlow for internal updates
    private val _timerData =
        MutableStateFlow<ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>>(ConcurrentHashMap())

    // A private concurrent safe mapping between extra timer ids, and StateFlows for their seconds remaining
    private val _secondsRemaining = ConcurrentHashMap<TimerUserInputDataId, MutableStateFlow<Seconds>>()

    /**
     * A read-only [StateFlow] that emits the current state of the countdown data for extra timers.
     * The data is stored in a [ConcurrentHashMap] with keys as [TimerUserInputDataId] and values as
     * [SingleTimerCountdownData].
     */
    override val timerData: StateFlow<ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>> = _timerData

    /**
     * Updates the countdown data for extra timers with new data.
     *
     * @param newData The new [ConcurrentHashMap] of [TimerUserInputDataId] to [SingleTimerCountdownData] to be used.
     */
    override fun updateData(newData: ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>) {
        // Ensure the millisecondsRemaining in all the Timers are none-negative:
        for (timer in newData) {
            val msRemaining = timer.value.data.millisecondsRemaining
            require(msRemaining >= 0) { "Time remaining cannot be negative" }
        }

        _timerData.value = newData // Atomic and thread-safe update

        // Get extra timer ids from the list of timers, after we update them with the new data
        val newIds = _timerData.value.map { it.value.useInputTimerId }

        // Remove entries in the _secondsRemaining map for timers that are no longer in the list
        _secondsRemaining.keys.removeAll { it !in newIds }

        // Add new StateFlow<Seconds> entries in the _secondsRemaining map for timers that are new to the list
        for (timer in newData) {
            if (!_secondsRemaining.containsKey(timer.value.useInputTimerId)) {
                val msInt = timer.value.data.millisecondsRemaining
                val secondsInt = msInt / Constants.MILLISECONDS_PER_SECOND
                val seconds = Seconds(secondsInt)
                val secondsStateFlow = MutableStateFlow(seconds)
                _secondsRemaining[timer.value.useInputTimerId] = secondsStateFlow
            }
        }

        // Update all the seconds in the stateflow mapping to match the new data
        for (timer in newData) {
            val msInt = timer.value.data.millisecondsRemaining
            val secondsInt = msInt / Constants.MILLISECONDS_PER_SECOND
            val seconds = Seconds(secondsInt)
            val maybeStateFlow = _secondsRemaining[timer.value.useInputTimerId]!!
            maybeStateFlow.value = seconds
        }
    }

    override fun extraTimerSecsFlow(timerUserInputDataId: TimerUserInputDataId): StateFlow<Seconds> {
        val maybeStateFlow = _secondsRemaining[timerUserInputDataId]
        requireNotNull(maybeStateFlow) { "No extra timer with id $timerUserInputDataId!" }
        return maybeStateFlow
    }
}

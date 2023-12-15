package com.example.bronnbakestimer.repository

import com.example.bronnbakestimer.logic.Constants
import com.example.bronnbakestimer.service.SingleTimerCountdownData
import com.example.bronnbakestimer.service.TimerData
import com.example.bronnbakestimer.util.Seconds
import com.example.bronnbakestimer.util.TimerUserInputDataId
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

    // A private concurrent safe mapping between extra timer ids, and StateFlows for their completion status
    private val _timerCompleted = ConcurrentHashMap<TimerUserInputDataId, MutableStateFlow<Boolean>>()

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
            val maybeStateFlow = _secondsRemaining[timer.value.useInputTimerId]
            if (maybeStateFlow != null) {
                maybeStateFlow.value = seconds
            } else {
                // Logic can never reach here, so we can't get test coverage:
                error("StateFlow for timer id ${timer.value.useInputTimerId} is missing in _secondsRemaining map")
            }
        }

        // TODO: Refactor this method....
        // TODO: Get a GPT-4 review...
        // TODO: After getting high test coverage here..

        // Remove entries in the _timerCompleted map for timers that are no longer in the list
        _timerCompleted.keys.removeAll { it !in newIds }

        // Add new StateFlow<Seconds> entries in the _timerCompleted map for timers that are new to the list
        for (timer in newData) {
            if (!_timerCompleted.containsKey(timer.value.useInputTimerId)) {
                val isFinished = timer.value.data.isFinished
                val isCompletedStateFlow = MutableStateFlow(isFinished)
                _timerCompleted[timer.value.useInputTimerId] = isCompletedStateFlow
            }
        }

        // Update all the completion statuses in the stateflow mapping to match the new data
        for (timer in newData) {
            val isFinished = timer.value.data.isFinished
            val maybeStateFlow = _timerCompleted[timer.value.useInputTimerId]
            if (maybeStateFlow != null) {
                if (isFinished) {
                    println() // Set a breakpoint here to debug
                }
                maybeStateFlow.value = isFinished
            } else {
                // Logic can never reach here, so we can't get test coverage:
                error("StateFlow for timer id ${timer.value.useInputTimerId} is missing in _timerCompleted map")
            }
        }
    }

    override fun extraTimerSecsFlow(timerUserInputDataId: TimerUserInputDataId): StateFlow<Seconds> {
        val maybeStateFlow = _secondsRemaining[timerUserInputDataId]
        requireNotNull(maybeStateFlow) { "No extra timer with id $timerUserInputDataId!" }
        return maybeStateFlow
    }

    /**
     * Provides a [StateFlow] of [Boolean] indicating whether a specific extra timer has completed its countdown.
     *
     * This method is an integral part of the [IExtraTimersCountdownRepository] interface and is responsible for
     * emitting the completion status of an extra timer identified by [id]. The method retrieves a
     * [StateFlow<Boolean>] that continuously emits updates to the completion status of the specified timer.
     * This allows for real-time tracking of whether the extra timer has completed its countdown.
     *
     * @param id The unique identifier [TimerUserInputDataId] of the extra timer
     *           for which the completion status is requested.
     * @return StateFlow<Boolean> that emits the completion status (true if completed, false otherwise)
     *         for the identified extra timer.
     * @throws IllegalArgumentException If no timer with the given identifier exists.
     */
    override fun extraTimerIsCompletedFlow(id: TimerUserInputDataId): StateFlow<Boolean> {
        val maybeStateFlow = _timerCompleted[id]
        requireNotNull(maybeStateFlow) { "No extra timer with id $id!" }
        return maybeStateFlow
    }

    override fun clearDataInAllTimers() {
        // Create a temporary map to hold the updated timer data
        val updatedTimers = ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>()

        // Iterate over each entry in the original _timerData map
        for ((id, timer) in _timerData.value) {
            // Create a new SingleTimerCountdownData with default TimerData and the same id
            val newTimerData = SingleTimerCountdownData(TimerData(), timer.useInputTimerId)

            // Add the updated timer data to the temporary map
            updatedTimers[id] = newTimerData

            // Also reset the related entries in _secondsRemaining and _timerCompleted maps
            _secondsRemaining[id]?.value = Seconds(0) // Assuming 0 seconds as the default
            _timerCompleted[id]?.value = false // Assuming false (not completed) as the default
        }

        // Update _timerData with the new map
        _timerData.value = updatedTimers
    }
}

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
    private val internalTimerData =
        MutableStateFlow<ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>>(ConcurrentHashMap())

    // A private concurrent safe mapping between extra timer ids, and StateFlows for their seconds remaining
    private val secondsRemaining = ConcurrentHashMap<TimerUserInputDataId, MutableStateFlow<Seconds>>()

    // A private concurrent safe mapping between extra timer ids, and StateFlows for their completion status
    private val internalTimerCompleted = ConcurrentHashMap<TimerUserInputDataId, MutableStateFlow<Boolean>>()

    /**
     * A read-only [StateFlow] that emits the current state of the countdown data for extra timers.
     * The data is stored in a [ConcurrentHashMap] with keys as [TimerUserInputDataId] and values as
     * [SingleTimerCountdownData].
     */
    override val timerData: StateFlow<ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>> =
        internalTimerData

    /**
     * Updates the countdown data for extra timers with new data.
     *
     * @param newData The new [ConcurrentHashMap] of [TimerUserInputDataId] to [SingleTimerCountdownData] to be used.
     */
    override fun updateData(newData: ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>) {
        // Validate that milliseconds remaining in all timers are non-negative
        for (timer in newData) {
            val msRemaining = timer.value.data.millisecondsRemaining
            require(msRemaining >= 0) { "Time remaining cannot be negative" }
        }

        // Update the main timer data
        internalTimerData.value = newData // Atomic and thread-safe update

        // Process each timer in the new data
        for ((timerId, timerData) in newData) {
            // Convert milliseconds remaining to seconds
            val secondsRemaining = timerData.data.millisecondsRemaining / Constants.MILLISECONDS_PER_SECOND

            // Update or initialize the seconds remaining StateFlow
            this.secondsRemaining.getOrPut(timerId) { MutableStateFlow(Seconds(secondsRemaining)) }
                .value = Seconds(secondsRemaining)

            // Update or initialize the timer completed StateFlow
            internalTimerCompleted.getOrPut(timerId) { MutableStateFlow(timerData.data.isFinished) }
                .value = timerData.data.isFinished
        }

        // Remove obsolete entries from _secondsRemaining and _timerCompleted maps
        val newIds = newData.keys
        secondsRemaining.keys.retainAll(newIds)
        internalTimerCompleted.keys.retainAll(newIds)
    }

    override fun extraTimerSecsFlow(timerUserInputDataId: TimerUserInputDataId): StateFlow<Seconds> {
        val maybeStateFlow = secondsRemaining[timerUserInputDataId]
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
        val maybeStateFlow = internalTimerCompleted[id]
        requireNotNull(maybeStateFlow) { "No extra timer with id $id!" }
        return maybeStateFlow
    }

    override fun clearDataInAllTimers() {
        // Create a temporary map to hold the updated timer data
        val updatedTimers = ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>()

        // Iterate over each entry in the original _timerData map
        for ((id, timer) in internalTimerData.value) {
            // Create a new SingleTimerCountdownData with default TimerData and the same id
            val newTimerData = SingleTimerCountdownData(TimerData(), timer.useInputTimerId)

            // Add the updated timer data to the temporary map
            updatedTimers[id] = newTimerData

            // Also reset the related entries in _secondsRemaining and _timerCompleted maps
            secondsRemaining[id]?.value = Seconds(0) // Assuming 0 seconds as the default
            internalTimerCompleted[id]?.value = false // Assuming false (not completed) as the default
        }

        // Update _timerData with the new map
        internalTimerData.value = updatedTimers
    }
}

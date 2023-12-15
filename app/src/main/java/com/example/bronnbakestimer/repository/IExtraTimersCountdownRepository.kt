package com.example.bronnbakestimer.repository

import com.example.bronnbakestimer.service.SingleTimerCountdownData
import com.example.bronnbakestimer.util.Seconds
import com.example.bronnbakestimer.util.TimerUserInputDataId
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Interface defining the operations for a repository managing extra timers countdown.
 */
interface IExtraTimersCountdownRepository {
    /**
     * A read-only [StateFlow] that emits the current state of the countdown data for extra timers.
     * The data is stored in a [ConcurrentHashMap] with keys as [TimerUserInputDataId] and values as
     * [SingleTimerCountdownData].
     */
    val timerData: StateFlow<ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>>

    /**
     * Updates the countdown data for extra timers with new data.
     *
     * @param newData The new [ConcurrentHashMap] of [TimerUserInputDataId] to [SingleTimerCountdownData] to be used.
     */
    fun updateData(newData: ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>)

    /**
     * Provides a [StateFlow] of [Seconds] representing the remaining time for a specific extra timer.
     *
     * This method is part of the [IExtraTimersCountdownRepository] interface and is responsible for
     * emitting the current remaining time for an extra timer identified by [timerUserInputDataId].
     * The method retrieves a [StateFlow<Seconds>] that continuously emits updates to the remaining
     * seconds for the specified timer, allowing real-time tracking of its countdown.
     *
     * @param timerUserInputDataId The unique identifier [TimerUserInputDataId] of the extra timer
     *                             for which the remaining time is requested.
     * @return StateFlow<Seconds> that emits the remaining seconds for the identified extra timer.
     * @throws IllegalArgumentException If no timer with the given identifier exists.
     */
    fun extraTimerSecsFlow(timerUserInputDataId: TimerUserInputDataId): StateFlow<Seconds>

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
    fun extraTimerIsCompletedFlow(id: TimerUserInputDataId): StateFlow<Boolean>

    /**
     * Clears all countdown-related data for every extra timer managed by the repository.
     *
     * This method resets the countdown data for all extra timers to their default states.
     * It is particularly useful for initializing or resetting the state of all extra timers,
     * such as at the beginning of a new user session or when a global reset of all timers is required.
     * After executing this method, the countdown data for each extra timer will be set to its initial,
     * default state. This includes resetting the remaining time to default values and marking timers as
     * not completed.
     *
     * Note that this method will affect all extra timers managed by the repository, and the changes will
     * be reflected in the corresponding [StateFlow]s that emit countdown data and completion status.
     */
    fun clearDataInAllTimers()
}

package com.example.bronnbakestimer.repository

import com.example.bronnbakestimer.service.SingleTimerCountdownData
import com.example.bronnbakestimer.util.TimerUserInputDataId
import com.example.bronnbakestimer.util.Seconds
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
}

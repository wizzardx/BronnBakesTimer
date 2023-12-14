package com.example.bronnbakestimer.service

import com.example.bronnbakestimer.repository.ITimerRepository

/**
 * Interface for managing timers.
 *
 * This interface defines methods for resuming, pausing, and clearing resources for timers. Implementations of this
 * interface provide the logic for managing timers.
 */
interface ITimerManager {
    /**
     * Resumes the timers managed by the provided timer repository.
     *
     * @param timerRepository The repository managing the timers.
     */
    fun resumeTimers(timerRepository: ITimerRepository)

    /**
     * Pauses the timers managed by the provided timer repository.
     *
     * @param timerRepository The repository managing the timers.
     */
    fun pauseTimers(timerRepository: ITimerRepository)

    /**
     * Clears the resources used by the timers managed by the provided timer repository.
     *
     * @param timerRepository The repository managing the timers.
     */
    fun clearResources(timerRepository: ITimerRepository)
}

package com.example.bronnbakestimer.service

import com.example.bronnbakestimer.repository.IExtraTimersCountdownRepository
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
     * Clears all resources and resets the state of timers managed by the provided repositories.
     *
     * This method is responsible for resetting the main timer managed by [timerRepository]
     * and also for clearing and resetting the countdown-related data for all extra timers
     * managed by [extraTimersCountdownRepo]. The method ensures that both the main timer
     * and all extra timers are reset to their initial states, effectively clearing any ongoing
     * countdowns and preparing them for a fresh start. This function is particularly useful
     * when needing to completely reset the state of all timers, such as at the end of an
     * activity or session, or when preparing the application for a new set of timer operations.
     *
     * After executing this method, the main timer and all extra timers will be reset,
     * with their countdown data cleared and set to default values.
     *
     * @param timerRepository The repository managing the main timer. It provides
     *                        functionalities to reset the main timer's data.
     * @param extraTimersCountdownRepo The repository managing the extra timers. It is
     *                                 used to clear and reset the countdown-related data
     *                                 in all extra timers.
     */
    fun clearResources(timerRepository: ITimerRepository, extraTimersCountdownRepo: IExtraTimersCountdownRepository)
}

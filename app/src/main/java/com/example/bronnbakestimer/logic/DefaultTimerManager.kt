package com.example.bronnbakestimer.logic

import com.example.bronnbakestimer.repository.IExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.ITimerRepository
import com.example.bronnbakestimer.service.ITimerManager

/**
 * Default implementation of the ITimerManager interface.
 * This class provides the functionality to manage timers.
 */
class DefaultTimerManager : ITimerManager {

    /**
     * Resumes the timers managed by the provided timer repository.
     * This includes resuming the main timer and any extra timers.
     *
     * @param timerRepository The repository managing the timers.
     */
    override fun resumeTimers(timerRepository: ITimerRepository) {
        // Resuming the main timer resume the extra timers too.
        timerRepository.resumeTimer()
    }

    /**
     * Pauses the timers managed by the provided timer repository.
     * This includes pausing the main timer and any extra timers.
     *
     * @param timerRepository The repository managing the timers.
     */
    override fun pauseTimers(timerRepository: ITimerRepository) {
        // Pausing the main timer pause the extra timers too.
        timerRepository.pauseTimer()
    }

    /**
     * Clears resources associated with all timers managed by the provided repositories.
     * This method is responsible for resetting the main timer managed by [timerRepository]
     * and also resetting the countdown-related data for all extra timers managed by
     * [extraTimersCountdownRepo].
     *
     * This function is particularly useful when needing to reset all timers to their default
     * states, such as at the end of a baking session, or when preparing the app for a new user
     * session. After executing this method, all timers will be set to their initial, default state.
     *
     * @param timerRepository The repository managing the main timer. It provides functionalities
     *                        to reset the main timer's data.
     * @param extraTimersCountdownRepo The repository managing the extra timers. It is used
     *                                 to reset the countdown-related data in all extra timers.
     */
    override fun clearResources(
        timerRepository: ITimerRepository,
        extraTimersCountdownRepo: IExtraTimersCountdownRepository
    ) {
        // Reset data in the main timer
        timerRepository.updateData(null)

        // Reset countdown-related in the extra timers, too.
        extraTimersCountdownRepo.clearDataInAllTimers()
    }
}

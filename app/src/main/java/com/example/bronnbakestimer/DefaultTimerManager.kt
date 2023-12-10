package com.example.bronnbakestimer

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
     * Clears the timers managed by the provided timer repository.
     * This includes clearing the main timer and any extra timers.
     *
     * @param timerRepository The repository managing the timers.
     */
    override fun clearResources(timerRepository: ITimerRepository) {
        timerRepository.updateData(null)
    }
}

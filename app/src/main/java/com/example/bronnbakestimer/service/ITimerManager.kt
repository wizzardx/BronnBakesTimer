package com.example.bronnbakestimer.service

import com.example.bronnbakestimer.repository.IExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.IMainTimerRepository
import com.example.bronnbakestimer.util.TimerUserInputDataId
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentHashMap

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
    fun resumeTimers(timerRepository: IMainTimerRepository)

    /**
     * Pauses the timers managed by the provided timer repository.
     *
     * @param timerRepository The repository managing the timers.
     */
    fun pauseTimers(timerRepository: IMainTimerRepository)

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
    fun clearResources(
        timerRepository: IMainTimerRepository,
        extraTimersCountdownRepo: IExtraTimersCountdownRepository,
    )

    /**
     * Initializes and starts all timers based on the user input for timer duration.
     *
     * This method is responsible for initializing the main timer and any additional extra timers
     * with the durations provided by the user. It validates and converts the user input into
     * millisecond values and sets up the timers accordingly. This method is essential for
     * starting the timer operations based on user-defined settings.
     *
     * After executing this method, the main timer and all extra timers will be initialized and
     * started with the specified durations.
     *
     * @param timerDurationInput A StateFlow representing the user's input for the main timer's duration.
     */
    fun initializeAndStartTimers(timerDurationInput: StateFlow<String>)

    /**
     * Processes and updates the countdown data for extra timers based on user inputs.
     *
     * This method takes a ConcurrentHashMap containing the current state of all extra timers and
     * updates it based on the latest user inputs. This includes validating user inputs, converting
     * them into the appropriate duration in milliseconds, and ensuring that the timers are set up
     * correctly. This function is crucial for maintaining the accuracy and relevance of extra
     * timers in response to user interactions and inputs.
     *
     * After executing this method, the countdown data for all extra timers will be updated
     * and synchronized with the latest user inputs.
     *
     * @param timerCountdownData A ConcurrentHashMap mapping TimerUserInputDataId to SingleTimerCountdownData,
     *                           representing the current countdown state of each extra timer.
     */
    fun processExtraTimers(timerCountdownData: ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>)
}

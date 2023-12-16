package com.example.bronnbakestimer.service

import com.example.bronnbakestimer.model.ExtraTimerUserInputData
import com.example.bronnbakestimer.repository.IExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.IExtraTimersUserInputsRepository
import com.example.bronnbakestimer.repository.IMainTimerRepository
import com.example.bronnbakestimer.util.InvalidTimerDurationException
import com.example.bronnbakestimer.util.TimerUserInputDataId
import com.example.bronnbakestimer.util.userInputToMillis
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages the operations of timers in the BronnBakesTimer application, including the main timer and extra timers.
 *
 * The `DefaultTimerManager` class encapsulates the logic for starting, pausing, resuming, and resetting timers,
 * as well as handling user inputs for timer configurations. It uses the `IMainTimerRepository` for managing the
 * main timer,`IExtraTimersCountdownRepository` for managing the countdowns of extra timers, and
 * `IExtraTimersUserInputsRepository` for managing user inputs related to extra timers.
 *
 * Key functionalities include:
 * - Resuming the operation of both the main timer and extra timers (`resumeTimers`).
 * - Pausing the operation of both the main timer and extra timers (`pauseTimers`).
 * - Resetting and clearing all timer-related data, bringing them back to their initial states (`clearResources`).
 * - Initializing and starting all timers based on user input (`initializeAndStartTimers`).
 * - Processing and updating the countdown data for all extra timers (`processExtraTimers`).
 * - Validating and processing individual extra timer data (`processIndividualExtraTimer`).
 * - Updating or creating new countdown data entries for extra timers (`updateCreateTimerCountdownData`).
 * - Validating and converting extra timer input into milliseconds (`validateConvertExtraTimerInput`).
 * - Filtering and retaining only the valid extra timers in the countdown data (`retainValidExtraTimers`).
 *
 * This class provides a cohesive and encapsulated way to manage timer functionalities, ensuring consistent
 * behavior and easy maintenance across the BronnBakesTimer application.
 */
class DefaultTimerManager(
    private val mainTimerRepository: IMainTimerRepository,
    private val extraTimersCountdownRepository: IExtraTimersCountdownRepository,
    private val extraTimersUserInputsRepository: IExtraTimersUserInputsRepository,
) : ITimerManager {
    /**
     * Resumes the timers managed by the provided timer repository.
     * This includes resuming the main timer and any extra timers.
     *
     * @param timerRepository The repository managing the timers.
     */
    override fun resumeTimers(timerRepository: IMainTimerRepository) {
        // Resuming the main timer resume the extra timers too.
        timerRepository.resumeTimer()
    }

    /**
     * Pauses the timers managed by the provided timer repository.
     * This includes pausing the main timer and any extra timers.
     *
     * @param timerRepository The repository managing the timers.
     */
    override fun pauseTimers(timerRepository: IMainTimerRepository) {
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
        timerRepository: IMainTimerRepository,
        extraTimersCountdownRepo: IExtraTimersCountdownRepository,
    ) {
        // Reset data in the main timer
        timerRepository.updateData(null)

        // Reset countdown-related in the extra timers, too.
        extraTimersCountdownRepo.clearDataInAllTimers()
    }

    override fun initializeAndStartTimers(timerDurationInput: StateFlow<String>) {
        // Validate main timer input and update the main timer data.
        val currentMainTimerMillis = validateConvertMainTimerInput(timerDurationInput)

        // Update the main timer in the repository.
        mainTimerRepository.updateData(
            TimerData(
                millisecondsRemaining = currentMainTimerMillis,
                isPaused = false,
                isFinished = false,
            ),
        )

        // Initialize a ConcurrentHashMap to hold the countdown data.
        val timerCountdownData = ConcurrentHashMap(extraTimersCountdownRepository.timerData.value)

        // Process extra timers: validate inputs, update or add timer data.
        processExtraTimers(timerCountdownData)

        // Save the updated extra timers data back to the repository.
        extraTimersCountdownRepository.updateData(timerCountdownData)
    }

    private fun validateConvertMainTimerInput(timerDurationInput: StateFlow<String>): Int {
        val maybeCurrentMainTimerMillis = userInputToMillis(timerDurationInput.value)
        if (maybeCurrentMainTimerMillis is Err) {
            throw InvalidTimerDurationException("Invalid timer duration input: ${maybeCurrentMainTimerMillis.error}")
        }
        return (maybeCurrentMainTimerMillis as Ok).value
    }

    override fun processExtraTimers(
        timerCountdownData: ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>,
    ) {
        // Remove extra timers not present in user inputs
        retainValidExtraTimers(timerCountdownData)

        // Update or add each extra timer's data after validation
        extraTimersUserInputsRepository.timerData.value.forEach { timer ->
            processIndividualExtraTimer(timer, timerCountdownData)
        }
    }

    // Processes an individual extra timer.
    //
    // Validates the timer's input, converts it to milliseconds, and updates or creates its countdown data.
    // If the input is invalid, an InvalidTimerDurationException is thrown.
    //
    // @param timer The extra timer data to process.
    // @param timerCountdownData The countdown data to update.
    //
    private fun processIndividualExtraTimer(
        timer: ExtraTimerUserInputData,
        timerCountdownData: ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>,
    ) {
        val currentExtraTimerMillis = validateConvertExtraTimerInput(timer)
        updateCreateTimerCountdownData(timer, currentExtraTimerMillis, timerCountdownData)
    }

    // Updates or creates a countdown data entry for an extra timer.
    //
    // @param timer The extra timer data.
    // @param millis The duration in milliseconds for the timer.
    // @param countdownData The ConcurrentHashMap holding the countdown data.
    private fun updateCreateTimerCountdownData(
        timer: ExtraTimerUserInputData,
        millis: Int,
        countdownData: ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>,
    ) {
        val timerCountdownDataEntry = countdownData[timer.id]
        if (timerCountdownDataEntry == null) {
            countdownData[timer.id] =
                SingleTimerCountdownData(
                    data =
                        TimerData(
                            millisecondsRemaining = millis,
                            isPaused = false,
                            isFinished = false,
                        ),
                    useInputTimerId = timer.id,
                )
        } else {
            countdownData[timer.id] =
                timerCountdownDataEntry.copy(
                    data =
                        timerCountdownDataEntry.data.copy(
                            millisecondsRemaining = millis,
                        ),
                )
        }
    }

    // Validates and converts an extra timer's input to milliseconds.
    // If the input is invalid, throws an InvalidTimerDurationException.
    //
    // @param timer The extra timer data to validate.
    // @return The duration in milliseconds if valid, or null if invalid.
    private fun validateConvertExtraTimerInput(timer: ExtraTimerUserInputData): Int {
        val maybeCurrentExtraTimerMillis = userInputToMillis(timer.inputs.timerDurationInput.value)
        if (maybeCurrentExtraTimerMillis is Err) {
            throw InvalidTimerDurationException("Invalid timer duration input: ${maybeCurrentExtraTimerMillis.error}")
        }
        return (maybeCurrentExtraTimerMillis as Ok).value
    }

    // Retains only the valid extra timers in the countdown data.
    //
    // This method filters the countdown data to include only those timers that are currently represented
    // in the user inputs.
    //
    // @param timerCountdownData The countdown data to filter.
    //
    private fun retainValidExtraTimers(
        timerCountdownData: ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>,
    ) {
        val userInputIds = extraTimersUserInputsRepository.timerData.value.map { it.id }.toSet()
        timerCountdownData.keys.retainAll(userInputIds)
    }
}

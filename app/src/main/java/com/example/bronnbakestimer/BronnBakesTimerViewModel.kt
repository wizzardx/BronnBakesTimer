package com.example.bronnbakestimer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

/**
 * ViewModel to help separate business logic from UI logic.
 */
class BronnBakesTimerViewModel(
    private val timerRepository: ITimerRepository,
    private val extraTimersRepository: IExtraTimersRepository,
) : ViewModel() {

    private val _timerMinutesInput = MutableStateFlow("5")

    /**
     * Input that we've received from the "Timer Minutes" TextField.
     */
    val timerMinutesInput: StateFlow<String> = _timerMinutesInput.asStateFlow()

    /**
     * Error message for the "Timer Minutes" TextField.
     */
    var timerMinutesInputError by mutableStateOf<String?>(null)

    /**
     * Represents the total time remaining as a state flow string. This property combines the timer data
     * from the timer repository and the user-inputted timer minutes. It formats and updates the total
     * time remaining, expressed as a string, whenever the timer data or the input minutes change.
     * The time is calculated and formatted using the formatTotalTimeRemainingString function.
     * Initially, the state is set to "Loading..." and will update as the timer data becomes available.
     */
    val totalTimeRemainingString: StateFlow<String> = timerRepository.timerData
        .combine(timerMinutesInput) { timerData, timerMinutesInput ->
            formatTotalTimeRemainingString(timerData, timerMinutesInput)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Loading...")

    /**
     * Updates the input value for the timer minutes.
     *
     * This function allows you to change the value of the timer minutes input. It takes a new value
     * as a parameter and updates the internal state, which is then reflected in the UI.
     *
     * @param newValue The new value for the timer minutes input, represented as a string.
     */
    fun updateTimerMinutesInput(newValue: String) {
        _timerMinutesInput.value = newValue
    }

    /**
     * Return true if the user can edit the text fields.
     */
    fun areTextInputControlsEnabled(timerData: TimerData?): Boolean = timerData == null

    private fun formatTotalTimeRemainingString(timerData: TimerData?, timerMinutesInput: String): String {
        val secondsRemaining = if (timerData == null) {
            userInputToSeconds(timerMinutesInput)
        } else {
            timerData.millisecondsRemaining / Constants.MillisecondsPerSecond
        }
        return formatMinSec(secondsRemaining)
    }

    /**
     * Computes and returns the total time remaining for an extra timer as a StateFlow of String.
     * This function maps the main timer data to calculate the remaining time for the extra timer.
     * It takes into consideration whether the main timer is active and adjusts the extra timer's
     * remaining time accordingly.
     *
     * @param timerData The data for the extra timer.
     * @return StateFlow<String> representing the remaining time for the extra timer.
     */
    fun extraTimerRemainingTime(timerData: ExtraTimerData): StateFlow<String> {
        return timerRepository.timerData
            .combine(timerData.inputs.timerMinutesInput) { mainTimerData, _ ->
                formatTotalTimeRemainingString(timerData, mainTimerData?.millisecondsRemaining)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, "Loading...")
    }

    private fun formatTotalTimeRemainingString(extraTimerData: ExtraTimerData, mainTimerMillis: Long?): String {
        val mainTimerActive = mainTimerMillis != null
        val secondsRemaining = extraTimerData.getTotalSeconds(mainTimerActive)
        return formatMinSec(secondsRemaining)
    }

    /**
     * Called when the user clicks the Start/Pause/Resume button.
     */
    @Suppress("TooGenericExceptionCaught")
    fun onButtonClick() {
        try {
            // Extract the current value from timerMinutesInput StateFlow
            val currentMainTimerMinutesInput = timerMinutesInput.value

            val timerData = timerRepository.timerData.value
            if (timerData == null) {
                // We're not running, so start up the timers.
                // First validate the inputs
                var errorsFound = false

                // Validate Main Timer Work seconds user input
                timerMinutesInputError = validateIntInput(currentMainTimerMinutesInput)
                errorsFound = errorsFound || timerMinutesInputError != null

                // Validate the Extra timers.
                extraTimersRepository.timerData.value.forEach { timerData2 ->
                    val inputs = timerData2.inputs
                    val currentExtraTimerMinutesInput = inputs.timerMinutesInput.value
                    inputs.timerMinutesInputError = validateIntInput(currentExtraTimerMinutesInput)

                    // Additional check for the extra timers: make sure their total time is
                    // less than or equal to the main timers:
                    if (inputs.timerMinutesInputError == null) {
                        val mainTimerSeconds = userInputToSeconds(currentMainTimerMinutesInput)
                        val extraTimerSeconds = currentExtraTimerMinutesInput.toLong() * Constants.SecondsPerMinute
                        if (extraTimerSeconds > mainTimerSeconds) {
                            inputs.timerMinutesInputError = "Extra timer time cannot be greater than main timer time."
                        }
                    }

                    errorsFound = errorsFound || inputs.timerMinutesInputError != null
                }

                // If all the validations passed then we can start up our timers.
                if (!errorsFound) {
                    // Start up the main timer:
                    timerRepository.updateData(
                        TimerData(
                            millisecondsRemaining =
                            userInputToSeconds(currentMainTimerMinutesInput) * Constants.MillisecondsPerSecond,
                            isPaused = false,
                            beepTriggered = false,
                            isFinished = false,
                        )
                    )

                    // And we can start up the regular timers, by setting their "remaining timer"
                    // fields based on the user inputs:
                    val extraTimers = extraTimersRepository.timerData.value.toMutableList()
                    extraTimers.forEachIndexed { index, timerData2 ->
                        val inputs = timerData2.inputs
                        val currentExtraTimerMinutesInput = inputs.timerMinutesInput.value
                        extraTimers[index] = timerData2.copy(
                            data = TimerData(
                                millisecondsRemaining =
                                currentExtraTimerMinutesInput.toLong() *
                                    Constants.MillisecondsPerSecond *
                                    Constants.SecondsPerMinute,
                                isPaused = false,
                                beepTriggered = false,
                                isFinished = false,
                            )
                        )
                    }
                    extraTimersRepository.updateData(extraTimers)
                }
            } else {
                // We're running, but are we currently paused?
                if (timerData.isPaused) {
                    // We are paused, so resume.
                    timerRepository.updateData(
                        timerData.copy(
                            isPaused = false,
                        )
                    )
                } else {
                    // We are not paused, so pause.
                    timerRepository.updateData(
                        timerData.copy(
                            isPaused = true,
                        )
                    )
                }
            }
        } catch (e: Exception) {
            logException(e)
        }
    }

    /**
     * Called when the user clicks the Reset button.
     */
    @Suppress("TooGenericExceptionCaught")
    fun onResetClick() {
        try {
            clearResources()
        } catch (e: Exception) {
            logException(e)
        }
    }

    private fun clearResources() {
        timerRepository.updateData(null)
    }

    /**
     * Handles the event when the "Add Timer" button is clicked. This function adds a new, empty timer
     * to the list of extra timers. It initializes the new timer with default values for the timer data
     * and inputs, ensuring that the timer is ready for user interaction and configuration.
     *
     * No parameters are required for this function.
     */
    fun onAddTimerClicked() {
        // Add a new empty timer to the list of timers
        val timerData = extraTimersRepository.timerData.value
        val newTimerData = timerData + ExtraTimerData(
            data = TimerData(
                millisecondsRemaining = 0,
                isPaused = false,
                beepTriggered = false,
                isFinished = false,
            ),
            inputs = ExtraTimerInputsData(),
        )
        extraTimersRepository.updateData(newTimerData)
    }

    /**
     * Handles the removal of a specific timer from the list of extra timers. This function is triggered
     * when the "Remove Timer" button is clicked. It identifies the timer to be removed by its unique ID
     * and updates the list of timers by filtering out the specified timer.
     *
     * @param id The unique UUID of the timer to be removed.
     */
    fun onRemoveTimerClicked(id: UUID) {
        // Remove timer with that index from our list of timers
        val timerData = extraTimersRepository.timerData.value
        val newTimerData = timerData.filter { it.id != id }
        extraTimersRepository.updateData(newTimerData)
    }
}

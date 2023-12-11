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
    private val timerManager: ITimerManager,
    private val inputValidator: IInputValidator,
    private val extraTimersRepository: IExtraTimersRepository,
    private val errorRepository: IErrorRepository,
    private val errorLoggerProvider: ErrorLoggerProvider,
) : ViewModel() {

    private val _timerDurationInput = MutableStateFlow("5")

    /**
     * Input that we've received from the "Timer Duration" TextField.
     */
    val timerDurationInput: StateFlow<String> = _timerDurationInput.asStateFlow()

    /**
     * Error message for the "Timer Duration" TextField.
     */
    var timerDurationInputError by mutableStateOf<String?>(null)

    /**
     * Represents the total time remaining as a state flow string. This property combines the timer data
     * from the timer repository and the user-inputted timer duration. It formats and updates the total
     * time remaining, expressed as a string, whenever the timer data or the input duration change.
     * The time is calculated and formatted using the formatTotalTimeRemainingString function.
     * Initially, the state is set to "Loading..." and will update as the timer data becomes available.
     */
    val totalTimeRemainingString: StateFlow<String> = timerRepository.timerData
        .combine(timerDurationInput) { timerData, timerDurationInput ->
            formatTotalTimeRemainingString(timerData, timerDurationInput)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Loading...")

    /**
     * Updates the input value for the timer duration.
     *
     * This function allows you to change the value of the timer duration input. It takes a new value
     * as a parameter and updates the internal state, which is then reflected in the UI.
     *
     * @param newValue The new value for the timer duration input, represented as a string.
     */
    fun updateTimerDurationInput(newValue: String) {
        _timerDurationInput.value = newValue
    }

    /**
     * Return true if the user can edit the text fields.
     */
    fun areTextInputControlsEnabled(timerData: TimerData?): Boolean = timerData == null

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
            .combine(timerData.inputs.timerDurationInput) { mainTimerData, _ ->
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
     * Handles the logic to be executed when the timer control button is clicked.
     *
     * This function performs different actions based on the current state of the main timer.
     * It checks the state of the main timer and decides whether to pause, resume, or start the timers.
     * The logic is as follows:
     * - If the main timer is currently paused, it resumes the timer.
     * - If the main timer is active (not paused and not null), it pauses the timer.
     * - If there is no active main timer (timer data is null), it attempts to start the timers, but only if the
     *   inputs are valid.
     *
     * In the case of exceptions during execution, the exception is caught and logged for troubleshooting.
     *
     * Note: This method should be linked to a UI button responsible for starting, pausing, and resuming timers.
     *
     * @throws Exception Captures and logs exceptions that occur within the method execution.
     */
    @Suppress("TooGenericExceptionCaught")
    fun onButtonClick() {
        try {
            val timerData = timerRepository.timerData.value
            when {
                timerData?.isPaused == true -> timerManager.resumeTimers(timerRepository)
                timerData != null -> timerManager.pauseTimers(timerRepository)
                else -> startTimersIfValid()
            }
        } catch (e: Exception) {
            logException(e, errorRepository, errorLoggerProvider)
        }
    }

    private fun startTimersIfValid() {
        val setTimerDurationInputError = { error: String ->
            timerDurationInputError = error
        }
        if (inputValidator.validateAllInputs(timerDurationInput, setTimerDurationInputError, extraTimersRepository)) {
            startTimers()
        }
    }

    private fun startTimers() {
        // Logic to start main and extra timers
        // Utilizes the validated inputs from validationResult
        // Assuming validationResult is valid, as it should be checked before calling this method

        val currentMainTimerMillis = userInputToMillis(timerDurationInput.value)
        timerRepository.updateData(
            TimerData(
                millisecondsRemaining = currentMainTimerMillis,
                isPaused = false,
                beepTriggered = false,
                isFinished = false,
            )
        )

        // Start all extra timers
        val updatedExtraTimers = extraTimersRepository.timerData.value.map { extraTimer ->
            val currentExtraTimerMillis = userInputToMillis(extraTimer.inputs.timerDurationInput.value)
            extraTimer.copy(data = extraTimer.data.copy(millisecondsRemaining = currentExtraTimerMillis))
        }

        extraTimersRepository.updateData(updatedExtraTimers)
    }

    /**
     * Handles the click event of the "Reset" button in the UI. This function is responsible for clearing
     * and resetting the resources associated with the timers. It is used to reset the timers to their
     * initial state, allowing the user to start over with fresh timer data.
     */
    @Suppress("TooGenericExceptionCaught")
    fun onResetClick() {
        try {
            timerManager.clearResources(timerRepository)
        } catch (e: Exception) {
            logException(e, errorRepository, errorLoggerProvider)
        }
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

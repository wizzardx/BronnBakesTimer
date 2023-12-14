package com.example.bronnbakestimer

import androidx.annotation.VisibleForTesting
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.ConcurrentHashMap

/**
 * ViewModel to help separate business logic from UI logic.
 */
open class BronnBakesTimerViewModel( // "open" for testing
    private val timerRepository: ITimerRepository,
    private val timerManager: ITimerManager,
    private val inputValidator: IInputValidator,
    private val extraTimersUserInputsRepository: IExtraTimersUserInputsRepository,
    private val extraTimersCountdownRepository: IExtraTimersCountdownRepository,
    private val errorRepository: IErrorRepository,
    private val errorLoggerProvider: IErrorLoggerProvider,
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
    val totalTimeRemainingString: StateFlow<String> = timerRepository.secondsRemaining
        .combine(timerDurationInput) { secondsRemaining, timerDurationInput ->
            formatTotalTimeRemainingString(secondsRemaining, timerDurationInput)
        }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Loading...")

    /**
     * A [StateFlow] representing whether configuration controls in the UI should be enabled.
     *
     * This property reflects the inverse of the timer's active state. When the timer is not running
     * (i.e., `null` or inactive), the configuration controls are enabled (i.e., this flow emits `true`),
     * allowing the user to interact with them. Conversely, when the timer is active, the controls are
     * disabled (this flow emits `false`), preventing any changes to the timer's configuration while it's running.
     *
     * The StateFlow is initialized to `true`, indicating that the controls are enabled by default.
     * It updates its value based on changes in the timer's state, as observed in `timerRepository.timerData`.
     */
    val configControlsEnabled: StateFlow<Boolean> = timerRepository.timerData
        .map { timerData ->
            timerData == null
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

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
     * Calculates and provides the remaining time for an extra timer as a StateFlow of String.
     *
     * This function computes the remaining time for a specified extra timer by considering the main timer's
     * remaining time and the duration set for the extra timer. It adjusts the extra timer's remaining time based
     * on the activity status of the main timer. The result is a StateFlow emitting the formatted time string,
     * reflecting the current state of the extra timer. This is particularly useful for updating the UI with the
     * latest remaining time for the extra timer.
     *
     * @param extraTimerUserInputData The data model for the extra timer, containing user input data.
     * @param extraTimerRemainingSeconds A StateFlow of Seconds indicating the remaining time for the extra timer.
     * @param timerDurationInput A StateFlow of String representing the duration input for the timer.
     * @param mainTimerSecondsRemaining The remaining seconds of the main timer, used to adjust the extra timer's time.
     * @return StateFlow<String> representing the formatted remaining time for the extra timer.
     */
    fun extraTimerRemainingTime(
        extraTimerUserInputData: ExtraTimerUserInputData,
        extraTimerRemainingSeconds: StateFlow<Seconds>,
        timerDurationInput: StateFlow<String>,
        mainTimerSecondsRemaining: Seconds?
    ): StateFlow<String> {
        fun formatTime(): String {
            return formatTotalTimeRemainingString(
                extraTimerRemainingSeconds = extraTimerRemainingSeconds,
                timerDurationInput = timerDurationInput,
                mainTimerSecondsRemaining = mainTimerSecondsRemaining,
            )
        }

        // Compute the initial state
        val initialState = formatTime()

        return timerRepository.timerData
            .combine(extraTimerUserInputData.inputs.timerDurationInput) { _, _ ->
                formatTime()
            }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Eagerly, initialState)
    }

    private fun formatTotalTimeRemainingString(
        extraTimerRemainingSeconds: StateFlow<Seconds>,
        timerDurationInput: StateFlow<String>,
        mainTimerSecondsRemaining: Seconds?
    ): String {
        val mainTimerActive = mainTimerSecondsRemaining != null
        val secondsRemaining = extraTimerRemainingSeconds.getTotalSeconds(mainTimerActive, timerDurationInput)
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

    /**
     * Starts the timers if the current timer duration input is valid.
     *
     * This function first validates the timer duration input using the `inputValidator`.
     * If the input is valid (as per the rules defined in the `inputValidator`), it proceeds to start the timers.
     * This involves updating the main timer's data in `timerRepository` with the current input and
     * setting up extra timers based on the user input data in `extraTimersUserInputsRepository`.
     *
     * If the input is invalid, the function sets an appropriate error message in `timerDurationInputError`,
     * and the timers are not started. This error message can then be displayed in the UI to inform the user.
     *
     * This function is typically invoked when the user attempts to start the timers, for instance,
     * by clicking a "Start" button in the UI.
     *
     * @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
     * The function is open for testing but private for other uses, ensuring encapsulation while allowing
     * for effective testing.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    open fun startTimersIfValid() { // Also, made "open" for testing
        val setTimerDurationInputError = { error: String ->
            timerDurationInputError = error
        }
        if (inputValidator.validateAllInputs(
                timerDurationInput,
                setTimerDurationInputError,
                extraTimersUserInputsRepository
            ) is ValidationResult.Valid
        ) {
            startTimers()
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun startTimers() {
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

        // Grab current countdown-related data for the timers:
        val timerCountdownData = ConcurrentHashMap(extraTimersCountdownRepository.timerData.value)

        // Get the current value of the timer user input data from the StateFlow
        val timerUserInputData = extraTimersUserInputsRepository.timerData.value

        // Create a set of IDs from the user input-related timer data
        val validIds = timerUserInputData.map { it.id }.toSet()

        // Iterate over the ConcurrentHashMap and remove entries not in validIds
        val iterator = timerCountdownData.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key !in validIds) {
                iterator.remove()
            }
        }

        // Update entries in the ConcurrentHashMap with the latest data from the user input-related timer data
        for (timer in timerUserInputData) {
            val currentExtraTimerMillis = userInputToMillis(timer.inputs.timerDurationInput.value)
            val timerCountdownDataEntry = timerCountdownData[timer.id]
            if (timerCountdownDataEntry == null) {
                timerCountdownData[timer.id] = SingleTimerCountdownData(
                    data = TimerData(
                        millisecondsRemaining = currentExtraTimerMillis,
                        isPaused = false,
                        beepTriggered = false,
                        isFinished = false,
                    ),
                    useInputTimerId = timer.id,
                )
            } else {
                timerCountdownData[timer.id] = timerCountdownDataEntry.copy(
                    data = timerCountdownDataEntry.data.copy(
                        millisecondsRemaining = currentExtraTimerMillis,
                    )
                )
            }
        }

        // Save the latest ConcurrentHashMap back to the repository:
        extraTimersCountdownRepository.updateData(timerCountdownData)
    }

    /**
     * Handles the click event of the "Reset" button in the UI. This function is responsible for clearing
     * and resetting the resources associated with the timers. It is used to reset the timers to their
     * initial state, allowing the user to start over with fresh timer data.
     */
    @Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
    fun onResetClick(testThrowingException: Boolean = false) {
        try {
            if (testThrowingException) {
                throw Exception("Test exception")
            }
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
        // Add a new empty set of user timer input data to the list:
        val timerData = extraTimersUserInputsRepository.timerData.value
        val newTimerData = timerData + ExtraTimerUserInputData()
        extraTimersUserInputsRepository.updateData(newTimerData)
    }

    /**
     * Handles the removal of a specific timer from the list of extra timers. This function is triggered
     * when the "Remove Timer" button is clicked. It identifies the timer to be removed by its unique ID
     * and updates the list of timers by filtering out the specified timer.
     *
     * @param id The unique UUID of the timer to be removed.
     */
    fun onRemoveTimerClicked(id: TimerUserInputDataId) {
        // Remove timer with that index from our list of timers
        val timerData = extraTimersUserInputsRepository.timerData.value
        val newTimerData = timerData.filter { it.id != id }
        extraTimersUserInputsRepository.updateData(newTimerData)
    }
}

/**
 * Calculates the total seconds remaining for a timer based on its current state and user input.
 *
 * This function determines the remaining time for a timer by considering whether the main timer is active.
 * If the main timer is active, it returns the current remaining seconds from this [StateFlow<Seconds>].
 * Otherwise, it calculates the remaining time based on the user's input provided in [timerDurationInput].
 *
 * @receiver StateFlow<Seconds> The StateFlow emitting the current remaining seconds of the timer.
 * @param mainTimerActive Boolean indicating whether the main timer is currently active.
 * @param timerDurationInput StateFlow<String> representing the user's input for the timer duration.
 * @return Seconds representing the total seconds remaining for the timer.
 */
fun StateFlow<Seconds>.getTotalSeconds(mainTimerActive: Boolean, timerDurationInput: StateFlow<String>): Seconds {
    return if (mainTimerActive) {
        // Timer is active, so use the current remaining time
        this.value
    } else {
        // Timer is not active, so use a value from the users input
        val userInput = timerDurationInput.value
        userInputToSeconds(userInput)
    }
}

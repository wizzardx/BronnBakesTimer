package com.example.bronnbakestimer.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bronnbakestimer.logic.IInputValidator
import com.example.bronnbakestimer.model.ExtraTimerUserInputData
import com.example.bronnbakestimer.provider.IErrorLoggerProvider
import com.example.bronnbakestimer.repository.IErrorRepository
import com.example.bronnbakestimer.repository.IExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.IExtraTimersUserInputsRepository
import com.example.bronnbakestimer.repository.ITimerRepository
import com.example.bronnbakestimer.service.ITimerManager
import com.example.bronnbakestimer.service.SingleTimerCountdownData
import com.example.bronnbakestimer.service.TimerData
import com.example.bronnbakestimer.util.InvalidTimerDurationException
import com.example.bronnbakestimer.util.Seconds
import com.example.bronnbakestimer.util.TimerUserInputDataId
import com.example.bronnbakestimer.util.formatMinSec
import com.example.bronnbakestimer.util.formatTotalTimeRemainingString
import com.example.bronnbakestimer.util.logException
import com.example.bronnbakestimer.util.userInputToMillis
import com.example.bronnbakestimer.util.userInputToSeconds
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * ViewModel to help separate business logic from UI logic.
 */
open class BronnBakesTimerViewModel(
    // "open" for testing
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
     * A `FocusRequester` instance for programmatically requesting focus on the "Timer Duration" input field.
     *
     * This object is used within the ViewModel to manage focus control in the UI. It can be called upon to
     * set the focus directly to the "Timer Duration" input field, ensuring a smoother user experience,
     * especially in scenarios where immediate user interaction with this field is desired.
     */
    @OptIn(ExperimentalFoundationApi::class)
    val timerDurationInputBringIntoViewRequester = BringIntoViewRequester()

    /**
     * A `BringIntoViewRequester` instance for programmatically scrolling the "Timer Duration" input field into view.
     *
     * This object is utilized in the ViewModel to control the visibility of the "Timer Duration" input field in the UI.
     * When invoked, it ensures that the input field is brought into the viewport of the device, making it visible
     * to the user without manual scrolling. This feature is particularly useful in forms or long pages where
     * the input field might not be immediately visible.
     */
    val timerDurationInputFocusRequester = FocusRequester()

    /**
     * Represents the total time remaining as a state flow string. This property combines the timer data
     * from the timer repository and the user-inputted timer duration. It formats and updates the total
     * time remaining, expressed as a string, whenever the timer data or the input duration change.
     * The time is calculated and formatted using the formatTotalTimeRemainingString function.
     * Initially, the state is set to "Loading..." and will update as the timer data becomes available.
     */
    val totalTimeRemainingString: StateFlow<String> = timerRepository.secondsRemaining
        .combine(timerDurationInput) { secondsRemaining, timerDurationInput ->
            val maybeFormattedString: Result<String, String> = try {
                formatTotalTimeRemainingString(secondsRemaining, timerDurationInput)
            } catch (e: IllegalArgumentException) {
                logException(e, errorRepository, errorLoggerProvider)
                Err("Invalid number")
            }
            if (maybeFormattedString is Ok) {
                maybeFormattedString.value
            } else {
                "Error: ${(maybeFormattedString as Err).error}"
            }
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
                else -> startTimersIfValid(skipUiLogic = false)
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
    open fun startTimersIfValid(skipUiLogic: Boolean) { // Also, made "open" for testing
        val setTimerDurationInputError = { error: String? ->
            timerDurationInputError = error
        }
        if (inputValidator.validateAllInputs(
                timerDurationInput,
                setTimerDurationInputError,
                extraTimersUserInputsRepository,
                this,
                viewModelScope,
                skipUiLogic,
            ) is Ok
        ) {
            startTimers()
        }
    }

    /**
     * Starts both the main timer and any extra timers with the current user inputs.
     *
     * This method initializes and starts the main timer and any additional extra timers based on the current
     * user input values. It updates the timer data in the timer repository for the main timer and adjusts the
     * countdown data for each extra timer accordingly. The method ensures that all timers are set up with their
     * respective durations, marked as un-paused, and ready to start the countdown.
     *
     * The main timer's duration is retrieved from the current value of the timer duration input, converted to
     * milliseconds, and then used to update the timer data in the timer repository. For extra timers, this
     * method iterates through each timer input data, converting the user input duration to milliseconds and
     * updating or adding the respective countdown data in a thread-safe manner using a ConcurrentHashMap.
     *
     * It is assumed that this method is called only after the timer inputs have been validated and are known to
     * be in a valid state. This method does not perform validation on its own.
     *
     * Note: This method modifies the state of the application's timers and should be used with caution. It is
     * primarily intended to be invoked when the user interacts with a UI element to start the timers.
     *
     * @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
     * The function is open for testing but private for other uses, ensuring encapsulation while allowing
     * for effective testing.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun startTimers() {
        // Logic to start main and extra timers
        val maybeCurrentMainTimerMillis = userInputToMillis(timerDurationInput.value)
        if (maybeCurrentMainTimerMillis is Err) {
            throw InvalidTimerDurationException("Invalid timer duration input: ${maybeCurrentMainTimerMillis.error}")
        }
        val currentMainTimerMillis: Int = (maybeCurrentMainTimerMillis as Ok).value

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
            val maybeCurrentExtraTimerMillis = userInputToMillis(timer.inputs.timerDurationInput.value)
            if (maybeCurrentExtraTimerMillis is Err) {
                throw InvalidTimerDurationException(
                    "Invalid timer duration input: ${maybeCurrentExtraTimerMillis.error}"
                )
            }
            val currentExtraTimerMillis = (maybeCurrentExtraTimerMillis as Ok).value
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

    /**
     * Focuses on the "Timer Duration" input field and brings it into view in the UI.
     *
     * This method uses `timerDurationInputFocusRequester` to programmatically set focus on the "Timer Duration" input
     * field, and `timerDurationInputBringIntoViewRequester` to scroll it into view if it's not currently visible. This
     * method is designed to enhance user interaction by automatically directing the user's attention to the required
     * input field.
     *
     * The method's execution is dependent on the provided `CoroutineScope` and can be conditionally skipped based on
     * the `skipUiLogic` flag, allowing flexibility in scenarios where automatic focus and scrolling are not necessary.
     *
     * @param coroutineScope The CoroutineScope within which the UI operations are executed.
     * @param skipUiLogic A Boolean flag that, when true, bypasses the UI logic for efficiency.
     */
    @OptIn(ExperimentalFoundationApi::class)
    fun focusOnTimerDurationInput(coroutineScope: CoroutineScope, skipUiLogic: Boolean) {
        if (skipUiLogic) return
        // Logic won't go here during unit tests, because this is some Android UI integration
        // that's hard to test. So, we'll just skip it during unit tests.
        coroutineScope.launch {
            timerDurationInputBringIntoViewRequester.bringIntoView()
            timerDurationInputFocusRequester.requestFocus()
        }
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
        val maybeSeconds = userInputToSeconds(userInput)
        if (maybeSeconds is Err) {
            // This shouldn't be possible (user's input input is validated), but just in case.
            return Seconds(0)
        }
        (maybeSeconds as Ok).value
    }
}

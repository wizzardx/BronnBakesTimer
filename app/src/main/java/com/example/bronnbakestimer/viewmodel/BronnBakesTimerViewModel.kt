package com.example.bronnbakestimer.viewmodel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bronnbakestimer.logic.IInputValidator
import com.example.bronnbakestimer.logic.ValidationParams
import com.example.bronnbakestimer.model.ExtraTimerUserInputData
import com.example.bronnbakestimer.provider.IErrorLoggerProvider
import com.example.bronnbakestimer.repository.IErrorRepository
import com.example.bronnbakestimer.repository.IExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.IExtraTimersUserInputsRepository
import com.example.bronnbakestimer.repository.IMainTimerRepository
import com.example.bronnbakestimer.service.ITimerManager
import com.example.bronnbakestimer.util.Seconds
import com.example.bronnbakestimer.util.TimerUserInputDataId
import com.example.bronnbakestimer.util.formatMinSec
import com.example.bronnbakestimer.util.formatTotalTimeRemainingString
import com.example.bronnbakestimer.util.logException
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

/**
 * ViewModel for managing the state and logic of the BronnBakesTimer application's single screen.
 *
 * This ViewModel acts as an intermediary between the UI and the underlying data models and business logic.
 * It manages the application's timer functionalities, including the main timer and additional timers,
 * user input validation, error handling, and UI interactions. The ViewModel encapsulates all the logic
 * required for the app's operation, making the UI code simpler and more maintainable.
 *
 * Responsibilities:
 * - Maintaining and updating the state of the main timer and extra timers.
 * - Validating user inputs for timer configurations.
 * - Managing focus and visibility of UI elements, particularly input fields.
 * - Coordinating actions in response to user interactions, such as starting, pausing, and resetting timers.
 * - Handling error reporting and logging.
 *
 * Additional notes regarding Suppressed lint warnings:
 *   - We might want to revisit the suppression notes again later, if this class grows a lot more
 *    in the future. For now, we're suppressing these warnings because we're okay with the number
 *    of functions and parameters in this class.
 */

@Suppress("TooManyFunctions", "LongParameterList")
open class BronnBakesTimerViewModel(
    // "open" for testing
    private val mainTimerRepository: IMainTimerRepository,
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
    val totalTimeRemainingString: StateFlow<String> =
        mainTimerRepository.secondsRemaining
            .combine(timerDurationInput) { secondsRemaining, timerDurationInput ->
                val maybeFormattedString: Result<String, String> =
                    try {
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
    val configControlsEnabled: StateFlow<Boolean> =
        mainTimerRepository.timerData
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
        mainTimerSecondsRemaining: Seconds?,
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

        return mainTimerRepository.timerData
            .combine(extraTimerUserInputData.inputs.timerDurationInput) { _, _ ->
                formatTime()
            }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Eagerly, initialState)
    }

    private fun formatTotalTimeRemainingString(
        extraTimerRemainingSeconds: StateFlow<Seconds>,
        timerDurationInput: StateFlow<String>,
        mainTimerSecondsRemaining: Seconds?,
    ): String {
        val mainTimerActive = mainTimerSecondsRemaining != null
        val secondsRemaining = extraTimerRemainingSeconds.getTotalSeconds(mainTimerActive, timerDurationInput)
        return formatMinSec(secondsRemaining)
    }

    /**
     * Handles the click event of the timer control button.
     * It delegates the action based on the current state of the main timer.
     */
    @Suppress("TooGenericExceptionCaught")
    fun onButtonClick() {
        try {
            // Retrieve the current state of the main timer
            val timerData = mainTimerRepository.timerData.value

            when {
                // Check if the timer is paused and resume it
                timerData?.isPaused == true -> resumeTimers()

                // Check if the timer is active (not paused and not null) and pause it
                timerData != null -> pauseTimers()

                // If no active main timer, attempt to start the timers if inputs are valid
                else -> startTimersIfValid(skipUiLogic = false)
            }
        } catch (e: Exception) {
            // Log any exceptions that occur during the button click processing
            logException(e, errorRepository, errorLoggerProvider)
        }
    }

    // Resumes the paused timers.
    // It calls the timerManager to resume the timers using the mainTimerRepository.
    private fun resumeTimers() {
        // Resume the timers through the timerManager
        timerManager.resumeTimers(mainTimerRepository)
    }

    // Pauses the currently active timers.
    // It calls the timerManager to pause the timers using the mainTimerRepository.
    private fun pauseTimers() {
        // Pause the timers through the timerManager
        timerManager.pauseTimers(mainTimerRepository)
    }

    /**
     * Starts the timers if the current timer duration input is valid.
     * It first validates the timer input and then, if valid, starts the timers.
     *
     * @param skipUiLogic Indicates whether to skip UI-related logic.
     */
    open fun startTimersIfValid(skipUiLogic: Boolean) {
        // Handling the result of the validation
        when (validateTimerInput(skipUiLogic)) {
            is Ok -> {
                initializeAndStartTimers()
            }
            is Err -> {
                // We did validation failure handling within the validateTimerInput function,
                // including setting flags on UI controls there, so we don't need to do anything
                // here.
            }
        }
    }

    /**
     * Validates the timer duration input.
     *
     * @param skipUiLogic Indicates whether to skip UI-related logic.
     * @return Result indicating whether the validation was successful or not.
     */
    @Suppress("CommentOverPrivateFunction")
    private fun validateTimerInput(skipUiLogic: Boolean): Result<Unit, String> {
        // Validate the timer input and obtain the result
        return inputValidator.validateAllInputs(
            ValidationParams(
                timerDurationInput = timerDurationInput,
                setTimerDurationInputError = ::timerDurationInputError,
                extraTimersUserInputsRepository = extraTimersUserInputsRepository,
                viewModel = this,
                coroutineScope = viewModelScope,
            ),
            skipUiLogic,
        )
    }

    /**
     * Initializes and starts the timers based on the validated input.
     */
    @Suppress("CommentOverPrivateFunction")
    private fun initializeAndStartTimers() {
        // Initialize and start the timers with the current timer duration input
        timerManager.initializeAndStartTimers(timerDurationInput)
    }

    // Sets the error message for the timer duration input.
    //
    // This function updates the `timerDurationInputError` with the provided error message.
    // It abstracts the UI logic of setting an error, making the `startTimersIfValid` function more focused.
    //
    // @param error The error message to set. If null, it indicates no error.
    //
    private fun timerDurationInputError(error: String?) {
        timerDurationInputError = error
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
            timerManager.clearResources(mainTimerRepository, extraTimersCountdownRepository)
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
    fun focusOnTimerDurationInput(
        coroutineScope: CoroutineScope,
        skipUiLogic: Boolean,
    ) {
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
fun StateFlow<Seconds>.getTotalSeconds(
    mainTimerActive: Boolean,
    timerDurationInput: StateFlow<String>,
): Seconds {
    return if (mainTimerActive) {
        // Timer is active, so use the current remaining time
        this.value
    } else {
        // Timer is not active, so use a value from the users input
        val userInput = timerDurationInput.value
        val maybeSeconds = userInputToSeconds(userInput, checkRange = false)
        if (maybeSeconds is Err) {
            // This shouldn't be possible (user's input input is validated), but just in case.
            return Seconds(0)
        }
        (maybeSeconds as Ok).value
    }
}

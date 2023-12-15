package com.example.bronnbakestimer.model

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the data and functionalities associated with extra timer inputs in the BronnBakesTimer application.
 *
 * This class encapsulates the state and behavior of additional timer inputs, including their respective values and
 * error messages. It primarily manages two types of inputs: timer duration and timer name, each with associated
 * StateFlows for maintaining their current values and mutable states for error messages. These inputs are initialized
 * with default values ("5" for timer duration and "check/flip/stir" for timer name).
 *
 * The class provides functions to update these inputs and handles the focus behavior on the timer duration input field
 * within the UI, leveraging CoroutineScope for asynchronous operations. It employs `BringIntoViewRequester` and
 * `FocusRequester` for managing UI interactions, such as bringing the timer duration input into view and requesting
 * focus on it.
 *
 * Key functionalities include:
 * - Managing state and updates for timer duration and name inputs.
 * - Handling UI interactions like focusing on and bringing the timer duration input into view.
 * - Offering a clear interface for updating input values and managing related error messages.
 *
 * Note: This class is marked with `@OptIn(ExperimentalFoundationApi::class)` indicating the use of experimental
 * APIs in Compose Foundation, specifically for UI focus and view adjustments.
 */
class ExtraTimerInputsData {

    private val _timerDurationInput = MutableStateFlow("5") // Initialize with default value

    /**
     * The input value for timer duration, initialized to "5".
     */
    val timerDurationInput: StateFlow<String> = _timerDurationInput.asStateFlow()

    /**
     * Error message for the timer duration input, to be shown in the UI if there's an error.
     */
    var timerDurationInputError by mutableStateOf<String?>(null)

    // MutableStateFlow to hold the user input value for the timer name.
    // This state flow is initialized with the default value "check/flip/stir".
    private val _timerNameInput = MutableStateFlow("check/flip/stir")

    /**
     * The input value for timer name, initialized to "check/flip/stir".
     */
    val timerNameInput: StateFlow<String> = _timerNameInput.asStateFlow()

    /**
     * Error message for the timer name input, to be shown in the UI if there's an error.
     */
    var timerNameInputError by mutableStateOf<String?>(null)

    /**
     * A requester for bringing the timer duration input into view in the UI.
     *
     * This instance of `BringIntoViewRequester` is used to programmatically scroll the UI to ensure that the timer
     * duration input field is visible to the user. It's particularly useful in scenarios where the input field might
     * be off-screen due to user interactions or layout changes.
     */
    @OptIn(ExperimentalFoundationApi::class)
    val timerDurationInputBringIntoViewRequester = BringIntoViewRequester()

    /**
     * A requester for setting focus on the timer duration input in the UI.
     *
     * This `FocusRequester` instance is employed to programmatically set the focus on the timer duration input field.
     * It's essential for enhancing user experience by allowing automatic focus transitions in response to certain user
     * actions or UI events.
     */
    val timerDurationInputFocusRequester = FocusRequester()

    /**
     * Updates the input value for timer duration.
     *
     * This function allows you to change the value of the timer duration input.
     *
     * @param newValue The new value for the timer duration input.
     */
    fun updateTimerDurationInput(newValue: String) {
        _timerDurationInput.value = newValue
    }

    /**
     * Updates the input value for timer name.
     *
     * @param newValue The new value for the timer name input.
     */
    fun updateTimerNameInput(newValue: String) {
        _timerNameInput.value = newValue
    }

    /**
     * Requests focus and brings the timer duration input into view in the UI.
     *
     * This function leverages `timerDurationInputBringIntoViewRequester` and `timerDurationInputFocusRequester` to
     * bring the timer duration input field into view and set focus on it. The function operates within a given
     * `CoroutineScope` to perform these actions asynchronously.
     *
     * It also provides an option to skip these UI interactions based on the `skipUiLogic` flag, allowing flexibility
     * in scenarios where such interactions are not needed.
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

    // TODO: Some validations during construction
}

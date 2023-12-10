package com.example.bronnbakestimer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the input state for an extra timer, including the timer name and duration.
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
}

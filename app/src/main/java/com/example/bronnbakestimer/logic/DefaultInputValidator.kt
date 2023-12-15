package com.example.bronnbakestimer.logic

import com.example.bronnbakestimer.model.ExtraTimerUserInputData
import com.example.bronnbakestimer.repository.IExtraTimersUserInputsRepository
import com.example.bronnbakestimer.util.Seconds
import com.example.bronnbakestimer.util.userInputToSeconds
import com.example.bronnbakestimer.viewmodel.BronnBakesTimerViewModel
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Implements the IInputValidator interface to provide input validation functionality for timer-related inputs.
 *
 * The DefaultInputValidator class is responsible for validating user inputs specific to timer durations in a timer
 * application. This includes validations for both the main timer and any additional extra timers a user may set. The
 * class ensures that the inputs adhere to certain rules, such as being valid integers and falling within specified
 * bounds. It also compares the durations of extra timers against the main timer's duration to maintain logical
 * consistency in the application.
 *
 * Primary functionalities include:
 * - Validating the main timer's duration input, ensuring it's a valid integer and setting an error message if it's not.
 * - Validating each extra timer's duration input based on the main timer's validated result, ensuring each extra
 *    timer's duration is less than the main timer's duration and within acceptable bounds.
 * - Aggregating the validation results of all extra timers and the main timer to provide a comprehensive validation
 *   status.
 *
 * Methods:
 * - validateMainTimerUserInputs: Validates the main timer's duration input.
 * - validateExtraTimerUserInputs: Validates a single extra timer's duration input.
 * - validateExtraTimersUserInputs: Validates all extra timers' duration inputs.
 * - validateAllInputs: Validates all timer inputs (main and extra timers) and aggregates their validation results.
 *
 * Usage:
 * This class is used in the context of a timer application where multiple timers (main and extra) may be set by the
 * user. It ensures that all user inputs for these timers are valid and logically consistent before they are processed
 * by the application.
 */
class DefaultInputValidator : IInputValidator {

    override fun validateAllInputs(
        timerDurationInput: StateFlow<String>,
        setTimerDurationInputError: (String?) -> Unit,
        extraTimersUserInputsRepository: IExtraTimersUserInputsRepository,
        viewModel: BronnBakesTimerViewModel,
        coroutineScope: CoroutineScope,
        skipUiLogic: Boolean,
    ): Result<Unit, String> {
        // Clear out errors in the UI
        setTimerDurationInputError(null)
        extraTimersUserInputsRepository.timerData.value.forEach {
            it.inputs.timerDurationInputError = null
        }

        // Initialize a variable for the result
        var validationResult: Result<Unit, String> = Ok(Unit)

        // Get the main timer duration in seconds and check for errors
        val maybeMainTimerSeconds = userInputToSeconds(timerDurationInput.value, checkRange = true)
        if (maybeMainTimerSeconds is Err) {
            setTimerDurationInputError(maybeMainTimerSeconds.error)
            viewModel.focusOnTimerDurationInput(coroutineScope, skipUiLogic)
            validationResult = maybeMainTimerSeconds.map { }
        } else {
            // Validate extra timers only if main timer validation passed
            for (extraTimer in extraTimersUserInputsRepository.timerData.value) {
                val validateResult = validateExtraTimerInputs(extraTimer, maybeMainTimerSeconds)
                if (validateResult is Err) {
                    extraTimer.inputs.focusOnTimerDurationInput(coroutineScope, skipUiLogic)
                    validationResult = validateResult
                    break // Exit the loop if an error is found
                }
            }
        }

        // Return the final validation result
        return validationResult
    }

    private fun validateExtraTimerInputs(
        extraTimer: ExtraTimerUserInputData,
        maybeMainTimerSeconds: Result<Seconds, String>
    ): Result<Unit, String> {
        // Initialize a variable for holding the function's result
        var validationResult: Result<Unit, String> = Ok(Unit)

        // Parse the extra timer duration input as an integer
        val maybeExtraTimerSeconds = userInputToSeconds(extraTimer.inputs.timerDurationInput.value, checkRange = true)

        // Handle parsing failure
        if (maybeExtraTimerSeconds is Err) {
            extraTimer.inputs.timerDurationInputError = maybeExtraTimerSeconds.error
            validationResult = maybeExtraTimerSeconds
        } else if (maybeMainTimerSeconds is Ok) {
            // Check if the extra timer duration is greater than the main timer duration
            val extraTimerSeconds = (maybeExtraTimerSeconds as Ok).value
            if (extraTimerSeconds > maybeMainTimerSeconds.value) {
                val msg = "Extra timer time cannot be greater than main timer time."
                extraTimer.inputs.timerDurationInputError = msg
                validationResult = Err(msg)
            }
        }

        // Return the validation result
        return validationResult
    }
}

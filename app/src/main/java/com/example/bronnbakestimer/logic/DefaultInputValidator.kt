package com.example.bronnbakestimer.logic

import com.example.bronnbakestimer.model.ExtraTimerUserInputData
import com.example.bronnbakestimer.util.Seconds
import com.example.bronnbakestimer.util.userInputToSeconds
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result

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
        params: ValidationParams,
        skipUiLogic: Boolean,
    ): Result<Unit, String> {
        // Clear any previously set error messages for the timer inputs
        params.setTimerDurationInputError(null)
        params.extraTimersUserInputsRepository.clearExtraTimerErrors()

        var focusedOnControl = false // Flag to track if any input control has been focused
        var finalResult: Result<Unit, String> = Ok(Unit) // Initialize the final result as successful

        // Validate the main timer's duration input
        val maybeMainTimerSeconds = userInputToSeconds(params.timerDurationInput.value, checkRange = true)
        if (maybeMainTimerSeconds is Err) {
            // Set an error for the main timer duration input
            params.setTimerDurationInputError(maybeMainTimerSeconds.error)

            // Sanity check: By this point in the logic, no control should have been focused yet
            check(!focusedOnControl) { "A control should not have been focused yet." }

            // Focus on the main timer duration input
            params.viewModel.focusOnTimerDurationInput(params.coroutineScope, skipUiLogic)
            focusedOnControl = true // Mark that a control has been focused

            finalResult = maybeMainTimerSeconds // Update the final result with the error
        }

        // Iterate through and validate each extra timer input
        for (extraTimer in params.extraTimersUserInputsRepository.timerData.value) {
            val result =
                validateExtraTimerInputs(
                    extraTimer,
                    maybeMainTimerSeconds,
                    { extraTimer.inputs.focusOnTimerDurationInput(params.coroutineScope, skipUiLogic) },
                    { extraTimer.inputs.focusOnTimerNameInput(params.coroutineScope, skipUiLogic) },
                    focusedOnControl,
                )
            if (result is Err) {
                focusedOnControl = true // Update focus tracking if validation fails

                // Update the final result with the first encountered error
                if (finalResult is Ok) {
                    finalResult = result
                }
            }
        }

        return finalResult // Return the final aggregated validation result
    }

    private fun validateExtraTimerInputs(
        extraTimer: ExtraTimerUserInputData,
        maybeMainTimerSeconds: Result<Seconds, String>,
        focusOnTimerDurationInput: () -> Unit,
        focusOnTimerNameInput: () -> Unit,
        alreadyFocusedOnControl: Boolean,
    ): Result<Unit, String> {
        // Initialize the final result as successful:
        var finalResult: Result<Unit, String> = Ok(Unit)

        // Convert user input for timer duration into seconds and check for errors
        val timerDurationResult = userInputToSeconds(extraTimer.inputs.timerDurationInput.value, checkRange = true)

        // Validate timer duration
        if (timerDurationResult is Err) {
            // Set error message for timer duration
            extraTimer.inputs.timerDurationInputError = timerDurationResult.error

            // Focus on timer duration input if no control has been focused yet
            if (!alreadyFocusedOnControl) {
                focusOnTimerDurationInput()
            }

            // Update final result with the error
            finalResult = Err(timerDurationResult.error)
        } else if (maybeMainTimerSeconds is Ok &&
            timerDurationResult is Ok &&
            timerDurationResult.value > maybeMainTimerSeconds.value
        ) {
            // Check if extra timer's duration is greater than the main timer's duration
            val errorMsg = "Extra timer time cannot be greater than main timer time."
            extraTimer.inputs.timerDurationInputError = errorMsg

            // Focus on timer duration input if no control has been focused yet
            if (!alreadyFocusedOnControl) {
                focusOnTimerDurationInput()
            }

            // Update final result with the error
            finalResult = Err(errorMsg)
        }

        // Validate timer name
        if (extraTimer.inputs.timerNameInput.value.isBlank()) {
            // Set error message for timer name
            val errorMsg = "Extra timer name cannot be blank."
            extraTimer.inputs.timerNameInputError = errorMsg

            // Focus on timer name input if no control has been focused yet and no previous errors
            if (!alreadyFocusedOnControl && finalResult is Ok) {
                focusOnTimerNameInput()
            }

            // Update final result with the error
            finalResult = Err(errorMsg)
        }

        return finalResult // Return the final validation result
    }
}

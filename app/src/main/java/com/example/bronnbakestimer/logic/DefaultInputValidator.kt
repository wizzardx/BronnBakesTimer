package com.example.bronnbakestimer.logic

import com.example.bronnbakestimer.model.ExtraTimerUserInputData
import com.example.bronnbakestimer.repository.IExtraTimersUserInputsRepository
import com.example.bronnbakestimer.util.Seconds
import com.example.bronnbakestimer.util.userInputToSeconds
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
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

    /**
     * Validates all input parameters and returns a Result indicating success or failure.
     *
     * @param timerDurationInput The main timer duration input as a StateFlow of String.
     * @param setTimerDurationInputError A function to set the timer duration input error message.
     * @param extraTimersUserInputsRepository The repository for extra timer inputs.
     * @return A Result indicating success (Ok) or failure (Err) with an error message.
     */
    override fun validateAllInputs(
        timerDurationInput: StateFlow<String>,
        setTimerDurationInputError: (String?) -> Unit,
        extraTimersUserInputsRepository: IExtraTimersUserInputsRepository
    ): Result<Unit, String> {
        // Clear out errors in the UI:
        setTimerDurationInputError(null)
        extraTimersUserInputsRepository.timerData.value.forEach {
            it.inputs.timerDurationInputError = null
        }

        // Get the main timer duration in seconds
        val maybeMainTimerSeconds: Result<Seconds, String> = userInputToSeconds(timerDurationInput.value)

        // Convert maybeSeconds from Result<Seconds, String> to  Result<Unit, String>
        var result: Result<Unit, String> = maybeMainTimerSeconds.map { }

        // Update user input error feedback label string:
        if (result is Err) {
            setTimerDurationInputError(result.error)
        } else {
            setTimerDurationInputError(null)
        }
        // Validate and process extra timers
        for (extraTimer in extraTimersUserInputsRepository.timerData.value) {
            val validateResult: Result<Unit, String> = validateExtraTimerInputs(extraTimer, maybeMainTimerSeconds)
            if (validateResult is Err && result is Ok) {
                result = validateResult
            }
        }

        // Return the result that we were building up until now:
        return result
    }

    @Suppress("ReturnCount") // TODO: After I get good unit test coverage, then I can try to remove this suppression
    private fun validateExtraTimerInputs(
        extraTimer: ExtraTimerUserInputData,
        maybeMainTimerSeconds: Result<Seconds, String>
    ): Result<Unit, String> {
        // Attempt to parse the extra timer duration input as an integer
        val maybeExtraTimerSeconds: Result<Seconds, String> =
            userInputToSeconds(extraTimer.inputs.timerDurationInput.value)
        // If the parsing failed then set the error message and set the result to an error if it wasn't already:
        if (maybeExtraTimerSeconds is Err) {
            val msg = maybeExtraTimerSeconds.error
            extraTimer.inputs.timerDurationInputError = msg
            return Err(msg)
        }

        // Check if the extra timer duration is greater than the main timer duration
        if (maybeMainTimerSeconds is Ok && (maybeExtraTimerSeconds as Ok).value > maybeMainTimerSeconds.value) {
            val msg = "Extra timer time cannot be greater than main timer time."
            extraTimer.inputs.timerDurationInputError = msg
            return Err(msg)
        }

        // If we got here then there were no validation errors
        return Ok(Unit)
    }
}

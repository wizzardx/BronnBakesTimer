package com.example.bronnbakestimer

import kotlinx.coroutines.flow.StateFlow

/**
 * Default implementation of the IInputValidator interface.
 * This class provides the functionality to validate inputs related to timers.
 */
class DefaultInputValidator : IInputValidator {
    /**
     * Validates all input fields related to timers and returns ValidationResult.Valid if there are no errors.
     *
     * This function performs validation on various input fields related to timers, including the main timer duration
     * input and extra timers' duration inputs. It checks for errors in each input field and updates error messages
     * accordingly. If any validation error is found, it returns ValidationResult.Invalid, indicating that there are
     * errors. Otherwise, it returns ValidationResult.Valid.
     *
     * @param timerDurationInput The state flow representing the main timer duration input.
     * @param setTimerDurationInputError A function to set an error message for the main timer duration input.
     * @param extraTimersRepository The repository containing data for extra timers.
     * @return ValidationResult.Valid if all inputs are valid; ValidationResult.Invalid if there are validation errors.
     */
    override fun validateAllInputs(
        timerDurationInput: StateFlow<String>,
        setTimerDurationInputError: (String) -> Unit,
        extraTimersRepository: IExtraTimersRepository,
    ): ValidationResult {
        val mainTimerDuration = timerDurationInput.value.toIntOrNull() ?: 0

        // Validate the main timer input and set the error message if invalid
        val mainTimerValidationResult = validateIntInput(timerDurationInput.value)
        if (mainTimerValidationResult is ValidationResult.Invalid) {
            setTimerDurationInputError(mainTimerValidationResult.reason)
        }

        val extraTimerValidationResults = extraTimersRepository.timerData.value.map { extraTimer ->
            val extraTimerDuration = extraTimer.inputs.timerDurationInput.value.toIntOrNull() ?: 0
            val extraTimerValidationResult = validateIntInput(extraTimer.inputs.timerDurationInput.value)

            if (!mainTimerValidationResult.isInvalid && extraTimerDuration > mainTimerDuration) {
                extraTimer.inputs.timerDurationInputError = "Extra timer time cannot be " +
                    "greater than main timer time."
                ValidationResult.Invalid("Extra timer time cannot be greater than main timer time.")
            } else if (extraTimerValidationResult is ValidationResult.Invalid) {
                extraTimer.inputs.timerDurationInputError = extraTimerValidationResult.reason
                ValidationResult.Invalid(extraTimerValidationResult.reason)
            } else {
                ValidationResult.Valid
            }
        }

        val allValidationResults = listOfNotNull(mainTimerValidationResult) + extraTimerValidationResults

        return if (allValidationResults.any { it is ValidationResult.Invalid }) {
            ValidationResult.Invalid("Validation failed")
        } else {
            ValidationResult.Valid
        }
    }
}

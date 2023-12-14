package com.example.bronnbakestimer.logic

import com.example.bronnbakestimer.repository.IExtraTimersUserInputsRepository
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.StateFlow

/**
 * An interface for validating user inputs.
 *
 * This interface defines a single method, validateAllInputs, which is responsible for validating
 * the user's inputs for the timer duration and extra timers. The method takes three parameters:
 * a StateFlow representing the user's input for the timer duration, a function for setting the error
 * message for the timer duration input, and an IExtraTimersRepository instance for accessing extra timer data.
 *
 * The validateAllInputs method returns a ValidationResult, which can be either ValidationResult.Valid if the inputs
 * are valid, or ValidationResult.Invalid with a reason for the invalidity if the inputs are invalid.
 *
 * Implementations of this interface should provide specific validation logic for the inputs.
 */
fun interface IInputValidator {

    /**
     * Validates all inputs related to timers and returns a ValidationResult.
     *
     * This method validates the main timer duration input and all extra timer duration inputs. It uses the provided
     * `setTimerDurationInputError` function to set an error message for the main timer duration input if it's invalid.
     * It also sets an error message for each invalid extra timer duration input.
     *
     * The method returns a ValidationResult. If all inputs are valid, it returns ValidationResult.Valid. If any input
     * is invalid, it returns ValidationResult.Invalid with a reason for the invalidity.
     *
     * @param timerDurationInput A StateFlow representing the user's input for the main timer duration.
     * @param setTimerDurationInputError A function that takes a string and sets it as the error message for the main
     *                                   timer duration input. If the string is null, it clears the error message.
     * @param extraTimersUserInputsRepository An IExtraTimersRepository instance for accessing extra timer data.
     * @return A ValidationResult representing the result of the validation. If all inputs are valid, it returns
     *         ValidationResult.Valid. If any input is invalid, it returns ValidationResult.Invalid with a reason for
     *         the invalidity.
     */
    fun validateAllInputs(
        timerDurationInput: StateFlow<String>,
        setTimerDurationInputError: (String?) -> Unit,
        extraTimersUserInputsRepository: IExtraTimersUserInputsRepository,
    ): Result<Unit, String>
}

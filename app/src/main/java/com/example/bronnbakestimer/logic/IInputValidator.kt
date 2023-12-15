package com.example.bronnbakestimer.logic

import com.example.bronnbakestimer.repository.IExtraTimersUserInputsRepository
import com.example.bronnbakestimer.viewmodel.BronnBakesTimerViewModel
import com.github.michaelbull.result.Result
import kotlinx.coroutines.CoroutineScope
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
     * This method is responsible for the comprehensive validation of user inputs associated with the main timer
     * duration and any additional timer durations. The validation process includes assessing the validity of the main
     * timer duration input, as well as verifying each extra timer duration input obtained from the
     * extraTimersUserInputsRepository.
     *
     * During the validation process, the `setTimerDurationInputError` function is utilized to assign an error message
     * to the main timer duration input in cases where the input is deemed invalid. Similarly, error messages are set
     * for each invalid extra timer input.
     *
     * The method operates within a CoroutineScope and can optionally bypass certain UI logic based on the skipUiLogic
     * flag. This flexibility allows for more efficient processing in scenarios where UI interaction is not necessary.
     *
     * The outcome of the validation is encapsulated in a Result object. If all inputs are validated successfully, a
     * Result of type 'Valid' is returned. Conversely, if any of the inputs are invalid, a Result of type 'Invalid' is
     * returned, including the reason for the invalidity.
     *
     * @param timerDurationInput A StateFlow representing the user's input for the main timer duration.
     * @param setTimerDurationInputError A function that takes a string and sets it as the error message for the main
     *                                   timer duration input. If the string is null, it clears the error message.
     * @param extraTimersUserInputsRepository An IExtraTimersRepository instance for accessing extra timer data.
     * @param viewModel A BronnBakesTimerViewModel instance for maintaining view model state.
     * @param coroutineScope A CoroutineScope within which the validation logic is executed.
     * @param skipUiLogic A Boolean flag that, when true, bypasses certain UI-related logic for more efficient
     *                    processing.
     * @return A Result encapsulating the validation outcome. It returns Result.Success<Unit> if all inputs are valid,
     *         or Result.Failure<String> with a reason for the invalidity if any input is invalid.
     */
    fun validateAllInputs(
        timerDurationInput: StateFlow<String>,
        setTimerDurationInputError: (String?) -> Unit,
        extraTimersUserInputsRepository: IExtraTimersUserInputsRepository,
        viewModel: BronnBakesTimerViewModel,
        coroutineScope: CoroutineScope,
        skipUiLogic: Boolean,
    ): Result<Unit, String>
}

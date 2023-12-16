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
     * This method performs comprehensive validation of user inputs associated with both the main timer
     * duration and any additional timer durations. It utilizes the ValidationParams object to access
     * necessary data and functions for the validation process. The method also takes a 'skipUiLogic' flag
     * that allows bypassing certain UI-related logic when set to true.
     *
     * The validation process includes checking the validity of the main timer duration input and verifying
     * each extra timer duration input obtained from the extraTimersUserInputsRepository. Error messages
     * are set accordingly for each invalid input using the provided functions in ValidationParams.
     *
     * The method operates within the provided CoroutineScope and returns a Result object. If all inputs
     * are validated successfully, it returns Result.Success<Unit>. If any input is invalid, it returns
     * Result.Failure<String> with a reason for the invalidity.
     *
     * @param params The ValidationParams object containing necessary data and functions for validation.
     * @param skipUiLogic A Boolean flag to optionally bypass certain UI logic for efficiency.
     * @return A Result encapsulating the validation outcome. It either returns Result.Success<Unit> for
     *         valid inputs or Result.Failure<String> with a reason for invalidity.
     */
    fun validateAllInputs(
        params: ValidationParams,
        skipUiLogic: Boolean,
    ): Result<Unit, String>
}

/**
 * Data class encapsulating parameters required for validating timer inputs.
 *
 * This class groups together parameters that are commonly used in the validation of timer inputs,
 * including the main timer duration and additional timer inputs. It simplifies method signatures
 * by reducing the number of parameters passed, enhancing code readability and maintainability.
 *
 * @property timerDurationInput A StateFlow representing the user's input for the main timer duration.
 * @property setTimerDurationInputError A function for setting the error message for the main timer duration input.
 * @property extraTimersUserInputsRepository An instance of IExtraTimersUserInputsRepository for accessing extra timer
 *                                           data.
 * @property viewModel A BronnBakesTimerViewModel instance for maintaining view model state.
 * @property coroutineScope A CoroutineScope within which the validation logic is executed.
 */
data class ValidationParams(
    val timerDurationInput: StateFlow<String>,
    val setTimerDurationInputError: (String?) -> Unit,
    val extraTimersUserInputsRepository: IExtraTimersUserInputsRepository,
    val viewModel: BronnBakesTimerViewModel,
    val coroutineScope: CoroutineScope,
)

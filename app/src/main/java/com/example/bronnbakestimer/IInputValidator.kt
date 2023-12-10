package com.example.bronnbakestimer

import kotlinx.coroutines.flow.StateFlow

/**
 * Default implementation of the IInputValidator interface.
 * This class provides the functionality to validate inputs related to timers.
 */
interface IInputValidator {
    /**
     * Validates all input fields related to timers and returns true if there are no errors.
     *
     * This function performs validation on various input fields related to timers, including the main timer duration
     * input and extra timers' duration inputs. It checks for errors in each input field and updates error messages
     * accordingly. If any validation error is found, it returns false, indicating that there are errors. Otherwise,
     * it returns true.
     *
     * @param timerDurationInput The state flow representing the main timer duration input.
     * @param setTimerDurationInputError A function to set an error message for the main timer duration input.
     * @param extraTimersRepository The repository containing data for extra timers.
     * @return True if all inputs are valid; false if there are validation errors.
     */
    fun validateAllInputs(
        timerDurationInput: StateFlow<String>,
        setTimerDurationInputError: (String) -> Unit,
        extraTimersRepository: IExtraTimersRepository,
    ): Boolean
}

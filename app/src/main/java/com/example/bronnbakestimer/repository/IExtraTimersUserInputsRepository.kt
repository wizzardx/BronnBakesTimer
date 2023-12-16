package com.example.bronnbakestimer.repository

import com.example.bronnbakestimer.model.ExtraTimerUserInputData
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining the operations for a repository managing user inputs for extra timers.
 */
interface IExtraTimersUserInputsRepository {
    /**
     * A read-only [StateFlow] that emits the current state of user input data for extra timers.
     * The data is a list of [ExtraTimerUserInputData].
     */
    val timerData: StateFlow<List<ExtraTimerUserInputData>>

    /**
     * Updates the user input data for extra timers with new data.
     *
     * @param newData The new list of [ExtraTimerUserInputData] to be used.
     */
    fun updateData(newData: List<ExtraTimerUserInputData>)

    /**
     * Clears all error messages associated with user inputs for extra timers.
     *
     * This method iterates through the list of [ExtraTimerUserInputData] in the repository and
     * resets any existing error messages related to timer duration input, timer name input, or other
     * validation fields within each [ExtraTimerUserInputData] instance. The method ensures that after
     * its execution, no extra timer retains any error message that may have been set due to previous
     * user input validation failures or other reasons.
     *
     * This is typically called when a global reset action is performed in the application, such as
     * clearing all forms or resetting the state of the application to its initial configuration. It
     * ensures that the user interface for extra timers is free from error messages and ready for fresh input.
     */
    fun clearExtraTimerErrors()
}

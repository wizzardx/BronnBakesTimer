package com.example.bronnbakestimer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

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

/**
 * Represents extra timer data, including the timer's data, inputs, and a unique identifier.
 *
 * @property data The data associated with the timer.
 * @property inputs The inputs for the timer.
 * @property id The unique identifier for this timer instance.
 */
data class ExtraTimerData(
    val data: TimerData,
    val inputs: ExtraTimerInputsData,
    val id: UUID = UUID.randomUUID(),
)

/**
 * Extension function to calculate the total seconds remaining for the timer.
 *
 * @receiver ExtraTimerData The extra timer data instance.
 * @param mainTimerActive Indicates if the main timer is active.
 * @return The total seconds remaining.
 */
fun ExtraTimerData.getTotalSeconds(mainTimerActive: Boolean): Long {
    return if (mainTimerActive) {
        // Timer is active, so use the current remaining time
        data.millisecondsRemaining / Constants.MillisecondsPerSecond
    } else {
        // Timer is not active, so use a value from the users input
        val userInput = inputs.timerDurationInput.value
        userInputToSeconds(userInput)
    }
}

/**
 * Interface defining the operations for a repository managing extra timers.
 */
interface IExtraTimersRepository {
    /**
     * A read-only [StateFlow] that emits the current state of the extra timers.
     */
    val timerData: StateFlow<List<ExtraTimerData>>

    /**
     * Updates the timer data with new data.
     *
     * @param newData The new list of ExtraTimerData to be used.
     */
    fun updateData(newData: List<ExtraTimerData>)
}

/**
 * Repository for managing extra timer data.
 * This repository handles the state and operations related to extra timers in the application.
 */
object ExtraTimersRepository : IExtraTimersRepository {
    // MutableStateFlow for internal updates
    private val _timerData = MutableStateFlow<List<ExtraTimerData>>(listOf())

    /**
     * Publicly exposed read-only StateFlow of timer data.
     * It provides a way to observe changes to the extra timers data.
     */
    override val timerData: StateFlow<List<ExtraTimerData>> = _timerData

    /**
     * Updates the list of extra timers with new data.
     * This function ensures that the millisecondsRemaining in all timers are non-negative
     * and updates the state flow with the new list.
     *
     * @param newData The new list of ExtraTimerData to update.
     * @throws IllegalArgumentException if any timer has negative millisecondsRemaining.
     */
    override fun updateData(newData: List<ExtraTimerData>) {
        // Ensure the millisecondsRemaining in all the Timers are none-negative:
        for (timer in newData) {
            val msRemaining = timer.data.millisecondsRemaining
            require(msRemaining >= 0) {
                "Time remaining cannot be negative"
            }
        }

        _timerData.value = newData // Atomic and thread-safe update
    }
}

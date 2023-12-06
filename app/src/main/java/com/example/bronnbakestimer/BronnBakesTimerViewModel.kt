package com.example.bronnbakestimer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * ViewModel to help separate business logic from UI logic.
 */
class BronnBakesTimerViewModel(private val timerRepository: ITimerRepository) : ViewModel() {
    /**
     * Input that we've received from the "Timer Minutes" TextField.
     */
    var timerMinutesInput by mutableStateOf("5")

    /**
     * Error message for the "Timer Minutes" TextField.
     */
    var timerMinutesInputError by mutableStateOf<String?>(null)

    /**
     * Return true if the user can edit the text fields.
     */
    fun areTextInputControlsEnabled(timerData: TimerData?): Boolean = timerData == null

    private fun getTotalSeconds() = (timerMinutesInput.toLongOrNull() ?: 0) * Constants.SecondsPerMinute

    /**
     * Return a mm:ss-formatted string for the total time remaining for the entire workout.
     */
    fun formatTotalTimeRemainingString(timerData: TimerData?): String {
        val secondsRemaining = if (timerData == null) {
            getTotalSeconds()
        } else {
            timerData.millisecondsRemaining / Constants.MillisecondsPerSecond
        }
        return formatMinSec(secondsRemaining)
    }

    /**
     * Called when the user clicks the Start/Pause/Resume button.
     */
    @Suppress("TooGenericExceptionCaught")
    fun onButtonClick() {
        try {
            val timerData = timerRepository.timerData.value
            if (timerData == null) {
                // We're not running, so start up the timer.
                // First validate the inputs

                // Validate Work seconds user input
                timerMinutesInputError = validateIntInput(timerMinutesInput)

                // If all the validations passed then we can start up our main timer
                if (timerMinutesInputError == null) {
                    timerRepository.updateData(
                        TimerData(
                            millisecondsRemaining = getTotalSeconds() * Constants.MillisecondsPerSecond,
                            isPaused = false,
                            beepTriggered = false,
                            isFinished = false,
                        )
                    )
                }
            } else {
                // We're running, but are we currently paused?
                if (timerData.isPaused) {
                    // We are paused, so resume.
                    timerRepository.updateData(
                        timerData.copy(
                            isPaused = false,
                        )
                    )
                } else {
                    // We are not paused, so pause.
                    timerRepository.updateData(
                        timerData.copy(
                            isPaused = true,
                        )
                    )
                }
            }
        } catch (e: Exception) {
            logException(e)
        }
    }

    /**
     * Called when the user clicks the Reset button.
     */
    @Suppress("TooGenericExceptionCaught")
    fun onResetClick() {
        try {
            clearResources()
        } catch (e: Exception) {
            logException(e)
        }
    }

    private fun clearResources() {
        timerRepository.updateData(null)
    }
}

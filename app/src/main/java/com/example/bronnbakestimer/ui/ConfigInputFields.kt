package com.example.bronnbakestimer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.bronnbakestimer.logic.Constants
import com.example.bronnbakestimer.util.normaliseIntInput
import com.example.bronnbakestimer.viewmodel.BronnBakesTimerViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Composable function for displaying configurable input fields in the BronnBakesTimer app.
 *
 * This function presents a user interface for inputting configuration settings, such as timer durations.
 * It utilizes a ViewModel and a TimerRepository to manage and reflect the current state of the timer.
 * The function dynamically enables or disables the input fields based on the timer's status, allowing
 * for user interaction when appropriate.
 *
 * The user can input values into these fields, which are then processed by the ViewModel to update the
 * timer's configuration in the TimerRepository. This composable is designed to be reactive; it observes
 * changes in the timer's data and updates the UI accordingly.
 *
 * @param modifier A [Modifier] for styling and layout of the input fields.
 * @param viewModel The [BronnBakesTimerViewModel] instance responsible for handling user interactions
 *                  and business logic associated with the timer.
 */
@Composable
fun ConfigInputFields(
    modifier: Modifier,
    viewModel: BronnBakesTimerViewModel = koinViewModel(),
) {
    // Use collectAsState to observe changes in timerDurationInput
    val currentTimerDurationInput by viewModel.timerDurationInput.collectAsState()

    val configControlsEnabled by viewModel.configControlsEnabled.collectAsState()
    val unitsName = Constants.USER_INPUT_TIME_UNIT.getName()
    val labelText = "Work ($unitsName)"

    InputTextField(
        InputTextFieldParams(
            errorMessage = viewModel.timerDurationInputError,
            value = currentTimerDurationInput,
            onValueChange = { viewModel.updateTimerDurationInput(normaliseIntInput(it)) },
            labelText = labelText,
            modifier = modifier,
            enabled = configControlsEnabled,
            keyboardType = KeyboardType.Number,
        )
    )
}

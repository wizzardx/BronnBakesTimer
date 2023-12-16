package com.example.bronnbakestimer.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bronnbakestimer.logic.Constants
import com.example.bronnbakestimer.model.ExtraTimerUserInputData
import com.example.bronnbakestimer.util.normaliseIntInput
import com.example.bronnbakestimer.viewmodel.BronnBakesTimerViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * A composable function to create a text field for inputting the name of an additional timer.
 *
 * This function renders a text field within the user interface, allowing the user to enter or edit the name
 * of an extra timer in the BronnBakesTimer application. The text field's state and behavior are controlled
 * by the provided `timerUserInputData` and `viewModel`. The function observes and reacts to changes in the
 * timer's configuration, such as enabling or disabling the input field based on the application's state.
 *
 * @param modifier A [Modifier] for applying layout and styling adjustments to the text field.
 * @param timerUserInputData An instance of [ExtraTimerUserInputData] containing the current input data and state for
 *                           the timer.
 * @param viewModel An instance of [BronnBakesTimerViewModel] providing the logic and data management for the timer.
 */
@Composable
@Suppress("FunctionName")
@OptIn(ExperimentalFoundationApi::class)
fun ExtraTimerNameTextField(
    modifier: Modifier,
    timerUserInputData: ExtraTimerUserInputData,
    viewModel: BronnBakesTimerViewModel = koinViewModel(),
) {
    val enabled = viewModel.configControlsEnabled.collectAsState().value
    val currentTimerNameInput by timerUserInputData.inputs.timerNameInput.collectAsState()

    // An input field for the name for this additional timer:
    InputTextField(
        InputTextFieldParams(
            errorMessage = timerUserInputData.inputs.timerNameInputError,
            value = currentTimerNameInput,
            onValueChange = { timerUserInputData.inputs.updateTimerNameInput(it) },
            labelText = "Label",
            modifier = modifier,
            enabled = enabled,
            keyboardType = KeyboardType.Text,
            bringIntoViewRequester = timerUserInputData.inputs.timerNameInputBringIntoViewRequester,
            focusRequester = timerUserInputData.inputs.timerNameInputFocusRequester,
        ),
    )
}

/**
 * A composable function to create a text field for inputting the duration of an additional timer.
 *
 * This function renders a text field within the user interface for setting the duration of an extra timer
 * in the BronnBakesTimer application. The duration input is managed by the `timerUserInputData` and `viewModel`.
 * It supports integer input and updates the state of the timer based on the user's input. The function also
 * observes the application's state to enable or disable the text field as needed, depending on the overall
 * configuration of the timer.
 *
 * @param modifier A [Modifier] for applying layout and styling adjustments to the text field.
 * @param timerUserInputData An instance of [ExtraTimerUserInputData] containing the current input data and state for
 *                           the timer.
 * @param viewModel An instance of [BronnBakesTimerViewModel] providing the logic and data management for the timer.
 */
@Composable
@Suppress("FunctionName")
@OptIn(ExperimentalFoundationApi::class)
fun ExtraTimerDurationTextField(
    modifier: Modifier,
    timerUserInputData: ExtraTimerUserInputData,
    viewModel: BronnBakesTimerViewModel = koinViewModel(),
) {
    val enabled = viewModel.configControlsEnabled.collectAsState().value
    val labelText = Constants.USER_INPUT_TIME_UNIT.getName()
    val currentTimerDurationInput by timerUserInputData.inputs.timerDurationInput.collectAsState()

    // An input field for the duration entry for this additional timer:
    InputTextField(
        InputTextFieldParams(
            errorMessage = timerUserInputData.inputs.timerDurationInputError,
            value = currentTimerDurationInput,
            onValueChange = { newValue ->
                val value = currentTimerDurationInput
                val normalisedValue = normaliseIntInput(newValue, value)
                timerUserInputData.inputs.updateTimerDurationInput(normalisedValue)
            },
            labelText = labelText,
            modifier = modifier.padding(bottom = 8.dp),
            enabled = enabled,
            keyboardType = KeyboardType.Number,
            focusRequester = timerUserInputData.inputs.timerDurationInputFocusRequester,
            bringIntoViewRequester = timerUserInputData.inputs.timerDurationInputBringIntoViewRequester,
        ),
    )
}

/**
 * Composable function for configuring an additional timer in the BronnBakesTimer app.
 *
 * This function provides UI components for configuring an additional timer, including setting its label and minutes.
 * It observes the main timer's data to determine whether the controls are enabled or disabled.
 *
 * @param modifier Modifier for styling and layout of the additional timer configuration.
 * @param timerUserInputData The data representing the additional timer.
 * @param viewModel The view model responsible for managing timer data and updates.
 */
@Composable
@Suppress("FunctionName")
fun AdditionalTimerConfig(
    modifier: Modifier,
    timerUserInputData: ExtraTimerUserInputData,
    viewModel: BronnBakesTimerViewModel = koinViewModel(),
) {
    val enabled = viewModel.configControlsEnabled.collectAsState().value

    // A column to contain our controls to follow:
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        // An input field for the name for this additional timer:
        ExtraTimerNameTextField(
            modifier = modifier,
            timerUserInputData = timerUserInputData,
        )

        // An input field for the duration entry for this additional timer:
        ExtraTimerDurationTextField(
            modifier = modifier,
            timerUserInputData = timerUserInputData,
        )

        // A button with the text "Remove" in it:
        Button(
            onClick = { viewModel.onRemoveTimerClicked(timerUserInputData.id) },
            modifier = modifier.padding(bottom = 8.dp),
            enabled = enabled,
        ) {
            Text(text = "Remove", modifier = modifier)
        }

        // Divider at the bottom for visual separation:
        Divider()
    }
}

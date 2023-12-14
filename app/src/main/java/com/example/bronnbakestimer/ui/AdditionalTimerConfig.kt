package com.example.bronnbakestimer.ui

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
import com.example.bronnbakestimer.viewmodel.BronnBakesTimerViewModel
import com.example.bronnbakestimer.logic.Constants
import com.example.bronnbakestimer.model.ExtraTimerUserInputData
import com.example.bronnbakestimer.util.normaliseIntInput
import org.koin.androidx.compose.koinViewModel

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
fun AdditionalTimerConfig(
    modifier: Modifier,
    timerUserInputData: ExtraTimerUserInputData,
    viewModel: BronnBakesTimerViewModel = koinViewModel(),
) {
    val enabled = viewModel.configControlsEnabled.collectAsState().value
    val currentTimerNameInput by timerUserInputData.inputs.timerNameInput.collectAsState()
    val currentTimerDurationInput by timerUserInputData.inputs.timerDurationInput.collectAsState()

    // A column to contain our controls to follow:
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // An input field for the Label for this additional timer:
        InputTextField(
            InputTextFieldParams(
                errorMessage = timerUserInputData.inputs.timerNameInputError,
                value = currentTimerNameInput,
                onValueChange = { timerUserInputData.inputs.updateTimerNameInput(it) },
                labelText = "Label",
                modifier = modifier,
                enabled = enabled,
                keyboardType = KeyboardType.Text,
            )
        )

        // An input field for the duration entry for this additional timer:
        val labelText = Constants.USER_INPUT_TIME_UNIT.getName()
        InputTextField(
            InputTextFieldParams(
                errorMessage = timerUserInputData.inputs.timerDurationInputError,
                value = currentTimerDurationInput,
                onValueChange = { timerUserInputData.inputs.updateTimerDurationInput(normaliseIntInput(it)) },
                labelText = labelText,
                modifier = modifier.padding(bottom = 8.dp),
                enabled = enabled,
                keyboardType = KeyboardType.Number,
            )
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

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
import androidx.compose.ui.unit.dp
import com.example.bronnbakestimer.repository.IExtraTimersUserInputsRepository
import com.example.bronnbakestimer.viewmodel.BronnBakesTimerViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * Composable function for displaying controls for managing additional timers in the BronnBakesTimer app.
 *
 * This function provides UI components for adding and removing additional timers. It also renders the configuration
 * inputs for additional timers. It observes the main timer's data and the data for additional timers to determine
 * the state and behavior of these controls.
 *
 * @param modifier Modifier for styling and layout of the additional timer controls.
 * @param extraTimersUserInputsRepository The repository for managing additional timer data.
 * @param viewModel The view model responsible for managing timer data and updates.
 */
@Composable
@Suppress("FunctionName")
fun AdditionalTimerControls(
    modifier: Modifier,
    extraTimersUserInputsRepository: IExtraTimersUserInputsRepository = koinInject(),
    viewModel: BronnBakesTimerViewModel = koinViewModel(),
) {
    val timersUserInputsData by extraTimersUserInputsRepository.timerData.collectAsState()
    val configControlsEnabled = viewModel.configControlsEnabled.collectAsState().value

    // A column to contain the sub-controls:
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .padding(top = 8.dp),
    ) {
        Divider()

        timersUserInputsData.forEach { timerUserInputData ->
            AdditionalTimerConfig(modifier, timerUserInputData)
        }

        // A horizontally centered button with the text "Add Timer"
        Button(
            onClick = { viewModel.onAddTimerClicked() },
            modifier = modifier.padding(top = 3.dp),
            enabled = configControlsEnabled,
        ) {
            Text(
                text = "Add Timer",
                modifier = modifier,
            )
        }
    }
}

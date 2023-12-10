package com.example.bronnbakestimer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import androidx.compose.runtime.getValue

/**
 * Composable function for displaying controls for managing additional timers in the BronnBakesTimer app.
 *
 * This function provides UI components for adding and removing additional timers. It also renders the configuration
 * inputs for additional timers. It observes the main timer's data and the data for additional timers to determine
 * the state and behavior of these controls.
 *
 * @param modifier Modifier for styling and layout of the additional timer controls.
 * @param extraTimersRepository The repository for managing additional timer data.
 * @param timerRepository The repository for managing main timer data.
 * @param viewModel The view model responsible for managing timer data and updates.
 */
@Composable
fun AdditionalTimerControls(
    modifier: Modifier,
    extraTimersRepository: IExtraTimersRepository = koinInject(),
    timerRepository: ITimerRepository = koinInject(),
    viewModel: BronnBakesTimerViewModel = koinViewModel(),
) {
    val mainTimerData by timerRepository.timerData.collectAsState()
    val timersData by extraTimersRepository.timerData.collectAsState()

    // A column to contain the sub-controls:
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(top = 8.dp)
    ) {
        Divider()

        timersData.forEach { timerData ->
            AdditionalTimerConfig(modifier, timerData)
        }

        // A horizontally centered button with the text "Add Timer"
        Button(
            onClick = { viewModel.onAddTimerClicked() },
            modifier = modifier.padding(top = 3.dp),
            enabled = viewModel.areTextInputControlsEnabled(mainTimerData)
        ) {
            Text(
                text = "Add Timer",
                modifier = modifier,
            )
        }
    }
}

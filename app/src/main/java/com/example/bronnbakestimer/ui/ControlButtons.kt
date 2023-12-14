package com.example.bronnbakestimer.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bronnbakestimer.repository.ITimerRepository
import com.example.bronnbakestimer.util.getStartPauseResumeButtonText
import com.example.bronnbakestimer.viewmodel.BronnBakesTimerViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * Composable function for displaying the Start/Pause/Resume and Reset buttons in the BronnBakesTimer app.
 *
 * This function is responsible for rendering the Start/Pause/Resume and Reset buttons in the user interface.
 * It interacts with the provided [viewModel] and [timerRepository] to determine the button text and behavior.
 *
 * @param modifier Modifier for styling and layout of the button components.
 * @param viewModel The view model that manages the timer's behavior and controls.
 * @param timerRepository The repository for timer data, used to determine button state and behavior.
 *
 * @see BronnBakesTimerViewModel
 * @see ITimerRepository
 */
@Composable
fun ControlButtons(
    modifier: Modifier,
    viewModel: BronnBakesTimerViewModel = koinViewModel(),
    timerRepository: ITimerRepository = koinInject(),
) {
    val timerData by timerRepository.timerData.collectAsState()

    Row {
        Button(onClick = { viewModel.onButtonClick() }) {
            Text(text = getStartPauseResumeButtonText(timerData), modifier = modifier)
        }

        Spacer(modifier = modifier.width(10.dp))

        Button(onClick = { viewModel.onResetClick() }) {
            Text(text = "Reset", modifier = modifier)
        }
    }
}

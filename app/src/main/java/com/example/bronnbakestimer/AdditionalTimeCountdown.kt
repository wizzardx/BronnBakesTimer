package com.example.bronnbakestimer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

/**
 * Composable function for displaying the time remaining for a single additional timer in the BronnBakesTimer app.
 *
 * This function renders the time remaining for an additional timer as a text element on the user interface.
 * It observes the time remaining and the timer's name input from the provided [viewModel] and updates the UI
 * accordingly.
 *
 * @param modifier Modifier for styling and layout of the additional time countdown view.
 * @param timerData The data representing the additional timer.
 * @param viewModel The view model responsible for managing timer data and updates.
 */
@Composable
fun AdditionalTimeCountdown(
    modifier: Modifier,
    timerData: ExtraTimerData,
    viewModel: BronnBakesTimerViewModel = koinViewModel(),
) {
    val timeRemaining by viewModel.extraTimerRemainingTime(timerData).collectAsState()
    val currentTimerNameInput by timerData.inputs.timerNameInput.collectAsState()

    // A column to contain our controls to follow:
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // A horizontally centered text field with the label for this additional timer:
        Text(
            text = currentTimerNameInput,
            modifier = modifier.padding(bottom = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = timeRemaining,
            modifier = modifier.padding(bottom = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        // Divider at the bottom for visual separation:
        Divider(modifier = modifier.padding(bottom = 8.dp))
    }
}

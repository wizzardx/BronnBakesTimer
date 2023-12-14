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
import org.koin.compose.koinInject

/**
 * Displays the countdown for an additional timer in the BronnBakesTimer app.
 *
 * This function creates a Composable UI element that shows the remaining time for an additional timer.
 * It utilizes a [viewModel] to observe and reflect changes in the time and the timer's name. The layout
 * and style are customizable through the [modifier] parameter. The timer's name and remaining time
 * are displayed in a column layout with a divider for visual separation.
 *
 * @param modifier Modifier for customizing the UI's layout and styling.
 * @param extraTimerUserInputData Data model containing user input for the extra timer.
 * @param extraTimersCountdownRepo Repository interface for the countdown logic of extra timers.
 *                                 Default is injected by Koin.
 * @param viewModel ViewModel that manages timer data and state updates. Default is provided by Koin.
 * @param mainTimerRepo Repository interface for the main timer's countdown logic. Default is injected by Koin.
 */

@Composable
fun AdditionalTimeCountdown(
    modifier: Modifier,
    extraTimerUserInputData: ExtraTimerUserInputData,
    extraTimersCountdownRepo: IExtraTimersCountdownRepository = koinInject(),
    viewModel: BronnBakesTimerViewModel = koinViewModel(),
    mainTimerRepo: ITimerRepository = koinInject(),
) {
    val mainTimerSecondsRemaining = mainTimerRepo.secondsRemaining.collectAsState().value

    val timerDurationInput = extraTimerUserInputData.inputs.timerDurationInput
    val extraTimerRemainingSecondsStateFlow =
        extraTimersCountdownRepo
            .extraTimerSecsFlow(extraTimerUserInputData.id)

    val timeRemaining by viewModel.extraTimerRemainingTime(
        extraTimerUserInputData = extraTimerUserInputData,
        extraTimerRemainingSeconds = extraTimerRemainingSecondsStateFlow,
        timerDurationInput = timerDurationInput,
        mainTimerSecondsRemaining = mainTimerSecondsRemaining,
    ).collectAsState()

    val currentTimerNameInput by extraTimerUserInputData.inputs.timerNameInput.collectAsState()

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

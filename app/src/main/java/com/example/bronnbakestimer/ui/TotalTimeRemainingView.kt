package com.example.bronnbakestimer.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.bronnbakestimer.repository.IMainTimerRepository
import com.example.bronnbakestimer.viewmodel.BronnBakesTimerViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * Composable function for displaying the total time remaining in the BronnBakesTimer app.
 *
 * This function presents the total remaining time as a text element within the UI, dynamically updating
 * based on the timer's status. The text is sourced from the `totalTimeRemainingString` property of the
 * provided [viewModel], ensuring the displayed time is current. Additionally, the text color changes based
 * on the timer's completion status, obtained from [mainTimerRepository]. Upon completion, the text color switches
 * to green, otherwise, it remains black.
 *
 * @param modifier Modifier for customizing styling and layout of the text view.
 * @param viewModel The view model ([BronnBakesTimerViewModel]) that provides the remaining time data.
 *                  This defaults to an instance obtained from Koin's dependency injection.
 * @param mainTimerRepository The repository ([IMainTimerRepository]) that offers the timer's completion status.
 *                            This is injected using Koin and is used to alter the text color upon completion.
 */
@Composable
@Suppress("FunctionName")
fun TotalTimeRemainingView(
    modifier: Modifier,
    viewModel: BronnBakesTimerViewModel = koinViewModel(),
    mainTimerRepository: IMainTimerRepository = koinInject(),
) {
    val text by viewModel.totalTimeRemainingString.collectAsState()

    // Countdown label is black until the countdown completes, then it turns to green.
    val isCompletedState by mainTimerRepository.timerCompleted.collectAsState()
    val textColor = if (isCompletedState == true) Color.Green else Color.Black

    Text(
        text = text,
        color = textColor,
        fontSize = 50.sp,
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

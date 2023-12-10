package com.example.bronnbakestimer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel

/**
 * Composable function for displaying the total time remaining in the BronnBakesTimer app.
 *
 * This function renders the total time remaining as a large text element on the user interface.
 * It observes the time remaining from the provided [viewModel] and updates the UI accordingly.
 *
 * @param modifier Modifier for styling and layout of the total time remaining view.
 * @param viewModel The view model responsible for managing timer data and updates.
 */
@Composable
fun TotalTimeRemainingView(
    modifier: Modifier,
    viewModel: BronnBakesTimerViewModel = koinViewModel(),
) {
    val text by viewModel.totalTimeRemainingString.collectAsState()
    Text(
        text = text,
        fontSize = 50.sp,
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

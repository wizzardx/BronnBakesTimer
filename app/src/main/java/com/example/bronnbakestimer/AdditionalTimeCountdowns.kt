package com.example.bronnbakestimer

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import androidx.compose.runtime.getValue

/**
 * Composable function for displaying the time remaining for all additional timers in the BronnBakesTimer app.
 *
 * This function renders the time remaining for all additional timers as a list of text elements on the user interface.
 * It observes the timer data for additional timers from the provided [extraTimersRepository] and updates the UI
 * accordingly.
 *
 * @param modifier Modifier for styling and layout of the additional time countdowns view.
 * @param extraTimersRepository The repository for managing additional timer data.
 */
@Composable
fun AdditionalTimeCountdowns(modifier: Modifier, extraTimersRepository: IExtraTimersRepository = koinInject()) {
    // Placeholder text

    // A list containing tuples with these values in them: ("Check Rice", 10), ("Stir Soup", 15)
    val timersData by extraTimersRepository.timerData.collectAsState()

    // A dividing line from the control above, but only if there's at least one additional timer:
    if (timersData.isNotEmpty()) {
        Divider(modifier = modifier.padding(bottom = 8.dp))
    }

    // Render the additional countdowns:
    timersData.forEach { timerData ->
        // to the point where we're rendering AdditionalTimeCountdowns
        AdditionalTimeCountdown(modifier, timerData)
    }
}

package com.example.bronnbakestimer

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

/**
 * Renders the countdowns for all additional timers in the BronnBakesTimer app.
 *
 * This Composable function displays the remaining time for all additional timers as a list of UI elements.
 * It observes and updates the UI based on the timer data from the [timerUserInputsRepo]. The function
 * dynamically generates a countdown display for each additional timer, separated by dividers for visual clarity.
 * The layout and appearance of the countdowns can be customized using the [modifier] parameter.
 *
 * @param modifier Modifier for customizing the layout and styling of the countdowns view.
 * @param timerUserInputsRepo Repository for managing data of additional timer inputs. Default is injected by Koin.
 */

@Composable
fun AdditionalTimeCountdowns(modifier: Modifier, timerUserInputsRepo: IExtraTimersUserInputsRepository = koinInject()) {
    val timerUserInputData by timerUserInputsRepo.timerData.collectAsState()

    // A dividing line from the control above, but only if there's at least one additional timer:
    if (timerUserInputData.isNotEmpty()) {
        Divider(modifier = modifier.padding(bottom = 8.dp))
    }

    // Render the additional countdowns:
    timerUserInputData.forEach { timerData ->
        AdditionalTimeCountdown(
            modifier = modifier,
            extraTimerUserInputData = timerData,
        )
    }
}

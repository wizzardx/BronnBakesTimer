package com.example.bronnbakestimer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

/**
 * Composable function representing the main user interface of the BronnBakesTimer app.
 *
 * This function defines the main user interface of the BronnBakesTimer app, including components for displaying
 * the total time remaining, control buttons (Start/Pause/Resume and Reset), configuration input fields,
 * error messages, and the app's version number.
 *
 * @param modifier Modifier for styling and layout of the entire UI.
 * @param errorRepository Repository for error messages, used to trigger recomposition when errors change.
 *
 * @see TotalTimeRemainingView
 * @see ControlButtons
 * @see ConfigInputFields
 */
@Composable
fun BronnBakesTimer(modifier: Modifier = Modifier, errorRepository: IErrorRepository = koinInject()) {
    // Collect state for error message, so we can update our error label a bit later:
    val errorMessage by errorRepository.errorMessage.collectAsState()

    // Column of controls, centered:
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(30.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Total time remaining
        TotalTimeRemainingView(modifier)

        // Time remaining for the additional timers:
        AdditionalTimeCountdowns(modifier)

        // Start/Pause and Reset buttons
        ControlButtons(modifier)

        // Configuration Input Fields
        ConfigInputFields(modifier)

        // Controls for additional timer user inputs come here:
        AdditionalTimerControls(modifier)

        // Padding so that everything after this point gets pushed to the bottom of the screen.
        Spacer(modifier = modifier.weight(1f))

        // Error message at the bottom of the screen, if applicable:
        if (errorMessage != null) {
            Column(
                modifier = modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "ERROR: $errorMessage",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Version number of our app:
        Text(
            text = "Version: ${Constants.APP_VERSION}",
            modifier = modifier,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

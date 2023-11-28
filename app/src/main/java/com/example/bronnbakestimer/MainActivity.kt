package com.example.bronnbakestimer

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bronnbakestimer.ui.theme.BronnBakesTimerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The main activity for our app. The android runtime calls this logic.
 */
class MainActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var errorMessage = mutableStateOf<String?>(null)

    private fun playBeep() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.prepare() // Prepare the MediaPlayer to start from the beginning
            }
            it.start()
        } ?: run {
            // MediaPlayer is null. Handle the case here.
            errorMessage.value = "MediaPlayer is not initialized."
        }
    }

    @Suppress("TooGenericExceptionCaught")
    // Catching a generic exception intentionally for top-level error logging
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.buzzer)
            if (mediaPlayer == null) {
                // MediaPlayer creation failed.
                errorMessage.value = "Error creating MediaPlayer instance."
            }
        } catch (e: Exception) {
            errorMessage.value = "Exception in MediaPlayer initialization: ${e.message}"
            Log.e("MainActivity", "Exception in MediaPlayer initialization", e)
        }

        setContent {
            BronnBakesTimerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BronnBakesTimer(
                        viewModel = BronnBakesTimerViewModel(::playBeep),
                        errorMessage = errorMessage,
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
        }
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}

/**
 * Constants for our app.
 */
object Constants {
    /**
     * There are 60 seconds in every minute.
     */
    const val SecondsPerMinute = 60

    /**
     * There are 1000 milliseconds in every second.
     */
    const val MillisecondsPerSecond = 1000

    /**
     * In our logic we delay for short periods of time and then perform timer-related logic.
     */
    const val SmallDelay = 100

    /**
     * For the sake of simplicity, we limit the user to inputting numbers between 1 and 100.
     */
    const val MaxUserInputNum = 100
}

/**
 * Keeps track of the timer-related state for our app.
 */
class RunningState(
    /**
     * Coroutine scope, used to help us to tidy up the coroutine logic where we manage the timer
     * state.
     */
    val coroutineScope: CoroutineScope,

    /**
     *  How many seconds of intense work per cycle. Usually 20.
     */
    val duration: Int,

    /**
     * Callback function to play a beep noise.
     */
    val playBeep: () -> Unit,
) {

    private var _millisecondsRemaining = mutableStateOf(duration * Constants.MillisecondsPerSecond)

    private var _isPaused = mutableStateOf(false)

    private var countdownJob: Job = coroutineScope.launch {
        // Flags that we use to track if state just changed:
        var timerJustEnded: Boolean

        // Keep track of whether a beep has been triggered for the end of the current cycle:
        var beepTriggered = false

        while (true) {
            // Do nothing while paused.
            while (_isPaused.value) {
                delay(Constants.SmallDelay.toLong())
            }

            // Not paused, so delay a bit, then continue our logic for this loop iteration.
            delay(Constants.SmallDelay.toLong())

            // Progress through the time remaining a bit, but don't let it go below 0
            _millisecondsRemaining.value =
                (_millisecondsRemaining.value - Constants.SmallDelay).coerceAtLeast(0)

            // Set flags based on the current timings.

            // Set a flag if we've just reached the end of the current interval.
            timerJustEnded = _millisecondsRemaining.value <= 0

            // If the time remaining in the interval is
            // less than 1000 milliseconds, and we haven't triggered the beep yet, then trigger
            // the beep now:
            if (
                _millisecondsRemaining.value < Constants.MillisecondsPerSecond &&
                !beepTriggered
            ) {
                playBeep()
                beepTriggered = true
            }

            // If the entire workout just ended then:
            if (timerJustEnded) {
                // Stop this loop, we're done with all our logic.
                break
            }
        }
    }

    /**
     * Return how many seconds remain for the timer.
     */
    fun getSecondsRemaining() = _millisecondsRemaining.value / Constants.MillisecondsPerSecond

    /**
     * Returns true if we are currently paused.
     */
    fun isPaused(): Boolean = _isPaused.value

    /**
     * Shuts down the coroutine and it's timer, etc. Should be called when we're done with this
     * object.
     */
    fun shutdown() {
        countdownJob.cancel()
    }

    /**
     * Pauses the timer.
     */
    fun pause() {
        _isPaused.value = true
    }

    /**
     * Resumes the timer.
     */
    fun resume() {
        _isPaused.value = false
    }
}

/**
 * ViewModel to help separate business logic from UI logic.
 */
class BronnBakesTimerViewModel(
    private val playBeep: () -> Unit = {},
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var runningState: MutableState<RunningState?> = mutableStateOf(null)

    /**
     * Input that we've received from the "Timer seconds" TextField.
     */
    var timerSecondsInput by mutableStateOf("5")

    /**
     * Error message for the "Timer Seconds" TextField.
     */
    var timerSecondsInputError by mutableStateOf<String?>(null)

    private var _cachedAppVersion: String? = null

    /**
     * Return the text to display on the Start/Pause/Resume button.
     */
    val buttonText: String
        get() {
            return if (runningState.value == null) {
                "Start"
            } else {
                if (runningState.value?.isPaused() == true) {
                    "Resume"
                } else {
                    "Pause"
                }
            }
        }

    /**
     * Return true if the user can edit the text fields.
     */
    val textInputControlsEnabled: Boolean
        get() = runningState.value == null

    private fun getTotalSeconds() = timerSecondsInput.toIntOrNull() ?: 0

    /**
     * Return a mm:ss-formatted string for the total time remaining for the entire workout.
     */
    fun formatTotalTimeRemainingString(): String {
        var timeString = formatMinSec(getTotalSeconds())

        runningState.value?.getSecondsRemaining()?.let {
            timeString = formatMinSec(it)
        }

        return timeString
    }

    private fun playBeep() {
        this.playBeep.invoke()
    }

    /**
     * Called when the user clicks the Start/Pause/Resume button.
     */
    fun onButtonClick() {
        if (runningState.value == null) {
            // We're stopped.

            // First validate the inputs

            // Validate Work seconds user input
            timerSecondsInputError = validateIntInput(timerSecondsInput)

            // If all the validations passed then we can start up our main timer
            if (timerSecondsInputError == null) {
                // Validations passed, so we can start up our running state.
                runningState.value = RunningState(
                    coroutineScope = coroutineScope,
                    duration = timerSecondsInput.toInt(),
                    playBeep = ::playBeep,
                )
            }
        } else {
            // We're running, but are we currently paused?
            if (runningState.value?.isPaused() == true) {
                // We are paused, so resume.
                runningState.value?.resume()
            } else {
                // We are not paused, so pause.
                runningState.value?.pause()
            }
        }
    }

    /**
     * Called when the user clicks the Reset button.
     */
    fun onResetClick() {
        clearResources()
    }

    /**
     * Called when we're done with this ViewModel.
     */
    fun clearResources() {
        runningState.value?.shutdown()
        runningState.value = null
    }

    /**
     * Return the version number of our app.
     */
    // We specifically want to catch the NameNotFoundException exception here, and return
    // "Unknown" if we can't get the version number. We don't care about the other details
    // related to the exception. Also, we catch the NullPointerException so that we can
    // show the Preview.
    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    fun getAppVersion(context: Context): String {
        // Return cached app version if we have it already:
        if (_cachedAppVersion != null) {
            return _cachedAppVersion!!
        }
        // Otherwise, cache and return the app version:
        _cachedAppVersion = try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            // This part lets us use the Preview again:
            try {
                packageInfo.versionName
            } catch (e: NullPointerException) {
                "NULL_POINTER_EXCEPTION"
            }
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
        return _cachedAppVersion!!
    }
}

/**
 * Display the total time remaining for the entire workout.
 */
@Composable
fun TotalTimeRemainingView(viewModel: BronnBakesTimerViewModel, modifier: Modifier) {
    Text(
        text = viewModel.formatTotalTimeRemainingString(),
        fontSize = 50.sp,
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/***
 * Display the Start/Pause/Resume and Reset buttons.
 */
@Composable
fun ControlButtons(viewModel: BronnBakesTimerViewModel, modifier: Modifier) {
    Row {
        Button(onClick = { viewModel.onButtonClick() }) {
            Text(text = viewModel.buttonText, modifier = modifier)
        }

        Spacer(modifier = modifier.width(10.dp))

        Button(onClick = { viewModel.onResetClick() }) {
            Text(text = "Reset", modifier = modifier)
        }
    }
}

/***
 * Display the configuration input fields.
 */
@Composable
fun ConfigInputFields(viewModel: BronnBakesTimerViewModel, modifier: Modifier) {
    // Configure Work (seconds, eg 20):
    InputTextField(
        InputTextFieldParams(
            errorMessage = viewModel.timerSecondsInputError,
            value = viewModel.timerSecondsInput,
            onValueChange = { viewModel.timerSecondsInput = normaliseIntInput(it) },
            labelText = "Work",
            modifier = modifier,
            enabled = viewModel.textInputControlsEnabled,
        )
    )
}

/**
 * The main UI for our app.
 */
@Composable
fun BronnBakesTimer(
    modifier: Modifier = Modifier,
    errorMessage: State<String?> = mutableStateOf(null),
    viewModel: BronnBakesTimerViewModel = BronnBakesTimerViewModel(),
) {
    /**
     * Also make sure that we shut down the coroutine and it's timer, etc, at the end when we're
     *  done, if it was allocated at that point.
     */
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearResources()
        }
    }

    // Column of controls, centered:

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(30.dp)
    ) {
        // Total time remaining
        TotalTimeRemainingView(viewModel, modifier)

        // Start/Pause and Reset buttons
        ControlButtons(viewModel, modifier)

        // Configuration Input Fields
        ConfigInputFields(viewModel, modifier)

        // Padding so that everything after this point gets pushed to the bottom of the screen.
        Spacer(modifier = modifier.weight(1f))

        // Error message at the bottom of the screen, if applicable:
        if (errorMessage.value != null) {
            Text(
                text = "ERROR: ${errorMessage.value}",
                color = Color.Red,
                fontWeight = FontWeight.Bold,
            )
        }

        // Version number of our app:
        Text(
            text = "Version: ${viewModel.getAppVersion(LocalContext.current)}",
            modifier = modifier,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Data class representing parameters for the InputTextField composable function.
 *
 * This class encapsulates all the parameters needed for customizing the appearance
 * and behavior of the InputTextField.
 *
 * @property errorMessage An optional error message to display. If non-null, the TextField
 *                        indicates an error state.
 * @property value The current text to be displayed in the TextField.
 * @property onValueChange Callback function to be invoked when the text changes.
 * @property labelText The label text to be displayed above the TextField.
 * @property modifier Modifier for styling and layout of the TextField.
 * @property enabled Flag to indicate whether the TextField is enabled or disabled.
 */
data class InputTextFieldParams(
    val errorMessage: String?,
    val value: String,
    val onValueChange: (String) -> Unit,
    val labelText: String,
    val modifier: Modifier = Modifier,
    val enabled: Boolean
)

/***
 * Customized TextField used by our app.
 */
@Composable
fun InputTextField(params: InputTextFieldParams) {
    val (supportingText, isError) = getErrorInfoFor(params.errorMessage)
    TextField(
        value = params.value,
        onValueChange = params.onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        label = { Text(text = params.labelText) },
        modifier = params.modifier.padding(top = 20.dp),
        enabled = params.enabled,
        supportingText = supportingText,
        isError = isError,
    )
}

/**
 * Preview our app.
 */
@Preview(showSystemUi = true)
@Composable
fun BronnBakesTimerPreview() {
    BronnBakesTimerTheme {
        BronnBakesTimer()
    }
}

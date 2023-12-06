package com.example.bronnbakestimer

import android.Manifest
import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.bronnbakestimer.ui.theme.BronnBakesTimerTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.compose.koinInject
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * Custom Application class for the BronnBakesTimer application.
 *
 * This class extends the Android `Application` class and is responsible for initializing the BronnBakesTimer app.
 * It sets up the Koin dependency injection framework and configures application-wide dependencies by declaring
 * Koin modules.
 *
 * @see startKoin
 * @see androidLogger
 * @see androidContext
 * @see appModule
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Start Koin with the context
        startKoin {
            // Use the androidLogger if you want to use the default Koin Android logger
            androidLogger()

            // Inject Android context into Koin
            androidContext(this@MyApplication)

            // Declare your Koin modules
            modules(appModule)
        }
    }
}

/**
 * The main activity for the BronnBakesTimer app.
 *
 * This class represents the main entry point of the BronnBakesTimer app. It manages the initialization of
 * the app, including starting the Koin dependency injection framework, setting up app-wide modules,
 * requesting permissions, and managing the lifecycle of the app.
 *
 * @see MyApplication
 * @see TimerService
 */
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow.
            startYourService()
        } else {
            // Explain to the user that the feature is unavailable
            logError("Notification Permissions not granted. The Timer countdown will not work.")
        }
    }

    @OptIn(KoinExperimentalAPI::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BronnBakesTimerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Set current Koin instance to Compose context
                    KoinAndroidContext {
                        BronnBakesTimer()
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // This code will only run on devices with Android 13 (API level 33) or higher
            // To request permission
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Provide alternative implementation for devices running Android 10 (API level 29) or below
            // Android 10 does not require a special permission to post notifications
            startYourService()
        }
    }

    override fun onDestroy() {
        // Stop the TimerService when the activity is destroyed
        stopService(Intent(this, TimerService::class.java))

        super.onDestroy()
    }

    private fun startYourService() {
        // Intent to start the TimerService
        val serviceIntent = Intent(this, TimerService::class.java)

        // Start the service using ContextCompat for compatibility across different Android versions
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}

/**
 * Composable function for displaying the total time remaining in the BronnBakesTimer app.
 *
 * This function is responsible for rendering the total time remaining for the entire workout in the user interface.
 * It retrieves the time data from the provided [timerRepository] and formats it using the provided [viewModel].
 *
 * @param modifier Modifier for styling and layout of the total time remaining display.
 * @param viewModel The view model responsible for formatting the total time remaining string.
 * @param timerRepository The repository for timer data, used to retrieve the time remaining information.
 *
 * @see BronnBakesTimerViewModel
 * @see ITimerRepository
 */
@Composable
fun TotalTimeRemainingView(
    modifier: Modifier,
    viewModel: BronnBakesTimerViewModel = koinViewModel(),
    timerRepository: ITimerRepository = koinInject(),
) {
    val timerData by timerRepository.timerData.collectAsState()

    Text(
        text = viewModel.formatTotalTimeRemainingString(timerData),
        fontSize = 50.sp,
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

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

/**
 * Composable function for displaying configurable input fields in the BronnBakesTimer app.
 *
 * This function presents a user interface for inputting configuration settings, such as timer durations.
 * It utilizes a ViewModel and a TimerRepository to manage and reflect the current state of the timer.
 * The function dynamically enables or disables the input fields based on the timer's status, allowing
 * for user interaction when appropriate.
 *
 * The user can input values into these fields, which are then processed by the ViewModel to update the
 * timer's configuration in the TimerRepository. This composable is designed to be reactive; it observes
 * changes in the timer's data and updates the UI accordingly.
 *
 * @param modifier A [Modifier] for styling and layout of the input fields.
 * @param viewModel The [BronnBakesTimerViewModel] instance responsible for handling user interactions
 *                  and business logic associated with the timer.
 * @param timerRepository The [ITimerRepository] instance that provides access to the current timer data,
 *                        which is used to determine the enabled state of the input fields.
 */
@Composable
fun ConfigInputFields(
    modifier: Modifier,
    viewModel: BronnBakesTimerViewModel = koinViewModel(),
    timerRepository: ITimerRepository = koinInject(),
) {
    val timerData by timerRepository.timerData.collectAsState()
    // Configure Work (minutes, eg 5):
    InputTextField(
        InputTextFieldParams(
            errorMessage = viewModel.timerMinutesInputError,
            value = viewModel.timerMinutesInput,
            onValueChange = { viewModel.timerMinutesInput = normaliseIntInput(it) },
            labelText = "Work (Minutes)",
            modifier = modifier,
            enabled = viewModel.areTextInputControlsEnabled(timerData)
        )
    )
}

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
    // Observe changes in the error message and trigger recomposition when it changes
    val errorMessage by errorRepository.errorMessage.collectAsState()

    // Column of controls, centered:
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(30.dp)
    ) {
        // Total time remaining
        TotalTimeRemainingView(modifier)

        // Start/Pause and Reset buttons
        ControlButtons(modifier)

        // Configuration Input Fields
        ConfigInputFields(modifier)

        // Padding so that everything after this point gets pushed to the bottom of the screen.
        Spacer(modifier = modifier.weight(1f))

        // Error message at the bottom of the screen, if applicable:
        if (errorMessage != null) {
            Text(
                text = "ERROR: $errorMessage",
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }

        // Version number of our app:
        Text(
            text = "Version: ${getAppVersion()}",
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

/**
 * Customized Text Field composable used by the BronnBakesTimer app.
 *
 * This composable function is responsible for rendering a customized text field within the app's UI.
 * It allows for displaying an optional error message, specifying the current text value, handling text
 * changes, providing a label, and enabling/disabling the text field.
 *
 * @param params A [InputTextFieldParams] object containing configuration parameters for the text field.
 *
 * @see InputTextFieldParams
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
 * Composable function for previewing the main user interface of the BronnBakesTimer app.
 *
 * This function displays a preview of the app's main user interface using the BronnBakesTimerTheme.
 * It is intended for use in development and testing to visualize how the UI components appear in a Compose preview.
 *
 * @see BronnBakesTimerTheme
 */
@Preview(showSystemUi = true)
@Composable
fun BronnBakesTimerPreview() {
    startKoin {
        modules(testModule)
    }

    BronnBakesTimerTheme {
        BronnBakesTimer()
    }
}

/**
 * Koin module used for providing mock implementations of dependencies during testing.
 * This module is intended for use in unit testing scenarios to replace real implementations with mocks.
 */
val testModule = module {
    // Provide mock implementations for your dependencies

    // Provide a mock implementation of TimerRepository
    single<ITimerRepository> { MockTimerRepository() }

    // Provide a mock implementation of ErrorRepository
    single<IErrorRepository> { MockErrorRepository() }

    // Just use the original viewmodel here, it works fine in preview mode.
    viewModel { BronnBakesTimerViewModel(get()) }
}

/**
 * Mock implementation of the [ITimerRepository] interface for testing purposes.
 * This class provides a mock [timerData] and does not perform actual data updates.
 */
class MockTimerRepository : ITimerRepository {

    override val timerData: StateFlow<TimerData?>
        get() = MutableStateFlow(null)

    override fun updateData(newData: TimerData?) {
        // Do nothing here
    }
}

/**
 * Mock implementation of the [IErrorRepository] interface for testing purposes.
 * This class provides a mock [errorMessage] and does not perform actual error message updates.
 */
class MockErrorRepository : IErrorRepository {
    override val errorMessage: StateFlow<String?>
        get() = MutableStateFlow("Mock Error Message")

    override fun updateData(newMessage: String?) {
        // Do nothing here
    }
}

package com.example.bronnbakestimer.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.bronnbakestimer.provider.IErrorLoggerProvider
import com.example.bronnbakestimer.repository.IErrorRepository
import com.example.bronnbakestimer.service.TimerService
import com.example.bronnbakestimer.ui.BronnBakesTimer
import com.example.bronnbakestimer.ui.theme.BronnBakesTimerTheme
import com.example.bronnbakestimer.util.logError
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.GlobalContext

/**
 * The main activity for the BronnBakesTimer app.
 *
 * This class represents the main entry point of the BronnBakesTimer app. It manages the initialization of
 * the app, including starting the Koin dependency injection framework, setting up app-wide modules,
 * requesting permissions, and managing the lifecycle of the app.
 *
 * @see BronnBakesTimerApp
 * @see TimerService
 */
class MainActivity : ComponentActivity() {
    private val errorRepository: IErrorRepository = GlobalContext.get().get()
    private val errorLoggerProvider: IErrorLoggerProvider = GlobalContext.get().get()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow.
                startYourService()
            } else {
                // Explain to the user that the feature is unavailable
                logError(
                    "Notification Permissions not granted. The Timer countdown will not work.",
                    errorRepository,
                    errorLoggerProvider,
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Note: This might be run multiple times during the app's lifecycle. eg, if the screen is
        //       rotated. However, this (automatically) does not start the service a second time
        //       while it is not already running. However, the lifecycle events of the existing
        //       service code (onCreate, onStartCommand, onDestroy, etc) will be called again.
        initializeActivity()
    }

    /**
     * Initializes the main activity of the BronnBakesTimer app.
     *
     * This function sets up the user interface, requests necessary permissions, and starts the TimerService.
     * It is called in the `onCreate` method of the MainActivity to initialize the app.
     */
    @OptIn(KoinExperimentalAPI::class)
    fun initializeActivity() {
        setContent {
            BronnBakesTimerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
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

    private fun startYourService() {
        // Intent to start the TimerService
        val serviceIntent = Intent(this, TimerService::class.java)

        // Start the service using ContextCompat for compatibility across different Android versions
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}

package com.example.bronnbakestimer.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.bronnbakestimer.provider.CoroutineScopeProvider
import com.example.bronnbakestimer.provider.IErrorLoggerProvider
import com.example.bronnbakestimer.provider.IMediaPlayerWrapper
import com.example.bronnbakestimer.repository.IErrorRepository
import com.example.bronnbakestimer.repository.IExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.IMainTimerRepository
import com.example.bronnbakestimer.util.CoroutineUtils
import com.example.bronnbakestimer.util.PhoneVibrator
import com.example.bronnbakestimer.util.logException
import com.example.bronnbakestimer.viewmodel.BronnBakesTimerViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.ext.android.inject
import org.koin.core.context.GlobalContext

/**
 * `TimerService` is a foreground service in the "BronnBakes Timer" app, designed to manage a
 * countdown timer for cooking purposes. It incorporates a `MediaPlayer` for alerting the user
 * at the end of the cooking duration and uses a `CoroutineScope` for handling timer operations
 * asynchronously. The service allows users to reset and restart the timer based on their
 * cooking needs and maintains its state during these interactions.

 * Key Characteristics:
 * - Facilitates user-initiated resets and restarts of the cooking timer.
 * - Uses `MediaPlayer` to provide audible alerts when the countdown completes.
 * - Functions as a foreground service with a static notification, adhering to Android's
 *   service guidelines.
 * - Handles the timer's lifecycle, including initialization, active countdown, user-initiated
 *   resets, and cleanup of resources.
 * - Operates with a single-instance mechanism, ensuring accurate state management.
 * - Connects with UI components to reflect and control the timer's status.

 * Usage:
 * Designed for the "BronnBakes Timer" app, this service is suitable for cooking activities
 * where users need to set, monitor, reset, or restart cooking times.
 */
class TimerService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Injecting dependencies using Koin
    private val timerRepository: IMainTimerRepository by inject()
    private val mediaPlayerWrapper: IMediaPlayerWrapper by inject()
    private val extraTimersCountdownRepository: IExtraTimersCountdownRepository by inject()
    private val errorRepository: IErrorRepository by inject()
    private val errorLoggerProvider: IErrorLoggerProvider by inject()
    private var coroutineScopeProvider: CoroutineScopeProvider? = null
    private val viewModel: BronnBakesTimerViewModel by inject()

    private val phoneVibrator: PhoneVibrator by lazy {
        PhoneVibrator(applicationContext)
    }

    override fun onBind(intent: Intent): IBinder? = null

    @Suppress("TooGenericExceptionCaught")
    override fun onCreate() {
        try {
            super.onCreate()

            val titleFlow = MutableStateFlow("BronnBakes Timer")
            val contentFlow = viewModel.totalTimeRemainingString
            NotificationHelper(this).launchNotification(this, titleFlow, contentFlow, serviceScope)
            coroutineScopeProvider = GlobalContext.get().get()
            startCountdown()
        } catch (e: Exception) {
            // Log any exceptions that occur during the button click processing
            logException(e, errorRepository, errorLoggerProvider)
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int = START_STICKY

    @Suppress("TooGenericExceptionCaught", "InstanceOfCheckForException")
    private fun startCountdown() {
        try {
            val timeController = RealTimeController()

            // coroutineScopeProvider should be non-null here
            check(coroutineScopeProvider != null) {
                "coroutineScopeProvider should be non-null here"
            }

            val countdownLogic =
                CountdownLogic(
                    timerRepository,
                    mediaPlayerWrapper,
                    coroutineScopeProvider as CoroutineScopeProvider,
                    timeController,
                    extraTimersCountdownRepository,
                    phoneVibrator,
                )
            val dispatcher = Dispatchers.Default
            (coroutineScopeProvider as CoroutineScopeProvider)
                .launch(dispatcher + CoroutineUtils.sharedExceptionHandler) {
                    try {
                        timeController.setDelayLambda({ delay(it) }, this)
                        countdownLogic.execute(this)
                    } catch (e: Exception) {
                        if (e is CancellationException) {
                            // Re-throw JobCancellationException to allow the coroutine to handle cancellation normally
                            throw e
                        } else {
                            // Handle other exceptions
                            logException(e, errorRepository, errorLoggerProvider)
                        }
                    }
                }
        } catch (e: Exception) {
            // Log any exceptions that occur during the button click processing
            logException(e, errorRepository, errorLoggerProvider)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override fun onDestroy() {
        try {
            super.onDestroy()
            serviceScope.cancel() // Cancel the scope when service is destroyed

            // Release resources when the service is destroyed
            mediaPlayerWrapper.release()

            // Cancel the coroutine scope
            if (coroutineScopeProvider != null) {
                (coroutineScopeProvider as CoroutineScopeProvider).coroutineScope.cancel()
                coroutineScopeProvider = null
            }

            // Use the newer stopForeground method
            // STOP_FOREGROUND_REMOVE = 1 or STOP_FOREGROUND_DETACH = 2
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (e: Exception) {
            // Log any exceptions that occur during the button click processing
            logException(e, errorRepository, errorLoggerProvider)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override fun onTaskRemoved(rootIntent: Intent) {
        try {
            super.onTaskRemoved(rootIntent)
            // Stop the service when the app is removed from recent apps
            stopSelf()
        } catch (e: Exception) {
            // Log any exceptions that occur during the button click processing
            logException(e, errorRepository, errorLoggerProvider)
        }
    }
}

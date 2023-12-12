package com.example.bronnbakestimer

// TODO: Unify main timer and extra timer handling, so no special cases for main timer.
// TODO: Optimize some of this code re the new timers. A lot of ugly looking things there.

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject

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
    // Injecting dependencies using Koin
    private val timerRepository: ITimerRepository by inject()
    private val mediaPlayerWrapper: IMediaPlayerWrapper by inject()
    private val extraTimersRepository: IExtraTimersRepository by inject()
    private val errorRepository: IErrorRepository by inject()
    private val errorLoggerProvider: ErrorLoggerProvider by inject()
    private val coroutineScopeProvider: CoroutineScopeProvider by inject()
    private val notificationHelper: NotificationHelper by inject()

    private val phoneVibrator: PhoneVibrator by lazy {
        PhoneVibrator(applicationContext)
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationHelper.launchNotification(this)
        startCountdown()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    @Suppress("TooGenericExceptionCaught", "InstanceOfCheckForException")
    private fun startCountdown() {
        val timeController = RealTimeController()
        val countdownLogic = CountdownLogic(
            timerRepository,
            mediaPlayerWrapper,
            coroutineScopeProvider,
            timeController,
            extraTimersRepository,
            phoneVibrator,
        )
        val dispatcher = Dispatchers.Default
        coroutineScopeProvider.launch(dispatcher + CoroutineUtils.sharedExceptionHandler) {
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
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release resources when the service is destroyed
        mediaPlayerWrapper.release()
        coroutineScopeProvider.coroutineScope.cancel()

        // Use the newer stopForeground method
        // STOP_FOREGROUND_REMOVE = 1 or STOP_FOREGROUND_DETACH = 2
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        // Stop the service when the app is removed from recent apps
        stopSelf()
    }
}

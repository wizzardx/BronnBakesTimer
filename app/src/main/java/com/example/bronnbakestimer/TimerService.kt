package com.example.bronnbakestimer

// TODO: Unify main timer and extra timer handling, so no special cases for main timer.
// TODO: Optimize some of this code re the new timers. A lot of ugly looking things there.

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
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

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        launchNotification()
        startCountdown()
    }

    // long-running operations or services that manage their own lifecycle. This implementation does not handle any
    // intent data, as the service's initialization logic has been moved to `onCreate()`. The service remains in the
    // started state, and if it's terminated due to system constraints, Android will try to recreate it as soon as
    // resources are available, but without re-delivering the original Intent.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    private fun launchNotification() {
        val notificationManager = getNotificationManager()
        createNotificationChannel(notificationManager)
        val notification = buildNotification()
        launchForegroundNotification(notification)
    }

    @SuppressLint("InlinedApi")
    private fun launchForegroundNotification(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For Android 8.0 (Oreo) or higher
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            // For versions below Android 8.0 (Oreo)
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    @Suppress("TooGenericExceptionCaught", "InstanceOfCheckForException")
    private fun startCountdown() {
        val timeController = RealTimeController()
        val countdownLogic = CountdownLogic(
            timerRepository,
            mediaPlayerWrapper,
            coroutineScopeProvider,
            timeController,
            extraTimersRepository,
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

    private fun getNotificationManager(): NotificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        // Unique identifier for the notification channel specific to the TimerService
        private const val NOTIFICATION_CHANNEL_ID = "timer_service_channel"

        // Numeric identifier for the notification used by the service
        private const val NOTIFICATION_ID = 1
    }
}

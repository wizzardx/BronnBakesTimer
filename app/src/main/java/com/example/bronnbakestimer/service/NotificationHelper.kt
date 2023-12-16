package com.example.bronnbakestimer.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.bronnbakestimer.R
import com.example.bronnbakestimer.app.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * The `NotificationHelper` class is responsible for managing and launching notifications
 * in the "BronnBakes Timer" app's `TimerService`. It handles the creation of a notification
 * channel and building a notification to be displayed as a foreground service.
 *
 * Key Functions:
 * - `launchNotification(service: Service)`: Launches a foreground notification for the
 *   `TimerService` using the provided `Service` instance.
 *
 * Usage:
 * This class is designed to be used in conjunction with the `TimerService` to create and
 * display notifications, ensuring that the service remains in the foreground and provides
 * a user-friendly interface for timer management.
 */
class NotificationHelper(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Launches a foreground notification for a specific Service, updating it based on the
     * provided StateFlows for title and content. The method creates a persistent notification
     * that ensures the Service, in this case, TimerService, remains active and visible to the user.
     *
     * This method sets up an initial notification with the current values from the titleFlow
     * and contentFlow. It then observes changes in these StateFlows and updates the notification
     * accordingly. This continuous update is facilitated through a CoroutineScope, allowing
     * asynchronous and non-blocking updates.
     *
     * @param service The Service instance for which the notification will be displayed.
     *                This is typically an instance of TimerService.
     * @param titleFlow A StateFlow<String> that emits updates to the notification's title.
     * @param contentFlow A StateFlow<String> that emits updates to the notification's content text.
     * @param scope A CoroutineScope in which the flow collectors for title and content updates
     *              will be launched. This scope should be tied to the lifecycle of the Service
     *              to ensure that updates are processed as long as the Service is running.
     *
     * Usage Example:
     * ```
     * val notificationHelper = NotificationHelper(context)
     * notificationHelper.launchNotification(
     *     serviceInstance,
     *     titleStateFlow,
     *     contentStateFlow,
     *     CoroutineScope(Dispatchers.Main)
     * )
     * ```
     */
    fun launchNotification(
        service: Service,
        titleFlow: StateFlow<String>,
        contentFlow: StateFlow<String>,
        scope: CoroutineScope,
    ) {
        createNotificationChannel(notificationManager)

        // Initial notification setup
        val initialTitle = titleFlow.value
        val initialContent = contentFlow.value
        val notification = buildNotification(initialTitle, initialContent)
        launchForegroundNotification(service, notification)

        // Observing StateFlows for updates
        titleFlow.combine(contentFlow) { title, content ->
            Pair(title, content)
        }.onEach { (title, content) ->
            updateNotification(title, content)
        }.launchIn(scope) // Use the passed CoroutineScope here
    }

    private fun updateNotification(
        title: String,
        content: String,
    ) {
        val notification = buildNotification(title, content)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(
        title: String,
        content: String,
    ): Notification {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel =
                NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("InlinedApi")
    private fun launchForegroundNotification(
        service: Service,
        notification: Notification,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ServiceCompat.startForeground(
                service,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
        } else {
            service.startForeground(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        // Unique identifier for the notification channel specific to the TimerService
        private const val NOTIFICATION_CHANNEL_ID = "timer_service_channel"

        // Numeric identifier for the notification used by the service
        private const val NOTIFICATION_ID = 1
    }
}

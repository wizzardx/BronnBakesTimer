package com.example.bronnbakestimer

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

    /**
     * Launches a foreground notification for the `TimerService`.
     *
     * @param service The `Service` instance to which the notification will be attached.
     */
    fun launchNotification(service: Service) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)
        val notification = buildNotification()
        launchForegroundNotification(service, notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_content))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    @SuppressLint("InlinedApi")
    private fun launchForegroundNotification(service: Service, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ServiceCompat.startForeground(
                service,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
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

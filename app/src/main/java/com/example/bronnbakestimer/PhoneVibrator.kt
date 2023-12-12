package com.example.bronnbakestimer

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun interface IPhoneVibrator {
    /**
     * Triggers a one-time vibration for a predefined duration.
     *
     * This method checks the Android version at runtime and uses the
     * appropriate API for triggering the vibration. It ensures backward
     * compatibility with older Android versions while utilizing the latest
     * APIs when available.
     *
     * The vibration is executed in a coroutine to prevent blocking the
     * main thread, allowing the application to remain responsive.
     */
    fun vibrate()
}

/**
 * Provides vibration capabilities for an Android application.
 *
 * This class uses the system vibrator service and is compatible with
 * different versions of the Android API. It leverages Kotlin coroutines
 * to perform vibration without blocking the main thread.
 *
 * @param context The context of the application or activity, required
 *                to access the system vibrator service.
 */
class PhoneVibrator(private val context: Context) : IPhoneVibrator {

    /**
     * Triggers a one-time vibration for a predefined duration.
     *
     * This method checks the Android version at runtime and uses the
     * appropriate API for triggering the vibration. It ensures backward
     * compatibility with older Android versions while utilizing the latest
     * APIs when available.
     *
     * The vibration is executed in a coroutine to prevent blocking the
     * main thread, allowing the application to remain responsive.
     */
    override fun vibrate() {
        CoroutineScope(Dispatchers.Default).launch {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        }
    }
}

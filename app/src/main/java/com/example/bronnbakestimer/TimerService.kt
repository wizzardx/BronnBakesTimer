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
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.lang.System.nanoTime
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.math.min

/**
 * A wrapper class that implements the [CoroutineScopeProvider] interface by delegating
 * to an underlying [CoroutineScope]. This allows for compatibility with classes or functions
 * that expect a [CoroutineScopeProvider] while utilizing a [CoroutineScope].
 *
 * @param scope The underlying [CoroutineScope] to be wrapped.
 */
class CoroutineScopeProviderWrapper(private val scope: CoroutineScope) : CoroutineScopeProvider {
    override val isActive: Boolean
        get() = scope.isActive

    override fun launch(
        context: CoroutineContext,
        start: CoroutineStart,
        block: suspend CoroutineScope.() -> Unit
    ) {
        scope.launch(context, start) { block() }
    }
}

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

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        // Initialization logic that should only run once
        launchNotification()
        startCountdown()
    }

    // long-running operations or services that manage their own lifecycle. This implementation does not handle any
    // intent data, as the service's initialization logic has been moved to `onCreate()`. The service remains in the
    // started state, and if it's terminated due to system constraints, Android will try to recreate it as soon as
    // resources are available, but without re-delivering the original Intent.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    private fun launchNotification() {
        // Create a notification channel if the Android version is Oreo or higher
        // Android Oreo (API level 26) introduced Notification Channels, requiring channels for notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Name and description for the notification channel
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            // Define the importance level of the notifications
            val importance = NotificationManager.IMPORTANCE_LOW // IMPORTANCE_LOW is silent
            // Create the NotificationChannel object
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Get NotificationManager and create the notification channel
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification for the foreground service
        val notificationIntent = Intent(this, MainActivity::class.java)
        // PendingIntent to launch the MainActivity when the notification is tapped
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        // Building the notification with various properties
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title)) // Title for the notification
            .setContentText(getString(R.string.notification_content)) // Content text for the notification
            .setSmallIcon(R.drawable.ic_notification) // Notification icon
            .setContentIntent(pendingIntent) // PendingIntent to be triggered on notification click
            .setOngoing(true) // This makes the notification non-dismissible
            .build()
        // TODO: Make the above reactive on timer state, which we update as the main timer
        //       progresses?

        // Launch the notification:
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
        val delayProvider = RealDelayProvider()
        val coroutineScopeProvider = CoroutineScopeProviderWrapper(coroutineScope)
        val countdownLogic = CountdownLogic(
            timerRepository,
            mediaPlayerWrapper,
            coroutineScopeProvider,
            delayProvider,
            extraTimersRepository,
        )
        val dispatcher = Dispatchers.Default
        coroutineScopeProvider.launch(dispatcher + CoroutineUtils.sharedExceptionHandler) {
            try {
                countdownLogic.execute()
            } catch (e: Exception) {
                if (e is CancellationException) {
                    // Re-throw JobCancellationException to allow the coroutine to handle cancellation normally
                    throw e
                } else {
                    // Handle other exceptions
                    logException(e)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release resources when the service is destroyed
        mediaPlayerWrapper.release()
        coroutineScope.cancel()

        // Use the newer stopForeground method
        // STOP_FOREGROUND_REMOVE = 1 or STOP_FOREGROUND_DETACH = 2
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        // Stop the service when the app is removed from recent apps
        stopSelf()
    }

    companion object {
        // Unique identifier for the notification channel specific to the TimerService
        private const val NOTIFICATION_CHANNEL_ID = "timer_service_channel"

        // Numeric identifier for the notification used by the service
        private const val NOTIFICATION_ID = 1
    }
}

/**
 * Interface defining the contract for a repository managing timer data.
 * It provides a flow of timer data and a method to update this data.
 */
interface ITimerRepository {

    /**
     * A [StateFlow] of [TimerData] representing the current state of the timer.
     * It emits the latest timer data whenever there are any updates.
     * The flow can emit `null` if the timer state has not been initialized.
     */
    val timerData: StateFlow<TimerData?>

    /**
     * Updates the current state of the timer with the provided [newData].
     * If [newData] is `null`, it signifies that the timer state is not initialized.
     *
     * @param newData The new [TimerData] state to be emitted.
     *                `null` indicates an uninitialized timer state.
     */
    fun updateData(newData: TimerData?)
}

/**
 * Functional interface representing a provider for delay operations in coroutines.
 * It abstracts the delay mechanism, allowing for different implementations such as real-time delay
 * or a mock delay for testing.
 */
fun interface DelayProvider {

    /**
     * Suspends the coroutine for a specified time.
     *
     * @param timeMillis The time in milliseconds for which the coroutine is to be suspended.
     */
    suspend fun delay(timeMillis: Long)
}

/**
 * Concrete implementation of [DelayProvider] using the standard Kotlin coroutine delay mechanism.
 * This class is used for real-time delay operations within the application.
 */
class RealDelayProvider : DelayProvider {

    /**
     * Implements the delay function using [kotlinx.coroutines.delay].
     *
     * @param timeMillis The time in milliseconds for which the coroutine is to be suspended.
     */
    override suspend fun delay(timeMillis: Long) {
        kotlinx.coroutines.delay(timeMillis)
    }
}

/**
 * An interface that defines the contract for providing a [CoroutineScope] and launching
 * coroutines within it. Implementations of this interface should facilitate the management
 * of coroutine execution and allow checking the activity status of the underlying coroutine scope.
 */
interface CoroutineScopeProvider {

    /**
     * Indicates whether the underlying coroutine scope is still active.
     *
     * @return `true` if the coroutine scope is active, `false` otherwise.
     */
    val isActive: Boolean

    /**
     * Launches a coroutine within the provided [CoroutineContext] and [CoroutineStart].
     * This method abstracts the coroutine launching mechanism, allowing for more control over
     * coroutine behavior and lifecycle.
     *
     * @param context The [CoroutineContext] in which to execute the coroutine. Defaults to [EmptyCoroutineContext].
     * @param start The [CoroutineStart] strategy for coroutine execution. Defaults to [CoroutineStart.DEFAULT].
     * @param block The suspending lambda to be executed within the coroutine.
     */
    fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit,
    )
}

/**
 * Class responsible for managing the countdown logic for a timer.
 * It utilizes a repository to fetch and update timer data and a media player wrapper
 * for audible alerts.
 *
 */
class CountdownLogic(
    private val timerRepository: ITimerRepository,
    private val mediaPlayerWrapper: IMediaPlayerWrapper,
    private val coroutineScopeProvider: CoroutineScopeProvider,
    private val delayProvider: DelayProvider,

    private val extraTimersRepository: IExtraTimersRepository, // = koinInject()
) {

    private var tickCount = 0
    private var totalElapsedTime = 0L

    /**
     * Executes the countdown logic for the timer. This method encapsulates the entire logic for managing
     * the countdown, including updating the timer state, handling pause and finish conditions, and triggering
     * alerts when necessary. It loops until the coroutine scope is active.
     */
    suspend fun execute() {
        var lastTickTimeNano = nanoTime()

        val smallDelayMillis = Constants.SmallDelay
        var accumulatedTimeMillis = 0L

        while (coroutineScopeProvider.isActive) {
            val currentTimeNano = nanoTime()
            val elapsedTimeNano = currentTimeNano - lastTickTimeNano

            // Convert nanoseconds to milliseconds and accumulate
            accumulatedTimeMillis += TimeUnit.NANOSECONDS.toMillis(elapsedTimeNano)

            // Call tick for each whole and partial tick

            // All the whole ticks:
            while (accumulatedTimeMillis >= smallDelayMillis) {
                tick(smallDelayMillis)
                accumulatedTimeMillis -= smallDelayMillis
            }

            // Do any partial tick over here:
            val remainingTime = accumulatedTimeMillis
            if (remainingTime > 0) {
                val timeForTick = min(remainingTime, smallDelayMillis)
                tick(timeForTick)
                accumulatedTimeMillis -= timeForTick
            }

            // Update lastTickTime after processing
            val nTime = nanoTime()
            lastTickTimeNano = nTime

            // Calculate time taken for this iteration including the tick processing time
            val iterationTimeMillis = TimeUnit.NANOSECONDS.toMillis(nTime - currentTimeNano)

            // Calculate the delay needed to maintain the loop frequency
            val delayTimeMillis = smallDelayMillis - iterationTimeMillis

            // Sanity check for delayTime
            check(delayTimeMillis <= smallDelayMillis) { "Delay time is out of range: $delayTimeMillis" }

            // Delay for the remaining time of this iteration. Also handle the case that
            // delayTimeMillis can sometimes be a very large negative value if for some
            // reason (eg testing) the logic thinks that the most recent loop iteration
            // took a very long time. In that case can just skip this delay.

            if (delayTimeMillis > 0) {
                delayProvider.delay(delayTimeMillis)
            }
        }
    }

    private fun tickLogic(
        elapsedTime: Long,
        getValue: () -> TimerData,
        setValue: (TimerData) -> Unit,
    ) {
        var state = getValue()
        val millisecondsRemaining: Long = state.millisecondsRemaining

        val newMillisecondsRemaining = (millisecondsRemaining - elapsedTime).coerceAtLeast(0)

        state = state.copy(millisecondsRemaining = newMillisecondsRemaining)

        // Set flags based on the current timings.

        // Set a flag if we've just reached the end of the current interval.
        val timerJustEnded = newMillisecondsRemaining <= 0

        // If the time remaining in the interval is
        // less than 1000 milliseconds, and we haven't triggered the beep yet, then trigger
        // the beep now:
        if (
            newMillisecondsRemaining < Constants.MillisecondsPerSecond &&
            !state.beepTriggered
        ) {
            mediaPlayerWrapper.playBeep()
            // Update state to reflect that we've triggered the beep:
            state = state.copy(beepTriggered = true)
        }

        // If the entire workout just ended then:
        if (timerJustEnded) {
            state = state.copy(isFinished = true)
        }

        // Update the timer state:
        setValue(state)
    }

    private fun getTimerLambdasSequence(
        mainTimerGetSetLambda: Pair<() -> TimerData, (TimerData) -> Unit>,
        extraTimersData: MutableList<ExtraTimerData>
    ): Sequence<Pair<() -> TimerData, (TimerData) -> Unit>> = sequence {
        yield(mainTimerGetSetLambda)

        extraTimersRepository.timerData.value.forEach { timerData ->
            val getter = { timerData.data }
            val setter: (TimerData) -> Unit = { newTimerData ->
                val index = extraTimersData.indexOfFirst { it.id == timerData.id }
                check(index != -1) { "TimerData not found in extraTimersData" }
                extraTimersData[index] = extraTimersData[index].copy(data = newTimerData)
            }
            yield(Pair(getter, setter))
        }
    }

    /**
     * Represents a single tick in the countdown logic. This function is responsible for decrementing
     * the timer, updating its state, and triggering alerts as necessary. It handles various states
     * like paused, finished, and managing beep alerts.
     *
     * The method checks the timer state and performs actions accordingly. It adjusts the timer's
     * milliseconds remaining, triggers the beep sound at appropriate times, and updates the timer
     * state through the repository.
     */
    fun tick(
        elapsedTime: Long,
    ) {
        tickCount += 1
        totalElapsedTime += elapsedTime

        var mainTimerState = timerRepository.timerData.value
        if (mainTimerState == null || mainTimerState.isPaused || mainTimerState.isFinished) {
            return
        }

        val mainTimerGetSetLambda = Pair(
            { mainTimerState!! },
            { newState: TimerData -> mainTimerState = newState }
        )
        val extraTimersData = extraTimersRepository.timerData.value.toMutableList()
        val timerLambdaSequence = getTimerLambdasSequence(mainTimerGetSetLambda, extraTimersData)

        timerLambdaSequence.forEach { (getValue, setValue) ->
            tickLogic(elapsedTime, getValue, setValue)
        }

        // Update the main timer repository if there were changes
        timerRepository.updateData(mainTimerState)

        // Update the extra timers repository if there were changes
        extraTimersRepository.updateData(extraTimersData)
    }
}

/**
 * Interface defining the contract for a media player wrapper.
 * It provides methods to play a beep sound and to release media player resources.
 */
interface IMediaPlayerWrapper {
    /**
     * Plays a beep sound. This method should handle the media player's state
     * and play the beep sound appropriately.
     */
    fun playBeep()

    /**
     * Releases the media player resources. This method should be called to clean up
     * the media player instance when it is no longer needed.
     */
    fun release()
}

/**
 * Implementation of the [IMediaPlayerWrapper] interface, providing media player functionality.
 * This class initializes and manages a [MediaPlayer] instance to play sounds.
 */
class MediaPlayerWrapper(
    private val context: Context,
    private val soundResId: Int
) : IMediaPlayerWrapper {
    private var mediaPlayer: MediaPlayer? = null

    init {
        initializeMediaPlayer()
    }

    @Suppress("TooGenericExceptionCaught")
    private fun initializeMediaPlayer() {
        // Initialize MediaPlayer for playing the beep sound
        try {
            mediaPlayer = MediaPlayer.create(context, soundResId)
            if (mediaPlayer == null) {
                // Handle MediaPlayer creation failure
                logError("Error creating MediaPlayer instance.")
            }
        } catch (e: Exception) {
            // Handle exceptions
            logException(e)
        }
    }

    override fun playBeep() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.prepare() // Prepare the MediaPlayer to start from the beginning
            }
            it.start()
        } ?: run {
            // Handle case where MediaPlayer is null
            logError("MediaPlayer is null.")
        }
    }

    override fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

package com.example.bronnbakestimer.service

import androidx.annotation.VisibleForTesting
import com.example.bronnbakestimer.logic.Constants
import com.example.bronnbakestimer.provider.CoroutineScopeProvider
import com.example.bronnbakestimer.provider.IMediaPlayerWrapper
import com.example.bronnbakestimer.repository.IExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.ITimerRepository
import com.example.bronnbakestimer.util.IPhoneVibrator
import com.example.bronnbakestimer.util.TimerUserInputDataId
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.math.min

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
    private val timeController: BaseTimeController,
    private val extraTimersCountdownRepository: IExtraTimersCountdownRepository,
    private val phoneVibrator: IPhoneVibrator,
) {
    private var tickCount = 0
    private var totalElapsedTime = 0L

    /**
     * Executes the countdown logic for the timer. This method encapsulates the entire logic for managing
     * the countdown, including updating the timer state, handling pause and finish conditions, and triggering
     * alerts when necessary. It loops until the coroutine scope is active.
     */
    suspend fun execute(scope: CoroutineScope) {
        var lastTickTimeNano = timeController.nanoTime()

        val smallDelayMillis = Constants.SMALL_DELAY
        var accumulatedTimeMillis = 0

        while (coroutineScopeProvider.isActive) {
            val currentTimeNano = timeController.nanoTime()
            val elapsedTimeNano = currentTimeNano - lastTickTimeNano

            // Convert nanoseconds to milliseconds and accumulate
            accumulatedTimeMillis += TimeUnit.NANOSECONDS.toMillis(elapsedTimeNano).toInt()

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
            val nTime = timeController.nanoTime()

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
                timeController.delay(delayTimeMillis, scope)
            }
        }
    }

    private fun tickLogic(
        elapsedTime: Int,
        getValue: () -> TimerData,
        setValue: (TimerData) -> Unit,
    ) {
        var state = getValue()
        val millisecondsRemaining: Int = state.millisecondsRemaining

        val newMillisecondsRemaining = (millisecondsRemaining - elapsedTime).coerceAtLeast(0)

        state = state.copy(millisecondsRemaining = newMillisecondsRemaining)

        // Set flags based on the current timings.

        // Set a flag if we've just reached the end of the current interval.
        val timerJustEnded = newMillisecondsRemaining <= 0

        // If the time remaining in the interval is
        // less than 1000 milliseconds, and we haven't triggered the beep yet, then trigger
        // the beep now:
        if (
            newMillisecondsRemaining < Constants.MILLISECONDS_PER_SECOND &&
            !state.beepTriggered
        ) {
            mediaPlayerWrapper.playBeep()
            // Vibrate the phone too:
            phoneVibrator.vibrate()
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

    /**
     * Generates a sequence of pairs containing getter and setter lambdas for the main timer and any extra timers.
     * This method is primarily used in the tick processing logic to update timer states in a unified way.
     *
     * The sequence starts with the main timer's getter and setter lambdas, followed by those of each extra timer.
     * For extra timers, the getter lambda retrieves the current state of the timer from a ConcurrentHashMap,
     * ensuring thread-safe access. The setter lambda updates the timer state in the ConcurrentHashMap.
     *
     * This approach allows for a consistent and thread-safe way to access and modify the state of multiple timers,
     * which is crucial in a multi-timer environment where timers may be accessed and modified concurrently.
     *
     * @param mainTimerGetSetLambda A pair of lambdas for getting and setting the main timer's state.
     *                              The first element is the getter lambda, and the second element is the setter lambda.
     * @param extraTimersData A ConcurrentHashMap mapping timer IDs to their corresponding countdown data.
     *                        This map is used to access and modify the state of extra timers.
     * @return A sequence of pairs, each containing a getter and setter lambda for a timer.
     *         The sequence starts with the main timer followed by each extra timer.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getTimerLambdasSequence(
        mainTimerGetSetLambda: Pair<() -> TimerData, (TimerData) -> Unit>,
        extraTimersData: ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>
    ): Sequence<Pair<() -> TimerData, (TimerData) -> Unit>> = sequence {
        yield(mainTimerGetSetLambda)

        extraTimersData.forEach { (id, _) ->
            val getter = {
                extraTimersData[id]?.data ?: error("Timer data for id $id is missing")
            }
            val setter: (TimerData) -> Unit = { newTimerData ->
                extraTimersData.computeIfPresent(id) { _, existingTimerData ->
                    existingTimerData.copy(data = newTimerData)
                } ?: error("Failed to update timer data for id $id")
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
    fun tick(elapsedTime: Int) {
        tickCount += 1
        totalElapsedTime += elapsedTime

        var mainTimerState = timerRepository.timerData.value
        if (mainTimerState == null || mainTimerState.isPaused || mainTimerState.isFinished) {
            return
        }

        val mainTimerGetSetLambda = Pair(
            { checkNotNull(mainTimerState) { "Main timer state is unexpectedly null" } },
            { newState: TimerData -> mainTimerState = newState }
        )
        val extraTimersData = extraTimersCountdownRepository.timerData.value
        val timerLambdaSequence = getTimerLambdasSequence(mainTimerGetSetLambda, extraTimersData)

        timerLambdaSequence.forEach { (getValue, setValue) ->
            tickLogic(elapsedTime, getValue, setValue)
        }

        // Update the main timer repository if there were changes
        mainTimerState?.let { timerRepository.updateData(it) }

        // Update the extra timers repository if there were changes
        extraTimersCountdownRepository.updateData(extraTimersData)
    }
}

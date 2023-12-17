package com.example.bronnbakestimer.service

import androidx.annotation.VisibleForTesting
import com.example.bronnbakestimer.logic.Constants
import com.example.bronnbakestimer.provider.CoroutineScopeProvider
import com.example.bronnbakestimer.provider.IMediaPlayerWrapper
import com.example.bronnbakestimer.repository.IExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.IMainTimerRepository
import com.example.bronnbakestimer.util.IPhoneVibrator
import com.example.bronnbakestimer.util.Nanos
import com.example.bronnbakestimer.util.TimerUserInputDataId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

/**
 * Class responsible for managing the countdown logic for a timer.
 * It utilizes a repository to fetch and update timer data and a media player wrapper
 * for audible alerts.
 *
 */
class CountdownLogic(
    private val timerRepository: IMainTimerRepository,
    private val mediaPlayerWrapper: IMediaPlayerWrapper,
    private val coroutineScopeProvider: CoroutineScopeProvider,
    private val timeController: BaseTimeController,
    private val extraTimersCountdownRepository: IExtraTimersCountdownRepository,
    private val phoneVibrator: IPhoneVibrator,
) {
    private var tickCount = 0
    private var totalElapsedTimeNanos = Nanos(0L)

    /**
     * Executes the countdown logic for the timer. This method encapsulates the entire logic for managing
     * the countdown, including updating the timer state, handling pause and finish conditions, and triggering
     * alerts when necessary. It loops until the coroutine scope is active.
     */
    suspend fun execute(scope: CoroutineScope) {
        val targetFrequency = Constants.TICKS_PER_SECOND // target ticks per second
        val targetIntervalNs = Nanos(Constants.NANOSECONDS_PER_SECOND / targetFrequency)

        var lastTickTime = Nanos(timeController.nanoTime())

        while (coroutineScopeProvider.isActive) {
            val currentTime = Nanos(timeController.nanoTime())
            val elapsedSinceLastTick = currentTime - lastTickTime

            if (elapsedSinceLastTick >= targetIntervalNs) {
                tick(elapsedSinceLastTick)
                lastTickTime = currentTime
            }

            // Calculate the time to the next tick
            val timeToNextTick = targetIntervalNs - (Nanos(timeController.nanoTime()) - lastTickTime)
            val delayTime = maxOf(0, timeToNextTick.toMillisLong())
            timeController.delay(delayTime, scope)
        }
    }

    private fun tickLogic(
        elapsedTimeNanos: Nanos,
        getValue: () -> TimerData,
        setValue: (TimerData) -> Unit,
    ) {
        var state = getValue()
        val nanosRemaining: Nanos = state.nanosRemaining

        val newNanosRemaining = (nanosRemaining - elapsedTimeNanos).coerceAtLeast(Nanos(0))

        state = state.copy(nanosRemaining = newNanosRemaining)

        // Set flags based on the current timings.

        // The timer ends when the user sees 00:00 (which might still be 999 ms), rather than
        // when the ms reaches 0. We don't want the user to see 00:00 for 999ms before triggering
        // beeps, vibrates, other feedback, etc
        val timerEnded = newNanosRemaining < Nanos(Constants.NANOSECONDS_PER_SECOND.toLong())
        val timerJustEnded = timerEnded && !state.isFinished

        if (
            timerJustEnded
        ) {
            check(!state.isFinished) { "Timer state is unexpectedly finished" }

            // It shouldn't ever be the case that we've already played the beep already, but in tests
            // it's possible that things were setup that way, so we'll just check for that now
            // anyway
            if (!state.beepTriggered) {
                mediaPlayerWrapper.playBeep()
            }

            // Vibrate the phone too:
            phoneVibrator.vibrate()

            // Update state to reflect that we've triggered the beep, and that the timer countdown has completed:
            state =
                state.copy(
                    beepTriggered = true,
                    isFinished = true,
                )
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
        extraTimersData: ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>,
    ): Sequence<Pair<() -> TimerData, (TimerData) -> Unit>> =
        sequence {
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
    fun tick(elapsedTimeNanos: Nanos) {
        tickCount += 1
        totalElapsedTimeNanos += elapsedTimeNanos

        var mainTimerState = timerRepository.timerData.value
        if (mainTimerState == null || mainTimerState.isPaused || mainTimerState.isFinished) {
            return
        }

        val mainTimerGetSetLambda =
            Pair(
                { checkNotNull(mainTimerState) { "Main timer state is unexpectedly null" } },
                { newState: TimerData -> mainTimerState = newState },
            )
        val extraTimersData = extraTimersCountdownRepository.timerData.value
        val timerLambdaSequence = getTimerLambdasSequence(mainTimerGetSetLambda, extraTimersData)

        timerLambdaSequence.forEach { (getValue, setValue) ->
            tickLogic(elapsedTimeNanos, getValue, setValue)
        }

        // Update the main timer repository if there were changes
        mainTimerState?.let { timerRepository.updateData(it) }

        // Update the extra timers repository if there were changes
        extraTimersCountdownRepository.updateData(extraTimersData)
    }
}

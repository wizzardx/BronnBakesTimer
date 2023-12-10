package com.example.bronnbakestimer

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineScope
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
    private val extraTimersRepository: IExtraTimersRepository,
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

        val smallDelayMillis = Constants.SmallDelay
        var accumulatedTimeMillis = 0L

        while (coroutineScopeProvider.isActive) {
            val currentTimeNano = timeController.nanoTime()
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun getTimerLambdasSequence(
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

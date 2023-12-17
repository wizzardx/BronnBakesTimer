package com.example.bronnbakestimer.repository

import com.example.bronnbakestimer.service.SingleTimerCountdownData
import com.example.bronnbakestimer.service.TimerData
import com.example.bronnbakestimer.util.Nanos
import com.example.bronnbakestimer.util.Seconds
import com.example.bronnbakestimer.util.TimerUserInputDataId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Suppress("FunctionMaxLength")
class DefaultExtraTimersCountdownRepositoryTest {
    private lateinit var countdownRepository: DefaultExtraTimersCountdownRepository

    @Before
    fun setUp() {
        countdownRepository = DefaultExtraTimersCountdownRepository()
    }

    @Test
    fun `extraTimerSecsFlow returns correct StateFlow for existing timer`() =
        runTest {
            // Setup: Create a random TimerUserInputDataId
            val timerId = TimerUserInputDataId.randomId()
            val timerData =
                SingleTimerCountdownData(
                    // 2 minutes:
                    data = TimerData(Nanos.fromMinutes(2)),
                    useInputTimerId = timerId,
                )
            val newData =
                ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>().apply {
                    put(timerId, timerData)
                }
            countdownRepository.updateData(newData)

            // Test: Retrieve the StateFlow for the timer
            val secondsFlow = countdownRepository.extraTimerSecsFlow(timerId)

            // Assert: Check if the StateFlow emits the correct Seconds value
            assertEquals(Seconds(120), secondsFlow.first())
        }

    @Test
    fun `extraTimerSecsFlow throws exception for non-existent timer`() {
        // Setup: Create a random TimerUserInputDataId that is not in the repository
        val nonExistentTimerId = TimerUserInputDataId.randomId()

        // Test and assert: Expect an exception when requesting StateFlow for a non-existent timer
        assertFailsWith<IllegalArgumentException> {
            countdownRepository.extraTimerSecsFlow(nonExistentTimerId)
        }
    }

    @Test
    fun `extraTimerIsCompletedFlow returns correct StateFlow for existing timer`() = runTest {
        // Setup: Create a random TimerUserInputDataId and SingleTimerCountdownData
        val timerId = TimerUserInputDataId.randomId()
        val timerData = SingleTimerCountdownData(
            // Set up TimerData with completed status
            data = TimerData(Nanos.fromMinutes(0), isFinished = true), // 0 minutes, indicating completion
            useInputTimerId = timerId
        )
        val newData = ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>().apply {
            put(timerId, timerData)
        }
        countdownRepository.updateData(newData)

        // Test: Retrieve the StateFlow for the timer's completion status
        val completionFlow = countdownRepository.extraTimerIsCompletedFlow(timerId)

        // Assert: Check if the StateFlow emits the correct Boolean value (true for completed)
        assertEquals(true, completionFlow.first())
    }

    @Test
    fun `clearDataInAllTimers resets all timers to default state`() = runTest {
        // Setup: Create and add multiple timers with different states
        val timerIds = List(3) { TimerUserInputDataId.randomId() }
        val newData = ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>().apply {
            timerIds.forEachIndexed { index, timerId ->
                val timerData = SingleTimerCountdownData(
                    data = TimerData(Nanos.fromSeconds(120 + index * 60), isFinished = index % 2 == 0),
                    useInputTimerId = timerId
                )
                put(timerId, timerData)
            }
        }
        countdownRepository.updateData(newData)

        // Act: Clear data in all timers
        countdownRepository.clearDataInAllTimers()

        // Assert: Check if all timers are reset to default state
        for (timerId in timerIds) {
            val timerDataFlow = countdownRepository.timerData.first()[timerId]
            val secondsRemainingFlow = countdownRepository.extraTimerSecsFlow(timerId).first()
            val timerCompletedFlow = countdownRepository.extraTimerIsCompletedFlow(timerId).first()

            assertEquals(TimerData(), timerDataFlow?.data, "Timer data should be reset to default")
            assertEquals(Seconds(0), secondsRemainingFlow, "Seconds remaining should be reset to 0")
            assertEquals(false, timerCompletedFlow, "Timer completion status should be reset to false")
        }
    }

}

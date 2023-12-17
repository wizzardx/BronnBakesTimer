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
}

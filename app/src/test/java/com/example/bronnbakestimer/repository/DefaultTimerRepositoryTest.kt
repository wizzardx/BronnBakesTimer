package com.example.bronnbakestimer.repository

import com.example.bronnbakestimer.service.TimerData
import com.example.bronnbakestimer.util.Nanos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Suppress("FunctionMaxLength")
@ExperimentalCoroutinesApi
class DefaultTimerRepositoryTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset the main dispatcher to the original Main dispatcher
    }

    @Test
    fun `updateData updates state correctly with valid data`() {
        val timerRepository = DefaultMainTimerRepository()
        val validTimerData =
            TimerData(
                Nanos.fromSeconds(60),
                isPaused = false,
                isFinished = false,
            )

        timerRepository.updateData(validTimerData)

        assertEquals(validTimerData, timerRepository.timerData.value)
    }

    @Test
    fun `updateData sets state to null correctly`() {
        val timerRepository = DefaultMainTimerRepository()

        timerRepository.updateData(null)

        assertNull(timerRepository.timerData.value)
    }

    @Test
    fun `timerData reflects state changes correctly`() =
        runTest {
            val timerRepository = DefaultMainTimerRepository()
            val timerDataList = mutableListOf<TimerData?>()

            // Launch a coroutine to collect timer data
            val job =
                launch {
                    timerRepository.timerData.collect {
                        timerDataList.add(it)
                    }
                }

            // Short delay to ensure collection coroutine is up and running
            delay(100)

            // Update state and collect changes
            val timerData1 = TimerData(Nanos.fromSeconds(60), isPaused = false, isFinished = false)
            timerRepository.updateData(timerData1)

            // Short delay to allow the update to be collected
            delay(100)

            val timerData2 = TimerData(Nanos.fromSeconds(30), isPaused = true, isFinished = false)
            timerRepository.updateData(timerData2)

            // Short delay to allow the second update to be collected
            delay(100)

            // Cancel the job to finish the collection coroutine
            job.cancel()

            // Assert that the timer data list contains the expected values
            assertEquals(listOf(null, timerData1, timerData2), timerDataList)
        }
}

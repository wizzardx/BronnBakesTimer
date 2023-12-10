package com.example.bronnbakestimer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class DefaultTimerRepositoryTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset the main dispatcher to the original Main dispatcher
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `updateData updates state correctly with valid data`() {
        val timerRepository = DefaultTimerRepository()
        val validTimerData = TimerData(60000, false, false, false)

        timerRepository.updateData(validTimerData)

        assertEquals(validTimerData, timerRepository.timerData.value)
    }

    @Test
    fun `updateData sets state to null correctly`() {
        val timerRepository = DefaultTimerRepository()

        timerRepository.updateData(null)

        assertNull(timerRepository.timerData.value)
    }

    @Test
    fun `updateData throws exception for negative milliseconds`() {
        val timerRepository = DefaultTimerRepository()
        val invalidTimerData = TimerData(-1000, false, false, false)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            timerRepository.updateData(invalidTimerData)
        }

        assertEquals("Time remaining cannot be negative", exception.message)
    }

    @Test
    fun `timerData reflects state changes correctly`() = runTest {
        val timerRepository = DefaultTimerRepository()
        val timerDataList = mutableListOf<TimerData?>()

        // Launch a coroutine to collect timer data
        val job = launch {
            timerRepository.timerData.collect {
                println("Collected: $it")
                timerDataList.add(it)
            }
        }

        // Short delay to ensure collection coroutine is up and running
        delay(100)

        // Update state and collect changes
        val timerData1 = TimerData(60000, false, false, false)
        println("Updating to: $timerData1")
        timerRepository.updateData(timerData1)

        // Short delay to allow the update to be collected
        delay(100)

        val timerData2 = TimerData(30000, true, false, false)
        println("Updating to: $timerData2")
        timerRepository.updateData(timerData2)

        // Short delay to allow the second update to be collected
        delay(100)

        // Cancel the job to finish the collection coroutine
        job.cancel()

        // Assert that the timer data list contains the expected values
        assertEquals(listOf(null, timerData1, timerData2), timerDataList)
    }


}

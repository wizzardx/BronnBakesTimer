package com.example.bronnbakestimer.util

import com.example.bronnbakestimer.di.testModule
import com.example.bronnbakestimer.logic.UserInputTimeUnit
import com.example.bronnbakestimer.provider.IErrorLoggerProvider
import com.example.bronnbakestimer.repository.IErrorRepository
import com.example.bronnbakestimer.service.TimerData
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Suppress("FunctionMaxLength")
class UtilsKtTest {
    private val errorRepository: IErrorRepository by lazy { GlobalContext.get().get() }

    @Before
    fun setup() {
        startKoin {
            modules(testModule)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    // Tests for normaliseIntInput

    @Test
    fun `normaliseIntInput returns same string when input contains only digits`() {
        val result = normaliseIntInput("12345", "original")
        assertEquals("12345", result)
    }

    @Test
    fun `normaliseIntInput returns empty string when input is empty`() {
        val result = normaliseIntInput("", "original")
        assertEquals("", result)
    }

    @Test
    fun `normaliseIntInput returns original string when input contains non-digit characters`() {
        val result = normaliseIntInput("abc123", "original")
        assertEquals("original", result)
    }

    @Test
    fun `normaliseIntInput returns original string when input is mixed with digits and non-digits`() {
        val result = normaliseIntInput("123abc", "original")
        assertEquals("original", result)
    }

    // Tests for formatMinSec

    @Test
    fun `formatMinSec formats zero seconds correctly`() {
        val result = formatMinSec(Seconds(0))
        assertEquals("00:00", result)
    }

    @Test
    fun `formatMinSec formats less than a minute correctly`() {
        val result = formatMinSec(Seconds(45))
        assertEquals("00:45", result)
    }

    @Test
    fun `formatMinSec formats exactly one minute correctly`() {
        val result = formatMinSec(Seconds(60))
        assertEquals("01:00", result)
    }

    @Test
    fun `formatMinSec formats more than a minute correctly`() {
        val result = formatMinSec(Seconds(90))
        assertEquals("01:30", result)
    }

    @Test
    fun `formatMinSec formats exactly one hour correctly`() {
        val result = formatMinSec(Seconds(3600))
        assertEquals("60:00", result)
    }

    @Test
    fun `formatMinSec formats more than an hour correctly`() {
        val result = formatMinSec(Seconds(3661))
        assertEquals("61:01", result)
    }

    // Tests for getStartPauseResumeButtonText

    @Test
    fun `getStartPauseResumeButtonText returns Start when timerData is null`() {
        val result = getStartPauseResumeButtonText(null)
        assertEquals("Start", result)
    }

    @Test
    fun `getStartPauseResumeButtonText returns Resume when timerData isPaused is true`() {
        val timerData =
            TimerData(
                isPaused = true,
                isFinished = false,
                nanosRemaining = Nanos(0),
            )
        val result = getStartPauseResumeButtonText(timerData)
        assertEquals("Resume", result)
    }

    @Test
    fun `getStartPauseResumeButtonText returns Pause when timerData isPaused is false`() {
        val timerData =
            TimerData(
                isPaused = false,
                isFinished = false,
                nanosRemaining = Nanos(0),
            )
        val result = getStartPauseResumeButtonText(timerData)
        assertEquals("Pause", result)
    }

    // Tests for userInputToSeconds

    @Test
    fun `converts minutes to seconds correctly`() {
        assertEquals(Seconds(600), (userInputToSeconds("10", UserInputTimeUnit.MINUTES) as Ok).value)
    }

    @Test
    fun `converts seconds to seconds correctly`() {
        assertEquals(Seconds(10), (userInputToSeconds("10", UserInputTimeUnit.SECONDS) as Ok).value)
    }

    @Test
    fun `handles non-numeric input correctly`() {
        val result: String = (userInputToSeconds("not a number") as Err).error
        assertEquals("Invalid input", result)
    }

    @Test
    fun `handles empty input correctly`() {
        assertEquals("Invalid input", (userInputToSeconds("") as Err).error)
    }

    @Test
    @Suppress("UnderscoresInNumericLiterals")
    fun `handles large numeric input correctly`() {
        assertEquals(Seconds(18000), (userInputToSeconds("300", UserInputTimeUnit.MINUTES) as Ok).value)
    }

    // Unit tests for logException

    @Test
    fun `logException reports to repository and logs`() {
        val testLogger = TestErrorLoggerProvider()
        val exception = RuntimeException("Test Exception")

        // Call logException with the test logger
        logException(exception, errorRepository, testLogger)

        // Verify that the test logger received the correct log information
        assertEquals("BronnBakesTimer", testLogger.lastLogTag)
        assertEquals("Error occurred: ", testLogger.lastLogMessage)
        assertEquals(exception, testLogger.lastThrowable)

        // Verify that the error repository was updated
        assertEquals(exception.message, errorRepository.errorMessage.value)
    }

    // Unit tests for logError

    @Test
    fun `logError reports to repository and logs`() {
        val testLogger = TestErrorLoggerProvider()
        val errorMessage = "Test Error"

        logError(errorMessage, errorRepository, testLogger)

        assertEquals("BronnBakesTimer", testLogger.lastLogTag)
        assertEquals(errorMessage, testLogger.lastLogMessage)
        assertNull(testLogger.lastThrowable) // Now correctly asserts null

        assertEquals(errorMessage, errorRepository.errorMessage.value)
    }

    // Tests for formatTotalTimeRemainingString

    @Test
    fun `formatTotalTimeRemainingString returns error for invalid input`() {
        // Arrange: Provide an invalid input that cannot be converted to seconds
        val invalidInput = "invalid"
        val timerData: TimerData? = null // Can be null, as it should not affect this test case

        // Act: Invoke the function with the invalid input
        val result = formatTotalTimeRemainingString(timerData, invalidInput)

        // Assert: Check if the result is an error and contains the expected error message
        assertTrue(result is Err)
        assertEquals("Invalid input", result.error)
    }

    @Test
    fun `formatTotalTimeRemainingString calculates time from input when timerData is null`() {
        // Set timerData to null to ensure the if branch is exercised
        val timerData: TimerData? = null

        // Provide a timer duration input in minutes
        val timerDurationInput = "15" // 15 minutes

        // Invoke the function with null TimerData and a timer duration input
        val result = formatTotalTimeRemainingString(timerData, timerDurationInput)

        // The expected result is "15:00", which is 15 minutes in MM:SS format
        assertEquals("15:00", (result as Ok).value)
    }

    @Test
    fun `formatTotalTimeRemainingString uses timerData when not null`() {
        // Create a non-null TimerData instance with a specific millisecondsRemaining value
        val timerData =
            TimerData(
                isPaused = false,
                isFinished = false,
                // 30 seconds remaining:
                nanosRemaining = Nanos.fromMillis(30_000),
            )

        // Any value for timerDurationInput, as it should not be used
        val timerDurationInput = "10"

        // Invoke the function with the non-null TimerData
        val result = formatTotalTimeRemainingString(timerData, timerDurationInput)

        // Verify that the result is based on the timerData's millisecondsRemaining
        // The expected result is "00:30", which is 30 seconds in MM:SS format
        assertEquals("00:30", (result as Ok).value)
    }

    @Test
    fun `formatTotalTimeRemainingString returns correct format when timerData is null`() {
        val result = formatTotalTimeRemainingString(seconds = null, "10")
        assertEquals("10:00", (result as Ok).value)
    }

    @Test
    fun `formatTotalTimeRemainingString returns correct format when timerData is not null`() {
        val timerData =
            TimerData(
                isPaused = false,
                isFinished = false,
                nanosRemaining = Nanos.fromMillis(60_000),
            )
        val result = formatTotalTimeRemainingString(timerData, "10")
        assertEquals("01:00", (result as Ok).value)
    }

    @Test
    fun `formatTotalTimeRemainingString handles non-numeric input correctly`() {
        val result = (formatTotalTimeRemainingString(seconds = null, "not a number") as Err).error
        assertEquals("Invalid input", result)
    }

    @Test
    fun `formatTotalTimeRemainingString handles empty input correctly`() {
        val result = formatTotalTimeRemainingString(seconds = null, "")
        assertEquals("Invalid input", (result as Err).error)
    }

    @Test
    fun `formatTotalTimeRemainingString handles large numeric input correctly`() {
        val result = formatTotalTimeRemainingString(seconds = null, "300")
        assertEquals("300:00", (result as Ok).value)
    }

    // Tests for myProfiler

    @Test
    fun `myProfiler calculates average execution time correctly`() {
        // Arrange
        val mockTimeProvider = mockk<() -> Long>()
        val mockPrintFunction = mockk<(String) -> Unit>(relaxed = true)

        // Setting up the mock behavior for mockTimeProvider
        // The first value is the start time and the second value is the end time after all iterations
        // For example, if the block takes 1000ms each time, for 4 iterations, the end time would be start time + 4000ms
        every { mockTimeProvider() } returnsMany listOf(0L, 4000L)

        // Act
        myProfiler(times = 4, block = {}, timeProvider = mockTimeProvider, printFunction = mockPrintFunction)

        // Assert
        // The expected average time should be (4000 - 0) / 4 = 1000.0 ms
        verify(exactly = 1) { mockPrintFunction("Average Execution Time for 4 runs: 1000.0 ms") }
    }
}

class TestErrorLoggerProvider : IErrorLoggerProvider {
    var lastLogTag: String? = null
    var lastLogMessage: String? = null
    var lastThrowable: Throwable? = null

    override fun logError(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        lastLogTag = tag
        lastLogMessage = message
        lastThrowable = throwable
    }
}

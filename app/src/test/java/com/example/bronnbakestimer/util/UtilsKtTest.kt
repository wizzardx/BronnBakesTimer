package com.example.bronnbakestimer.util

import com.example.bronnbakestimer.logic.Constants
import com.example.bronnbakestimer.logic.UserInputTimeUnitType
import com.example.bronnbakestimer.logic.ValidationResult
import com.example.bronnbakestimer.provider.IErrorLoggerProvider
import com.example.bronnbakestimer.repository.IErrorRepository
import com.example.bronnbakestimer.service.TimerData
import com.example.bronnbakestimer.di.testModule
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
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
    fun `normaliseIntInput removes non-numeric characters`() {
        val result = normaliseIntInput("a1b2c3")
        assertEquals("123", result)
    }

    @Test
    fun `normaliseIntInput removes leading zeros`() {
        val result = normaliseIntInput("000123")
        assertEquals("123", result)
    }

    @Test
    fun `normaliseIntInput returns zero for all zero input`() {
        val result = normaliseIntInput("0000")
        assertEquals("0", result)
    }

    @Test
    fun `normaliseIntInput returns zero for non-numeric input`() {
        val result = normaliseIntInput("abc")
        assertEquals("0", result)
    }

    @Test
    fun `normaliseIntInput handles empty input`() {
        val result = normaliseIntInput("")
        assertEquals("0", result)
    }

    @Test
    fun `normaliseIntInput preserves internal zeros`() {
        val result = normaliseIntInput("10203")
        assertEquals("10203", result)
    }

    // Tests for validateIntInput

    @Test
    fun `validateIntInput returns error for non-numeric input`() {
        val result = validateIntInput("abc")
        assertTrue(result is ValidationResult.Invalid) // Check if the result is Invalid
        assertEquals("Invalid number", result.reason)
    }

    @Test
    fun `validateIntInput returns error for input less than 1`() {
        val result = validateIntInput("0")
        assertTrue(result is ValidationResult.Invalid) // Check if the result is Invalid
        assertEquals("Must be at least 1", result.reason)
    }

    @Test
    fun `validateIntInput returns error for input greater than MaxUserInputNum`() {
        val result = validateIntInput((Constants.MAX_USER_INPUT_NUM + 1).toString())
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Must be at most ${Constants.MAX_USER_INPUT_NUM}", result.reason)
    }

    @Test
    fun `validateIntInput returns Valid for valid input`() {
        val result = validateIntInput("1")
        assertIs<ValidationResult.Valid>(result)
    }

    // Tests for getErrorInfoFor

    @Test
    fun `getErrorInfoFor returns null Composable and false for null error`() {
        val (composable, hasError) = getErrorInfoFor(null)
        assertNull(composable)
        assertFalse(hasError)
    }

    @Test
    fun `getErrorInfoFor returns non-null Composable and true for non-null error`() {
        val (composable, hasError) = getErrorInfoFor("Error message")
        assertNotNull(composable)
        assertTrue(hasError)
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
        val timerData = TimerData(
            isPaused = true,
            beepTriggered = false,
            isFinished = false,
            millisecondsRemaining = 0
        )
        val result = getStartPauseResumeButtonText(timerData)
        assertEquals("Resume", result)
    }

    @Test
    fun `getStartPauseResumeButtonText returns Pause when timerData isPaused is false`() {
        val timerData = TimerData(
            isPaused = false,
            beepTriggered = false,
            isFinished = false,
            millisecondsRemaining = 0
        )
        val result = getStartPauseResumeButtonText(timerData)
        assertEquals("Pause", result)
    }

    // Tests for userInputToSeconds

    @Test
    fun `converts minutes to seconds correctly`() {
        assertEquals(Seconds(600), userInputToSeconds("10", UserInputTimeUnitType.MINUTES))
    }

    @Test
    fun `converts seconds to seconds correctly`() {
        assertEquals(Seconds(10), userInputToSeconds("10", UserInputTimeUnitType.SECONDS))
    }

    @Test
    fun `handles non-numeric input correctly`() {
        assertEquals(Seconds(0), userInputToSeconds("not a number"))
    }

    @Test
    fun `handles empty input correctly`() {
        assertEquals(Seconds(0), userInputToSeconds(""))
    }

    @Test
    @Suppress("UnderscoresInNumericLiterals")
    fun `handles large numeric input correctly`() {
        assertEquals(Seconds(18000), userInputToSeconds("300", UserInputTimeUnitType.MINUTES))
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
    fun `formatTotalTimeRemainingString calculates time from input when timerData is null`() {
        // Set timerData to null to ensure the if branch is exercised
        val timerData: TimerData? = null

        // Provide a timer duration input in minutes
        val timerDurationInput = "15" // 15 minutes

        // Invoke the function with null TimerData and a timer duration input
        val result = formatTotalTimeRemainingString(timerData, timerDurationInput)

        // The expected result is "15:00", which is 15 minutes in MM:SS format
        assertEquals("15:00", result)
    }

    @Test
    fun `formatTotalTimeRemainingString uses timerData when not null`() {
        // Create a non-null TimerData instance with a specific millisecondsRemaining value
        val timerData = TimerData(
            isPaused = false,
            beepTriggered = false,
            isFinished = false,
            millisecondsRemaining = 30_000 // 30 seconds remaining
        )

        // Any value for timerDurationInput, as it should not be used
        val timerDurationInput = "10"

        // Invoke the function with the non-null TimerData
        val result = formatTotalTimeRemainingString(timerData, timerDurationInput)

        // Verify that the result is based on the timerData's millisecondsRemaining
        // The expected result is "00:30", which is 30 seconds in MM:SS format
        assertEquals("00:30", result)
    }

    @Test
    fun `formatTotalTimeRemainingString returns correct format when timerData is null`() {
        val result = formatTotalTimeRemainingString(seconds = null, "10")
        assertEquals("10:00", result)
    }

    @Test
    fun `formatTotalTimeRemainingString returns correct format when timerData is not null`() {
        val timerData = TimerData(
            isPaused = false,
            beepTriggered = false,
            isFinished = false,
            millisecondsRemaining = 60_000
        )
        val result = formatTotalTimeRemainingString(timerData, "10")
        assertEquals("01:00", result)
    }

    @Test
    fun `formatTotalTimeRemainingString handles non-numeric input correctly`() {
        val result = formatTotalTimeRemainingString(seconds = null, "not a number")
        assertEquals("00:00", result)
    }

    @Test
    fun `formatTotalTimeRemainingString handles empty input correctly`() {
        val result = formatTotalTimeRemainingString(seconds = null, "")
        assertEquals("00:00", result)
    }

    @Test
    fun `formatTotalTimeRemainingString handles large numeric input correctly`() {
        val result = formatTotalTimeRemainingString(seconds = null, "300")
        assertEquals("300:00", result)
    }
}

class TestErrorLoggerProvider : IErrorLoggerProvider {
    var lastLogTag: String? = null
    var lastLogMessage: String? = null
    var lastThrowable: Throwable? = null

    override fun logError(tag: String, message: String, throwable: Throwable) {
        lastLogTag = tag
        lastLogMessage = message
        lastThrowable = throwable
    }

    override fun logError(tag: String, message: String) {
        lastLogTag = tag
        lastLogMessage = message
        lastThrowable = null // Indicate that no Throwable was provided
    }
}

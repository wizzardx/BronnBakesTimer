package com.example.bronnbakestimer

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.assertIs

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
        assertEquals("Invalid number", (result as ValidationResult.Invalid).reason)
    }

    @Test
    fun `validateIntInput returns error for input less than 1`() {
        val result = validateIntInput("0")
        assertTrue(result is ValidationResult.Invalid) // Check if the result is Invalid
        assertEquals("Must be at least 1", (result as ValidationResult.Invalid).reason)
    }

    @Test
    fun `validateIntInput returns error for input greater than MaxUserInputNum`() {
        val result = validateIntInput((Constants.MaxUserInputNum + 1).toString())
        assertTrue(result is ValidationResult.Invalid) // Check if the result is Invalid
        assertEquals("Must be at most ${Constants.MaxUserInputNum}", (result as ValidationResult.Invalid).reason)
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
        val result = formatMinSec(0)
        assertEquals("00:00", result)
    }

    @Test
    fun `formatMinSec formats less than a minute correctly`() {
        val result = formatMinSec(45)
        assertEquals("00:45", result)
    }

    @Test
    fun `formatMinSec formats exactly one minute correctly`() {
        val result = formatMinSec(60)
        assertEquals("01:00", result)
    }

    @Test
    fun `formatMinSec formats more than a minute correctly`() {
        val result = formatMinSec(90)
        assertEquals("01:30", result)
    }

    @Test
    fun `formatMinSec formats exactly one hour correctly`() {
        val result = formatMinSec(3600)
        assertEquals("60:00", result)
    }

    @Test
    fun `formatMinSec formats more than an hour correctly`() {
        val result = formatMinSec(3661)
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
        assertEquals(600, userInputToSeconds("10", UserInputTimeUnitType.MINUTES))
    }

    @Test
    fun `converts seconds to seconds correctly`() {
        assertEquals(10, userInputToSeconds("10", UserInputTimeUnitType.SECONDS))
    }

    @Test
    fun `handles non-numeric input correctly`() {
        assertEquals(0, userInputToSeconds("not a number"))
    }

    @Test
    fun `handles empty input correctly`() {
        assertEquals(0, userInputToSeconds(""))
    }

    @Test
    @Suppress("UnderscoresInNumericLiterals")
    fun `handles large numeric input correctly`() {
        assertEquals(18000, userInputToSeconds("300", UserInputTimeUnitType.MINUTES))
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
    fun `formatTotalTimeRemainingString returns correct format when timerData is null`() {
        val result = formatTotalTimeRemainingString(null, "10")
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
        val result = formatTotalTimeRemainingString(null, "not a number")
        assertEquals("00:00", result)
    }

    @Test
    fun `formatTotalTimeRemainingString handles empty input correctly`() {
        val result = formatTotalTimeRemainingString(null, "")
        assertEquals("00:00", result)
    }

    @Test
    fun `formatTotalTimeRemainingString handles large numeric input correctly`() {
        val result = formatTotalTimeRemainingString(null, "300")
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

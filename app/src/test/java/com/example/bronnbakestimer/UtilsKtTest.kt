package com.example.bronnbakestimer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("FunctionMaxLength")
class UtilsKtTest {

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
        assertEquals("Invalid number", result)
    }

    @Test
    fun `validateIntInput returns error for input less than 1`() {
        val result = validateIntInput("0")
        assertEquals("Must be at least 1", result)
    }

    @Test
    fun `validateIntInput returns error for input greater than MaxUserInputNum`() {
        val result = validateIntInput((Constants.MaxUserInputNum + 1).toString())
        assertEquals("Must be at most ${Constants.MaxUserInputNum}", result)
    }

    @Test
    fun `validateIntInput returns null for valid input`() {
        val result = validateIntInput("1")
        assertNull(result)
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

    // Tests for getAppVersion

    @Test
    fun `getAppVersion returns correct version`() {
        val result = getAppVersion()
        assertEquals(BuildConfig.VERSION_NAME, result)
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
}

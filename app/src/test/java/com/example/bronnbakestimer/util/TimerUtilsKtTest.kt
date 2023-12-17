package com.example.bronnbakestimer.util

import com.example.bronnbakestimer.logic.Constants
import com.github.michaelbull.result.Ok
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("FunctionMaxLength")
class TimerUtilsKtTest {
    @Test
    fun `userInputToNanos converts valid seconds input correctly`() {
        val input = "30" // 30 minutes
        val result = userInputToNanos(input)
        assertTrue(result is Ok)
        assertEquals(30L * Constants.SECONDS_PER_MINUTE * Constants.NANOSECONDS_PER_SECOND, (result as Ok).value.value)
    }

    @Test
    fun `userInputToNanos converts valid minutes input correctly`() {
        val input = "2" // 2 minutes
        val result = userInputToNanos(input)
        assertTrue(result is Ok)
        assertEquals(2 * 60L * Constants.NANOSECONDS_PER_SECOND, (result as Ok).value.value)
    }

    @Test
    fun `userInputToNanos returns error for non-integer input`() {
        val input = "not a number"
        val result = userInputToNanos(input)
        assertFalse(result is Ok)
    }

    @Test
    fun `userInputToNanos returns error for negative input`() {
        val input = "-5"
        val result = userInputToNanos(input)
        assertFalse(result is Ok)
    }

    @Test
    fun `userInputToNanos returns error for input exceeding maximum limit`() {
        val input = (Constants.MAX_USER_INPUT_NUM + 1).toString()
        val result = userInputToNanos(input)
        assertFalse(result is Ok)
    }

    @Test
    fun `userInputToNanos handles zero input correctly`() {
        val input = "0"
        val result = userInputToNanos(input)
        assertFalse(result is Ok) // Assuming zero is not allowed as per range checking
    }
}

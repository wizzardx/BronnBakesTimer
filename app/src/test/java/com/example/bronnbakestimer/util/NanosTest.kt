package com.example.bronnbakestimer.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("FunctionMaxLength")
class NanosTest {
    @Test
    fun `constructor with long should initialize correctly`() {
        val nanos = Nanos(1_000_000L)
        assertEquals(1_000_000L, nanos.value)
    }

    @Test
    fun `constructor with int should initialize correctly`() {
        val nanos = Nanos(1000)
        assertEquals(1000L, nanos.value)
    }

    @Test
    fun `compareTo should return positive when first instance is greater`() {
        val first = Nanos(2000)
        val second = Nanos(1000)
        assertTrue(first > second)
    }

    @Test
    fun `plus should correctly add two Nanos instances`() {
        val first = Nanos(2000)
        val second = Nanos(3000)
        assertEquals(Nanos(5000), first + second)
    }

    @Test
    fun `minus should correctly subtract two Nanos instances`() {
        val first = Nanos(5000)
        val second = Nanos(3000)
        assertEquals(Nanos(2000), first - second)
    }

    @Test
    fun `toString should return correct representation`() {
        val nanos = Nanos(5000)
        assertEquals("5000 ns", nanos.toString())
    }

    @Test
    fun `toSeconds should correctly convert to Seconds`() {
        val nanos = Nanos(1_000_000_000)
        assertEquals(Seconds(1), nanos.toSeconds())
    }

    @Test
    fun `toMillisLong should correctly convert to milliseconds`() {
        val nanos = Nanos(1_000_000)
        assertEquals(1L, nanos.toMillisLong())
    }

    @Test
    fun `fromMinutes should correctly convert minutes to Nanos`() {
        assertEquals(Nanos(60_000_000_000), Nanos.fromMinutes(1))
    }

    @Test
    fun `fromSeconds should correctly convert seconds to Nanos`() {
        assertEquals(Nanos(1_000_000_000), Nanos.fromSeconds(1))
    }

    @Test
    fun `fromMillis should correctly convert milliseconds to Nanos`() {
        assertEquals(Nanos(1_000_000), Nanos.fromMillis(1))
    }

    @Test
    fun `min function should return smaller instance when first is smaller`() {
        val first = Nanos(1000)
        val second = Nanos(2000)
        assertEquals(first, min(first, second))
    }

    @Test
    fun `min function should return smaller instance when second is smaller`() {
        val first = Nanos(3000)
        val second = Nanos(2000)
        assertEquals(second, min(first, second))
    }

    @Test
    fun `min function should return first instance when both are equal`() {
        val first = Nanos(1000)
        val second = Nanos(1000)
        assertEquals(first, min(first, second))
    }

    @Test
    fun `div should correctly divide a Nanos instance by an integer`() {
        val nanos = Nanos(10_000_000) // 10 million nanoseconds
        val divisor = 2
        val expected = Nanos(5_000_000) // Expected result after division
        assertEquals(expected, nanos / divisor)
    }

    @Test
    fun `div should throw ArithmeticException when dividing by zero`() {
        val nanos = Nanos(1_000_000) // 1 million nanoseconds
        assertThrows(ArithmeticException::class.java) {
            nanos / 0
        }
    }
}

package com.example.bronnbakestimer.logic

import org.junit.Test
import kotlin.test.assertEquals

@Suppress("FunctionMaxLength")
class UserInputTimeUnitTest {

    @Test
    fun `getName for MINUTES returns Minutes`() {
        val unitType = UserInputTimeUnit.MINUTES
        val expected = "Minutes"
        val actual = unitType.getName()

        assertEquals(expected, actual, "Expected name for MINUTES is not correct")
    }

    @Test
    fun `getName for SECONDS returns Seconds`() {
        val unitType = UserInputTimeUnit.SECONDS
        val expected = "Seconds"
        val actual = unitType.getName()

        assertEquals(expected, actual, "Expected name for SECONDS is not correct")
    }
}

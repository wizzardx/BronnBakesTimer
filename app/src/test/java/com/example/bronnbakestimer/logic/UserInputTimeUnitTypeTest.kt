package com.example.bronnbakestimer.logic

import com.example.bronnbakestimer.logic.UserInputTimeUnitType
import org.junit.Test
import kotlin.test.assertEquals

@Suppress("FunctionMaxLength")
class UserInputTimeUnitTypeTest {

    @Test
    fun `getName for MINUTES returns Minutes`() {
        val unitType = UserInputTimeUnitType.MINUTES
        val expected = "Minutes"
        val actual = unitType.getName()

        assertEquals(expected, actual, "Expected name for MINUTES is not correct")
    }

    @Test
    fun `getName for SECONDS returns Seconds`() {
        val unitType = UserInputTimeUnitType.SECONDS
        val expected = "Seconds"
        val actual = unitType.getName()

        assertEquals(expected, actual, "Expected name for SECONDS is not correct")
    }
}

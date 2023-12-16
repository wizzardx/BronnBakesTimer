package com.example.bronnbakestimer.util

import org.junit.Test
import kotlin.test.assertEquals

@Suppress("FunctionMaxLength")
class DevelopmentUtilsKtTest {
    private val output = StringBuilder()

    private fun testOutputFunction(message: String) {
        output.append(message)
    }

    // Reset the output StringBuilder before each test
    private fun resetOutput() {
        output.clear()
    }

    @Test
    fun testPrintDebugInfoWithMessage() {
        resetOutput()
        val testMessage = "Test message"

        printDebugInfo(message = testMessage, outputFunction = ::testOutputFunction)

        assert(output.toString().contains(testMessage))
        assert(output.toString().contains("TESTING (Module:"))
    }

    @Test
    fun testPrintDebugInfoWithoutMessage() {
        resetOutput()

        printDebugInfo(outputFunction = ::testOutputFunction)

        val expectedOutput = "TESTING (Module: com.example.bronnbakestimer.util, DevelopmentUtilsKtTest.kt: 34)"
        assertEquals(expectedOutput, output.toString().trim())
    }
}

package com.example.bronnbakestimer

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

@Suppress("FunctionMaxLength")
class ExtraTimerInputsDataTest {

    private lateinit var extraTimerInputsData: ExtraTimerInputsData

    @Before
    fun setup() {
        extraTimerInputsData = ExtraTimerInputsData()
    }

    @Test
    fun `initial values are set correctly`() {
        assertEquals("5", extraTimerInputsData.timerMinutesInput.value)
        assertEquals("check/flip/stir", extraTimerInputsData.timerNameInput.value)
        assertNull(extraTimerInputsData.timerMinutesInputError)
        assertNull(extraTimerInputsData.timerNameInputError)
    }

    @Test
    fun `updateTimerMinutesInput updates value correctly`() {
        val newMinutes = "10"
        extraTimerInputsData.updateTimerMinutesInput(newMinutes)
        assertEquals(newMinutes, extraTimerInputsData.timerMinutesInput.value)
    }

    // Additional tests for updateTimerNameInput and error messages...

    // ExtraTimerInputsData Tests

    @Test
    fun `updateTimerNameInput updates value correctly`() {
        val newName = "new timer name"
        extraTimerInputsData.updateTimerNameInput(newName)
        assertEquals(newName, extraTimerInputsData.timerNameInput.value)
    }

    @Test
    fun `timerMinutesInputError updates correctly`() {
        val errorMessage = "Invalid input"
        extraTimerInputsData.timerMinutesInputError = errorMessage
        assertEquals(errorMessage, extraTimerInputsData.timerMinutesInputError)
    }

    @Test
    fun `timerNameInputError updates correctly`() {
        val errorMessage = "Invalid name"
        extraTimerInputsData.timerNameInputError = errorMessage
        assertEquals(errorMessage, extraTimerInputsData.timerNameInputError)
    }

    // ExtraTimerData Tests

    @Test
    fun `ExtraTimerData has unique UUID`() {
        val timerData1 = ExtraTimerData(
            TimerData(0, isPaused = false, isFinished = false, beepTriggered = false),
            ExtraTimerInputsData()
        )
        val timerData2 = ExtraTimerData(
            TimerData(0, isPaused = false, isFinished = false, beepTriggered = false),
            ExtraTimerInputsData()
        )
        assertNotEquals(timerData1.id, timerData2.id)
    }

    // getTotalSeconds Extension Function Tests

    @Test
    fun `getTotalSeconds returns correct value for active main timer`() {
        val extraTimerData = ExtraTimerData(
            TimerData(
                60_000,
                isPaused = false,
                isFinished = false,
                beepTriggered = false
            ),
            ExtraTimerInputsData()
        )
        assertEquals(60, extraTimerData.getTotalSeconds(mainTimerActive = true))
    }

    @Test
    fun `getTotalSeconds returns correct value for inactive main timer`() {
        val extraTimerInputsData = ExtraTimerInputsData().apply { updateTimerMinutesInput("2") }
        val extraTimerData = ExtraTimerData(
            TimerData(0, isPaused = false, isFinished = false, beepTriggered = false),
            extraTimerInputsData
        )
        assertEquals(120, extraTimerData.getTotalSeconds(mainTimerActive = false))
    }

    // ExtraTimersRepository Tests

    @Test
    fun `updateData updates the repository correctly`() {
        val newData = listOf(
            ExtraTimerData(
                TimerData(
                    30_000,
                    isPaused = false,
                    isFinished = false,
                    beepTriggered = false
                ),
                ExtraTimerInputsData()
            )
        )
        ExtraTimersRepository.updateData(newData)
        assertEquals(newData, ExtraTimersRepository.timerData.value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `updateData throws exception for negative millisecondsRemaining`() {
        val invalidData = listOf(
            ExtraTimerData(
                TimerData(
                    -1000,
                    isPaused = false,
                    isFinished = false,
                    beepTriggered = false
                ),
                ExtraTimerInputsData()
            )
        )
        ExtraTimersRepository.updateData(invalidData)
    }
}

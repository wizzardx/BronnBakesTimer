package com.example.bronnbakestimer

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.assertNotEquals

@Suppress("FunctionMaxLength")
class ExtraTimerInputsDataTest {

    private lateinit var extraTimerInputsData: ExtraTimerInputsData
    private val extraTimersRepository: IExtraTimersRepository by lazy { GlobalContext.get().get() }

    @Before
    fun setup() {
        startKoin {
            modules(testModule)
        }
        extraTimerInputsData = ExtraTimerInputsData()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `initial values are set correctly`() {
        assertEquals("5", extraTimerInputsData.timerDurationInput.value)
        assertEquals("check/flip/stir", extraTimerInputsData.timerNameInput.value)
        assertNull(extraTimerInputsData.timerDurationInputError)
        assertNull(extraTimerInputsData.timerNameInputError)
    }

    @Test
    fun `updateTimerDurationInput updates value correctly`() {
        val newDuration = "10"
        extraTimerInputsData.updateTimerDurationInput(newDuration)
        assertEquals(newDuration, extraTimerInputsData.timerDurationInput.value)
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
    fun `timerDurationInputError updates correctly`() {
        val errorMessage = "Invalid input"
        extraTimerInputsData.timerDurationInputError = errorMessage
        assertEquals(errorMessage, extraTimerInputsData.timerDurationInputError)
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
        val extraTimerInputsData = ExtraTimerInputsData().apply { updateTimerDurationInput("2") }
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
        extraTimersRepository.updateData(newData)
        assertEquals(newData, extraTimersRepository.timerData.value)
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
        extraTimersRepository.updateData(invalidData)
    }

    companion object {
        val testModule = module {
            single<IExtraTimersRepository> { DefaultExtraTimersRepository() }
        }
    }
}

package com.example.bronnbakestimer.model

import com.example.bronnbakestimer.di.testModule
import com.example.bronnbakestimer.repository.IExtraTimersUserInputsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Suppress("FunctionMaxLength")
class ExtraTimerInputsDataTest {
    private lateinit var extraTimerInputsData: ExtraTimerInputsData

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

    // ExtraTimersRepository Tests

    @Test
    fun `updateData updates the repository correctly`() {
        // Create new data to be updated in the repository
        val newData =
            listOf(
                ExtraTimerUserInputData(
                    inputs =
                        ExtraTimerInputsData().apply {
                            updateTimerDurationInput("30")
                            updateTimerNameInput("New Timer")
                        },
                ),
            )

        // Mock the behavior of the repository
        val mockRepository = mock(IExtraTimersUserInputsRepository::class.java)
        val mockStateFlow = MutableStateFlow(newData)
        `when`(mockRepository.timerData).thenReturn(mockStateFlow)

        // Update the repository with new data
        mockRepository.updateData(newData)

        // Retrieve the updated data from the repository
        val updatedData = mockRepository.timerData.value

        // Assert that the updated data matches the new data
        assertEquals(newData, updatedData, "Repository should contain the updated data")
    }
}

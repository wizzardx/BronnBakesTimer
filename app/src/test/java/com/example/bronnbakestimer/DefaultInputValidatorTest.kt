package com.example.bronnbakestimer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@Suppress("FunctionMaxLength")
@RunWith(MockitoJUnitRunner::class)
class DefaultInputValidatorTest {

    @Mock
    private lateinit var extraTimersUserInputRepository: IExtraTimersUserInputsRepository

    private lateinit var defaultInputValidator: DefaultInputValidator

    @Before
    fun setUp() {
        defaultInputValidator = DefaultInputValidator()
    }

    @Test
    fun validatesAllInputsReturnsValidWhenInputsAreValid() = runTest {
        val timerDurationInput = MutableStateFlow("10")
        val setTimerDurationInputError: (String) -> Unit = {}
        `when`(extraTimersUserInputRepository.timerData).thenReturn(MutableStateFlow(emptyList()))

        val result = defaultInputValidator.validateAllInputs(
            timerDurationInput,
            setTimerDurationInputError,
            extraTimersUserInputRepository
        )

        // Check that the result is Valid
        assert(result is ValidationResult.Valid)
    }

    @Test
    fun validatesAllInputsReturnsInvalidWhenMainTimerInputIsInvalid() = runTest {
        val timerDurationInput = MutableStateFlow("invalid")
        var errorMessage = ""
        val setTimerDurationInputError: (String) -> Unit = { errorMessage = it }
        `when`(extraTimersUserInputRepository.timerData).thenReturn(MutableStateFlow(emptyList()))

        val result = defaultInputValidator.validateAllInputs(
            timerDurationInput,
            setTimerDurationInputError,
            extraTimersUserInputRepository
        )

        // Check that the result is Invalid and error message is not empty
        assert(result is ValidationResult.Invalid)
        assert(errorMessage.isNotEmpty())
    }

    @Test
    fun validatesAllInputsReturnsInvalidWhenExtraTimerInputIsInvalid() = runTest {
        val timerDurationInput = MutableStateFlow("10")
        val setTimerDurationInputError: (String) -> Unit = {}

        // Mocking an extra timer with invalid duration input
        val invalidExtraTimerInput = ExtraTimerUserInputData().apply {
            inputs.updateTimerDurationInput("invalid")
        }
        `when`(extraTimersUserInputRepository.timerData).thenReturn(MutableStateFlow(listOf(invalidExtraTimerInput)))

        val result = defaultInputValidator.validateAllInputs(
            timerDurationInput,
            setTimerDurationInputError,
            extraTimersUserInputRepository
        )

        // Check that the result is Invalid and timer duration input error is not empty
        assert(result is ValidationResult.Invalid)
        assert(invalidExtraTimerInput.inputs.timerDurationInputError!!.isNotEmpty())
    }

    @Test
    fun validatesAllInputsReturnsInvalidWhenExtraTimerDurationIsGreaterThanMainTimerDuration() = runTest {
        val mainTimerDurationInput = MutableStateFlow("10") // 10 seconds
        val setMainTimerDurationInputError: (String) -> Unit = {}

        // Mock an extra timer with a duration greater than the main timer
        val extraTimer = ExtraTimerUserInputData().apply {
            inputs.updateTimerDurationInput("15") // 15 seconds
        }
        `when`(extraTimersUserInputRepository.timerData).thenReturn(MutableStateFlow(listOf(extraTimer)))

        val result = defaultInputValidator.validateAllInputs(
            mainTimerDurationInput,
            setMainTimerDurationInputError,
            extraTimersUserInputRepository
        )

        // Check that the result is Invalid
        assert(result is ValidationResult.Invalid)
        // Check the specific error message for the extra timer
        assert(extraTimer.inputs.timerDurationInputError == "Extra timer time cannot be greater than main timer time.")
    }

    @Test
    fun validateAllInputsShouldReturnValidResultWhenAllInputsAreCorrect() = runTest {
        val mainTimerDurationInput = MutableStateFlow("30") // 30 seconds, valid
        val setMainTimerDurationInputError: (String) -> Unit = {}

        // Mock an extra timer with a valid duration
        val extraTimer = ExtraTimerUserInputData().apply {
            inputs.updateTimerDurationInput("20") // 20 seconds, valid
        }
        `when`(extraTimersUserInputRepository.timerData).thenReturn(MutableStateFlow(listOf(extraTimer)))

        val result = defaultInputValidator.validateAllInputs(
            mainTimerDurationInput,
            setMainTimerDurationInputError,
            extraTimersUserInputRepository
        )

        // Check that the result is Valid
        assert(result is ValidationResult.Valid)
    }
}

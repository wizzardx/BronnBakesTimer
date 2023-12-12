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
    private lateinit var extraTimersRepository: IExtraTimersRepository

    private lateinit var defaultInputValidator: DefaultInputValidator

    @Before
    fun setUp() {
        defaultInputValidator = DefaultInputValidator()
    }

    @Test
    fun validatesAllInputsReturnsValidWhenInputsAreValid() = runTest {
        val timerDurationInput = MutableStateFlow("10")
        val setTimerDurationInputError: (String) -> Unit = {}
        `when`(extraTimersRepository.timerData).thenReturn(MutableStateFlow(emptyList()))

        val result = defaultInputValidator.validateAllInputs(
            timerDurationInput,
            setTimerDurationInputError,
            extraTimersRepository
        )

        // Check that the result is Valid
        assert(result is ValidationResult.Valid)
    }

    @Test
    fun validatesAllInputsReturnsInvalidWhenMainTimerInputIsInvalid() = runTest {
        val timerDurationInput = MutableStateFlow("invalid")
        var errorMessage = ""
        val setTimerDurationInputError: (String) -> Unit = { errorMessage = it }
        `when`(extraTimersRepository.timerData).thenReturn(MutableStateFlow(emptyList()))

        val result = defaultInputValidator.validateAllInputs(
            timerDurationInput,
            setTimerDurationInputError,
            extraTimersRepository
        )

        // Check that the result is Invalid and error message is not empty
        assert(result is ValidationResult.Invalid)
        assert(errorMessage.isNotEmpty())
    }

    @Test
    fun validatesAllInputsReturnsInvalidWhenExtraTimerInputIsInvalid() = runTest {
        val timerDurationInput = MutableStateFlow("10")
        val setTimerDurationInputError: (String) -> Unit = {}

        val extraTimer = ExtraTimerData(
            data = TimerData(
                millisecondsRemaining = 0,
                isPaused = false,
                isFinished = false,
                beepTriggered = false
            ),
            inputs = ExtraTimerInputsData().apply {
                updateTimerDurationInput("invalid")
            }
        )

        `when`(extraTimersRepository.timerData).thenReturn(MutableStateFlow(listOf(extraTimer)))

        val result = defaultInputValidator.validateAllInputs(
            timerDurationInput,
            setTimerDurationInputError,
            extraTimersRepository
        )

        // Check that the result is Invalid and timer duration input error is not empty
        assert(result is ValidationResult.Invalid)
        assert((result as ValidationResult.Invalid).reason.isNotEmpty())
    }

    @Test
    fun validatesAllInputsReturnsInvalidWhenExtraTimerDurationIsGreaterThanMainTimerDuration() = runTest {
        val mainTimerDurationInput = MutableStateFlow("10")
        var mainTimerErrorMessage = ""
        val setMainTimerDurationInputError: (String) -> Unit = { mainTimerErrorMessage = it }

        // Create an extra timer with a duration greater than the main timer
        val extraTimerInputsData = ExtraTimerInputsData().apply {
            updateTimerDurationInput("15")
        }
        val extraTimer = ExtraTimerData(
            data = TimerData(
                millisecondsRemaining = 0,
                isPaused = false,
                isFinished = false,
                beepTriggered = false
            ),
            inputs = extraTimerInputsData
        )

        `when`(extraTimersRepository.timerData).thenReturn(MutableStateFlow(listOf(extraTimer)))

        val result = defaultInputValidator.validateAllInputs(
            mainTimerDurationInput,
            setMainTimerDurationInputError,
            extraTimersRepository
        )

        // Check that the result is Invalid
        assert(result is ValidationResult.Invalid)
        // Check that the extra timer's duration input error message is set correctly
        assert(extraTimer.inputs.timerDurationInputError == "Extra timer time cannot be greater than main timer time.")
        // Assert that no error message was set for the main timer's input
        assert(mainTimerErrorMessage.isEmpty())
    }
}

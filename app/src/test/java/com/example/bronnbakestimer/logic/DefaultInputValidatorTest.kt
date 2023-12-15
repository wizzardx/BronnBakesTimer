package com.example.bronnbakestimer.logic

import com.example.bronnbakestimer.di.testModule
import com.example.bronnbakestimer.model.ExtraTimerUserInputData
import com.example.bronnbakestimer.provider.IErrorLoggerProvider
import com.example.bronnbakestimer.repository.DefaultErrorRepository
import com.example.bronnbakestimer.repository.DefaultExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.DefaultTimerRepository
import com.example.bronnbakestimer.repository.IExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.IExtraTimersUserInputsRepository
import com.example.bronnbakestimer.repository.ITimerRepository
import com.example.bronnbakestimer.util.testErrorLoggerProvider
import com.example.bronnbakestimer.viewmodel.BronnBakesTimerViewModel
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@Suppress("FunctionMaxLength")
@RunWith(MockitoJUnitRunner::class)
class DefaultInputValidatorTest {

    @Mock
    private lateinit var extraTimersUserInputRepository: IExtraTimersUserInputsRepository

    private lateinit var defaultInputValidator: DefaultInputValidator

    private lateinit var viewModel: BronnBakesTimerViewModel
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mainTimerRepository: ITimerRepository
    private lateinit var extraTimersCountdownRepository: IExtraTimersCountdownRepository
    private lateinit var errorRepository: DefaultErrorRepository
    private lateinit var timerManager: DefaultTimerManager
    private lateinit var inputValidator: DefaultInputValidator
    private lateinit var errorLoggerProvider: IErrorLoggerProvider

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        defaultInputValidator = DefaultInputValidator()

        Dispatchers.setMain(testDispatcher)

        mainTimerRepository = DefaultTimerRepository()
        extraTimersCountdownRepository = DefaultExtraTimersCountdownRepository()
        errorRepository = DefaultErrorRepository()
        timerManager = DefaultTimerManager()
        inputValidator = DefaultInputValidator()
        errorLoggerProvider = testErrorLoggerProvider

        viewModel = BronnBakesTimerViewModel(
            mainTimerRepository,
            timerManager,
            inputValidator,
            extraTimersUserInputRepository,
            extraTimersCountdownRepository,
            errorRepository,
            errorLoggerProvider,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        // Any cleanup code if needed
        Dispatchers.resetMain()
    }

    @Test
    fun validatesAllInputsReturnsValidWhenInputsAreValid() = runTest {
        val timerDurationInput = MutableStateFlow("10")
        val setTimerDurationInputError: (String?) -> Unit = {}
        `when`(extraTimersUserInputRepository.timerData).thenReturn(MutableStateFlow(emptyList()))

        val result = defaultInputValidator.validateAllInputs(
            timerDurationInput,
            setTimerDurationInputError,
            extraTimersUserInputRepository,
            viewModel,
            this,
            true,
        )

        // Check that the result is Valid
        assert(result is Ok)
    }

    @Test
    fun validatesAllInputsReturnsInvalidWhenMainTimerInputIsInvalid() = runTest {
        val timerDurationInput = MutableStateFlow("invalid")
        var errorMessage: String? = ""
        val setTimerDurationInputError: (String?) -> Unit = { errorMessage = it }
        `when`(extraTimersUserInputRepository.timerData).thenReturn(MutableStateFlow(emptyList()))

        val result = defaultInputValidator.validateAllInputs(
            timerDurationInput,
            setTimerDurationInputError,
            extraTimersUserInputRepository,
            viewModel,
            this,
            true,
        )

        // Check that the result is Invalid and error message is not empty
        assert(result is Err)
        assert(errorMessage!!.isNotEmpty())
    }

    @Test
    fun validatesAllInputsReturnsInvalidWhenExtraTimerInputIsInvalid() = runTest {
        val timerDurationInput = MutableStateFlow("10")
        val setTimerDurationInputError: (String?) -> Unit = {}

        // Mocking an extra timer with invalid duration input
        val invalidExtraTimerInput = ExtraTimerUserInputData().apply {
            inputs.updateTimerDurationInput("invalid")
        }
        `when`(extraTimersUserInputRepository.timerData).thenReturn(MutableStateFlow(listOf(invalidExtraTimerInput)))

        val result = defaultInputValidator.validateAllInputs(
            timerDurationInput,
            setTimerDurationInputError,
            extraTimersUserInputRepository,
            viewModel,
            this,
            true,
        )

        // Check that the result is Invalid and timer duration input error is not empty
        assert(result is Err)
        assert(invalidExtraTimerInput.inputs.timerDurationInputError!!.isNotEmpty())
    }

    @Test
    fun validatesAllInputsReturnsInvalidWhenExtraTimerDurationIsGreaterThanMainTimerDuration() = runTest {
        val mainTimerDurationInput = MutableStateFlow("10") // 10 seconds
        val setMainTimerDurationInputError: (String?) -> Unit = {}

        // Mock an extra timer with a duration greater than the main timer
        val extraTimer = ExtraTimerUserInputData().apply {
            inputs.updateTimerDurationInput("15") // 15 seconds
        }
        `when`(extraTimersUserInputRepository.timerData).thenReturn(MutableStateFlow(listOf(extraTimer)))

        val result = defaultInputValidator.validateAllInputs(
            mainTimerDurationInput,
            setMainTimerDurationInputError,
            extraTimersUserInputRepository,
            viewModel,
            this,
            true,
        )

        // Check that the result is Invalid
        assert(result is Err)
        // Check the specific error message for the extra timer
        assert(
            extraTimer.inputs.timerDurationInputError == "Extra timer time cannot be greater than main timer time."
        )
    }

    @Test
    fun validateAllInputsShouldReturnValidResultWhenAllInputsAreCorrect() = runTest {
        val mainTimerDurationInput = MutableStateFlow("30") // 30 minutes, valid
        val setMainTimerDurationInputError: (String?) -> Unit = {}

        // Mock an extra timer with a valid duration
        val extraTimer = ExtraTimerUserInputData().apply {
            inputs.updateTimerDurationInput("20") // 20 minutes, valid
        }
        `when`(extraTimersUserInputRepository.timerData).thenReturn(MutableStateFlow(listOf(extraTimer)))

        val result = defaultInputValidator.validateAllInputs(
            mainTimerDurationInput,
            setMainTimerDurationInputError,
            extraTimersUserInputRepository,
            viewModel,
            this,
            true,
        )

        // Check that the result is Valid
        assert(result is Ok)
    }

    @Test
    fun validateAllInputsClearsExistingErrorMessagesBeforeValidation() = runTest {
        // Initialize the repository
        val extraTimersUserInputsRepository: IExtraTimersUserInputsRepository = GlobalContext.get().get()

        // Create ExtraTimerUserInputData with initial error messages
        val extraTimer = ExtraTimerUserInputData().apply {
            // Set initial state
            inputs.timerDurationInputError = "Initial error"
        }

        // Update repository data
        extraTimersUserInputsRepository.updateData(listOf(extraTimer))

        // Initialize the validator
        val defaultInputValidator = DefaultInputValidator()

        // Run validation with valid inputs
        val validTimerDurationInput = MutableStateFlow("10")
        defaultInputValidator.validateAllInputs(
            validTimerDurationInput,
            { _ -> }, // Mock or implement as needed
            extraTimersUserInputsRepository,
            viewModel,
            this,
            true,
        )

        // Assert that the error messages are cleared
        var allErrorsCleared = true
        for (timer in extraTimersUserInputsRepository.timerData.value) {
            if (!timer.inputs.timerDurationInputError.isNullOrEmpty()) {
                allErrorsCleared = false
                break
            }
        }
        assert(allErrorsCleared)
    }

    companion object {
        @JvmStatic
        @BeforeClass
        fun beforeAllTests() {
            // Start Koin here
            startKoin {
                modules(testModule)
            }
        }

        @JvmStatic
        @AfterClass
        fun afterAllTests() {
            // Stop Koin after all tests
            stopKoin()
        }
    }
}

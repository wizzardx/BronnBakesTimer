package com.example.bronnbakestimer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("FunctionMaxLength")
class BronnBakesTimerViewModelTest {

    private lateinit var closeable: AutoCloseable

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: BronnBakesTimerViewModel

    private lateinit var timerRepository: DefaultTimerRepository
    private lateinit var extraTimersRepository: DefaultExtraTimersRepository
    private lateinit var errorRepository: DefaultErrorRepository
    private lateinit var timerManager: DefaultTimerManager
    private lateinit var inputValidator: DefaultInputValidator
    private lateinit var errorLoggerProvider: ErrorLoggerProvider

    @Before
    fun setUp() {
        startKoin {
            modules(testModule)
        }

        closeable = MockitoAnnotations.openMocks(this)

        Dispatchers.setMain(testDispatcher)

        timerRepository = DefaultTimerRepository()
        extraTimersRepository = DefaultExtraTimersRepository()
        errorRepository = DefaultErrorRepository()
        timerManager = DefaultTimerManager()
        inputValidator = DefaultInputValidator()
        errorLoggerProvider = testErrorLoggerProvider

        viewModel = BronnBakesTimerViewModel(
            timerRepository,
            timerManager,
            inputValidator,
            extraTimersRepository,
            errorRepository,
            errorLoggerProvider,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        // Any cleanup code if needed
        closeable.close()
        Dispatchers.resetMain() // Reset to the original Main dispatcher
        stopKoin()
    }

    @Test
    fun testInitialValues() {
        assertEquals("5", viewModel.timerDurationInput.value)
        assertNull(viewModel.timerDurationInputError)
        // Add assertions for other initial states as necessary
    }

    @Test
    fun testUpdateTimerDurationInput() = runTest {
        val testValue = "10"
        viewModel.updateTimerDurationInput(testValue)
        assertEquals(testValue, viewModel.timerDurationInput.value)
    }

    @Test
    fun testAreTextInputControlsEnabled() = runTest {
        val timerData = TimerData(10_000, isPaused = false, isFinished = false, beepTriggered = false)
        timerRepository.updateData(timerData)
        assertFalse(viewModel.areTextInputControlsEnabled(timerData))

        timerRepository.updateData(null)
        assertTrue(viewModel.areTextInputControlsEnabled(null))
    }

    @Test
    fun testOnButtonClick_StartTimers() = runTest {
        // Assuming 'onButtonClick' should start timers if they are not already started
        assertNull(timerRepository.timerData.value) // Initial state, no timer started

        // Simulate starting timers
        viewModel.onButtonClick()

        assertNotNull(timerRepository.timerData.value) // Check if the timer has started
        assertFalse(timerRepository.timerData.value!!.isPaused) // Timer should not be paused
    }

    @Test
    fun testOnButtonClick_PauseAndResumeTimers() = runTest {
        // Start the timer first
        timerRepository.updateData(TimerData(10_000, isPaused = false, isFinished = false, beepTriggered = false))

        // Simulate pausing timers
        viewModel.onButtonClick()
        assertTrue(timerRepository.timerData.value!!.isPaused) // Timer should be paused

        // Simulate resuming timers
        viewModel.onButtonClick()
        assertFalse(timerRepository.timerData.value!!.isPaused) // Timer should be resumed
    }

    @Test
    fun testInputValidation_InvalidInput() = runTest {
        val invalidInput = "invalid"
        viewModel.updateTimerDurationInput(invalidInput)
        val setter = { error: String ->
            viewModel.timerDurationInputError = error
        }
        val result = inputValidator.validateAllInputs(viewModel.timerDurationInput, setter, extraTimersRepository)
        // TODO: Check the above function, it looks like it can (depending on some weird condition) return the
        //       opposite boolean value compared to what you'd expect.
        assertFalse(result)
        assertNotNull(viewModel.timerDurationInputError) // Error should be set
    }

    // Additional test cases for error handling and extra timer functionalities
}

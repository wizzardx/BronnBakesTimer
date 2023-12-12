package com.example.bronnbakestimer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import org.mockito.Mock
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import kotlin.test.assertIs

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
        assertIs<ValidationResult.Invalid>(result)
        assertNotNull(viewModel.timerDurationInputError) // Error should be set
    }

    @Test
    fun testExtraTimerRemainingTime_WithActiveMainTimer() = runTest {
        // Set up main timer data to simulate an active main timer (e.g., 30 seconds remaining)
        val mainTimerData = TimerData(30_000, isPaused = false, isFinished = false, beepTriggered = false)
        timerRepository.updateData(mainTimerData)

        // Create an extra timer with a specific time remaining (e.g., 15 seconds)
        val extraTimerData = ExtraTimerData(
            data = TimerData(15_000, isPaused = false, isFinished = false, beepTriggered = false),
            inputs = ExtraTimerInputsData() // Assuming default constructor or predefined inputs
        )
        val remainingTimeFlow = viewModel.extraTimerRemainingTime(extraTimerData)

        // Assert that the remaining time is calculated correctly
        assertEquals("00:15", remainingTimeFlow.value)
    }

    @Test
    fun testExtraTimerRemainingTime_WithInactiveMainTimer() = runTest {
        // Set up main timer data to simulate an inactive main timer (null)
        timerRepository.updateData(null)

        // Create an extra timer with a specific time remaining (e.g., 15 seconds)
        val extraTimerData = ExtraTimerData(
            data = TimerData(15_000, isPaused = false, isFinished = false, beepTriggered = false),
            inputs = ExtraTimerInputsData() // Assuming default constructor or predefined inputs
        )
        val remainingTimeFlow = viewModel.extraTimerRemainingTime(extraTimerData)

        // Assert that the remaining time is calculated correctly for inactive main timer
        assertEquals("05:00", remainingTimeFlow.value)
    }

    @Test
    fun testOnButtonClickExceptionHandling() = runTest {
        // Create a spy of the real DefaultTimerManager instance
        val timerManagerSpy = spy(DefaultTimerManager())

        // Stub the pauseTimers method to throw an exception when called
        doThrow(RuntimeException("Simulated Exception")).whenever(timerManagerSpy).pauseTimers(timerRepository)

        // Reinitialize the ViewModel with the spy
        viewModel = BronnBakesTimerViewModel(
            timerRepository,
            timerManagerSpy, // Replace the real instance with the spy
            inputValidator,
            extraTimersRepository,
            errorRepository,
            errorLoggerProvider
        )

        // Set up the ViewModel state to ensure pauseTimers will be called
        val timerData = TimerData(10_000, isPaused = false, isFinished = false, beepTriggered = false)
        timerRepository.updateData(timerData)

        // Call onButtonClick which should internally call timerManager.pauseTimers
        viewModel.onButtonClick()

        // Collect the latest error message from errorMessage StateFlow
        val errorMessage = errorRepository.errorMessage.first()
        assertNotNull(errorMessage)
        assertEquals("Simulated Exception", errorMessage)
    }

    @Test
    fun testOnResetClickExceptionHandling() = runTest {
        // Assuming timerManager is a mock and can be set to throw an exception
        val timerManagerMock = mock<DefaultTimerManager>()
        whenever(timerManagerMock.clearResources(any())).thenThrow(RuntimeException("Simulated Reset Exception"))

        // Set the mock to your ViewModel
        viewModel = BronnBakesTimerViewModel(
            timerRepository,
            timerManagerMock, // Use the mock here
            inputValidator,
            extraTimersRepository,
            errorRepository,
            errorLoggerProvider,
        )

        // Call onResetClick
        viewModel.onResetClick()

        // Verify that the exception is handled
        val errorMessage = errorRepository.errorMessage.first()
        assertNotNull(errorMessage)
        assertEquals("Simulated Reset Exception", errorMessage)
    }
    @Test
    fun testStartTimersWithValidInput() = runTest {
        // Set a valid timer duration input
        viewModel.updateTimerDurationInput("10")

        // Attempt to start timers
        viewModel.startTimersIfValid()

        // Verify that the timers started correctly
        val timerData = timerRepository.timerData.value
        assertNotNull(timerData)
        assertEquals(10 * 60 * 1000L, timerData?.millisecondsRemaining) // 10 minutes in milliseconds
    }

    @Test
    fun testStartTimersWithInvalidInput() = runTest {
        // Set an invalid timer duration input
        viewModel.updateTimerDurationInput("invalid")

        // Attempt to start timers
        viewModel.startTimersIfValid()

        // Verify that the timers did not start
        val timerData = timerRepository.timerData.value
        assertNull(timerData)
    }

    @Test
    fun testTimerResetFunctionality() = runTest {
        // Setup and start timers
        val timerData = TimerData(10000, false, false, false)
        timerRepository.updateData(timerData)

        // Call onResetClick to reset timers
        viewModel.onResetClick()

        // Assertions to verify timers are reset
        val resetTimerData = timerRepository.timerData.value
        assertNull(resetTimerData) // Assuming reset sets the timerData to null
    }

    @Test
    fun testAddTimer() = runTest {
        val initialCount = extraTimersRepository.timerData.value.size

        viewModel.onAddTimerClicked()

        val newCount = extraTimersRepository.timerData.value.size
        assertEquals(initialCount + 1, newCount)
    }

    @Test
    fun testRemoveTimer() = runTest {
        // Ensure there's at least one timer to remove
        val initialTimers = listOf(
            ExtraTimerData(
                TimerData(
                    10_000,
                    isPaused = false,
                    isFinished = false,
                    beepTriggered = false
                ),
                ExtraTimerInputsData()
            )
        )
        extraTimersRepository.updateData(initialTimers)

        val timerId = extraTimersRepository.timerData.value.first().id

        viewModel.onRemoveTimerClicked(timerId)

        val remainingTimers = extraTimersRepository.timerData.value
        assertTrue(remainingTimers.none { it.id == timerId })
    }

    @Test
    fun testInputUpdateEffects() = runTest {
        // Update timer duration input
        viewModel.updateTimerDurationInput("15")

        // Assertions to verify changes in ViewModel's state or properties
        assertEquals("15", viewModel.timerDurationInput.value)
        // Add additional assertions if the update affects other properties or states
    }

    @Test
    fun testSuccessfulUpdateOfExtraTimers() = runTest {
        // Initialize extra timers with initial values
        val initialExtraTimer1 = ExtraTimerInputsData().apply { updateTimerDurationInput("5") } // 5 minutes
        val initialExtraTimer2 = ExtraTimerInputsData().apply { updateTimerDurationInput("10") } // 10 minutes

        val extraTimerData1 = ExtraTimerData(
            TimerData(0, isPaused = false, isFinished = false, beepTriggered = false),
            initialExtraTimer1
        )
        val extraTimerData2 = ExtraTimerData(
            TimerData(0, isPaused = false, isFinished = false, beepTriggered = false),
            initialExtraTimer2
        )

        extraTimersRepository.updateData(listOf(extraTimerData1, extraTimerData2))

        // Update the main timer duration input as a prerequisite for starting timers
        viewModel.updateTimerDurationInput("15") // 15 minutes for the main timer

        // Start timers, which should update extra timers' millisecondsRemaining based on inputs
        viewModel.startTimersIfValid()

        // Retrieve updated extra timers
        val updatedExtraTimers = extraTimersRepository.timerData.value

        // Verify that the millisecondsRemaining is updated correctly
        assertEquals(5 * 60 * 1000L, updatedExtraTimers[0].data.millisecondsRemaining) // 5 minutes in milliseconds
        assertEquals(10 * 60 * 1000L, updatedExtraTimers[1].data.millisecondsRemaining) // 10 minutes in milliseconds
    }
}

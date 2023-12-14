package com.example.bronnbakestimer.viewmodel

import androidx.annotation.VisibleForTesting
import com.example.bronnbakestimer.di.testModule
import com.example.bronnbakestimer.logic.DefaultInputValidator
import com.example.bronnbakestimer.logic.DefaultTimerManager
import com.example.bronnbakestimer.logic.IInputValidator
import com.example.bronnbakestimer.model.ExtraTimerInputsData
import com.example.bronnbakestimer.model.ExtraTimerUserInputData
import com.example.bronnbakestimer.provider.IErrorLoggerProvider
import com.example.bronnbakestimer.repository.DefaultErrorRepository
import com.example.bronnbakestimer.repository.DefaultExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.DefaultExtraTimersUserInputsRepository
import com.example.bronnbakestimer.repository.DefaultTimerRepository
import com.example.bronnbakestimer.repository.IErrorRepository
import com.example.bronnbakestimer.repository.IExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.IExtraTimersUserInputsRepository
import com.example.bronnbakestimer.repository.ITimerRepository
import com.example.bronnbakestimer.service.ITimerManager
import com.example.bronnbakestimer.service.SingleTimerCountdownData
import com.example.bronnbakestimer.service.TimerData
import com.example.bronnbakestimer.util.InvalidTimerDurationException
import com.example.bronnbakestimer.util.Seconds
import com.example.bronnbakestimer.util.TimerUserInputDataId
import com.example.bronnbakestimer.util.testErrorLoggerProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.mockito.MockitoAnnotations
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("FunctionMaxLength")
class BronnBakesTimerViewModelTest {

    private lateinit var closeable: AutoCloseable

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: BronnBakesTimerViewModel

    private lateinit var mainTimerRepository: ITimerRepository
    private lateinit var extraTimersUserInputRepository: IExtraTimersUserInputsRepository
    private lateinit var extraTimersCountdownRepository: IExtraTimersCountdownRepository
    private lateinit var errorRepository: DefaultErrorRepository
    private lateinit var timerManager: DefaultTimerManager
    private lateinit var inputValidator: DefaultInputValidator
    private lateinit var errorLoggerProvider: IErrorLoggerProvider

    @Before
    fun setUp() {
        startKoin {
            modules(testModule)
        }

        closeable = MockitoAnnotations.openMocks(this)

        Dispatchers.setMain(testDispatcher)

        mainTimerRepository = DefaultTimerRepository()
        extraTimersUserInputRepository = DefaultExtraTimersUserInputsRepository()
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
    fun `configControlsEnabled is false when timer is active`() = runTest {
        // Arrange: Simulate an active timer
        val activeTimerData = TimerData(10_000, isPaused = false, isFinished = false, beepTriggered = false)
        mainTimerRepository.updateData(activeTimerData)

        // Act: Collect the latest value of configControlsEnabled
        delay(100) // Small delay to wait for flow to emit
        val isEnabled = viewModel.configControlsEnabled.value //  value

        // Assert: configControlsEnabled should be false when the timer is active
        assertFalse(isEnabled, "configControlsEnabled should be false when timer is active")
    }

    @Test
    fun `configControlsEnabled is true when timer is inactive`() = runTest {
        // Arrange: Simulate no active timer
        mainTimerRepository.updateData(null)

        // Act: Collect the latest value of configControlsEnabled
        delay(100) // Small delay to wait for flow to emit
        val isEnabled = viewModel.configControlsEnabled.value

        // Assert: configControlsEnabled should be true when there is no active timer
        assertTrue(isEnabled, "configControlsEnabled should be true when there is no active timer")
    }

    @Test
    fun testOnButtonClick_StartTimers() = runTest {
        // Assuming 'onButtonClick' should start timers if they are not already started
        assertNull(mainTimerRepository.timerData.value) // Initial state, no timer started

        // Simulate starting timers
        viewModel.onButtonClick()

        assertNotNull(mainTimerRepository.timerData.value) // Check if the timer has started
        assertFalse(mainTimerRepository.timerData.value!!.isPaused) // Timer should not be paused
    }

    @Test
    fun testOnButtonClick_PauseAndResumeTimers() = runTest {
        // Start the timer first
        mainTimerRepository.updateData(TimerData(10_000, isPaused = false, isFinished = false, beepTriggered = false))

        // Simulate pausing timers
        viewModel.onButtonClick()
        assertTrue(mainTimerRepository.timerData.value!!.isPaused) // Timer should be paused

        // Simulate resuming timers
        viewModel.onButtonClick()
        assertFalse(mainTimerRepository.timerData.value!!.isPaused) // Timer should be resumed
    }

    @Test
    fun testInputValidation_InvalidInput() = runTest {
        // Arrange
        val invalidInput = "invalid"
        viewModel.updateTimerDurationInput(invalidInput)

        // Act
        viewModel.startTimersIfValid()

        // Assert
        assertNotNull(viewModel.timerDurationInputError, "Invalid input should set an error message.")
    }

    @Test
    fun testExtraTimerRemainingTime_WithActiveMainTimer() = runTest {
        // Arrange
        val mainTimerSecondsRemaining = Seconds(30) // Assuming seconds
        val extraTimerUserInputData = ExtraTimerUserInputData() // Initialize with appropriate data
        val extraTimerRemainingSeconds = MutableStateFlow(Seconds(15)) // Example: 15 seconds

        // Mock or set timerDurationInput, as per your application's structure

        // Act
        val remainingTimeFlow = viewModel.extraTimerRemainingTime(
            extraTimerUserInputData,
            extraTimerRemainingSeconds,
            viewModel.timerDurationInput, // Assuming this is how you get the timer duration input
            mainTimerSecondsRemaining
        )

        // Assert
        assertEquals(
            "00:15",
            remainingTimeFlow.value,
            "Remaining time should be correctly calculated with an active main timer."
        )
    }

    @Test
    fun testExtraTimerRemainingTime_WithInactiveMainTimer() = runTest {
        // Arrange
        val extraTimerUserInputData = ExtraTimerUserInputData() // Initialize with appropriate data
        val extraTimerRemainingSeconds = MutableStateFlow(Seconds(15)) // Example: 15 seconds
        val mainTimerSecondsRemaining: Seconds? = null // Main timer inactive

        // Mock or set timerDurationInput, as per your application's structure

        // Act
        val remainingTimeFlow = viewModel.extraTimerRemainingTime(
            extraTimerUserInputData,
            extraTimerRemainingSeconds,
            viewModel.timerDurationInput, // Assuming this is how you get the timer duration input
            mainTimerSecondsRemaining
        )

        // Assert
        assertEquals(
            "05:00",
            remainingTimeFlow.value,
            "Remaining time should be correctly calculated with an inactive main timer."
        )
    }

    @Test
    fun testOnButtonClickExceptionHandling() = runTest {
        // Subclass of BronnBakesTimerViewModel for testing
        class TestViewModel(
            timerRepository: ITimerRepository,
            timerManager: ITimerManager,
            inputValidator: IInputValidator,
            extraTimersUserInputsRepository: IExtraTimersUserInputsRepository,
            extraTimersCountdownRepository: IExtraTimersCountdownRepository,
            errorRepository: IErrorRepository,
            errorLoggerProvider: IErrorLoggerProvider
        ) : BronnBakesTimerViewModel(
            timerRepository,
            timerManager,
            inputValidator,
            extraTimersUserInputsRepository,
            extraTimersCountdownRepository,
            errorRepository,
            errorLoggerProvider
        ) {
            @Suppress("TooGenericExceptionThrown")
            @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
            override fun startTimersIfValid() {
                throw RuntimeException("Simulated Exception")
            }
        }

        // Arrange
        val timerRepository = DefaultTimerRepository()
        val timerManager = DefaultTimerManager()
        val inputValidator = DefaultInputValidator()
        val extraTimersUserInputsRepository = DefaultExtraTimersUserInputsRepository()
        val extraTimersCountdownRepository = DefaultExtraTimersCountdownRepository()
        val errorRepository = DefaultErrorRepository()
        val errorLoggerProvider = testErrorLoggerProvider

        val testViewModel = TestViewModel(
            timerRepository,
            timerManager,
            inputValidator,
            extraTimersUserInputsRepository,
            extraTimersCountdownRepository,
            errorRepository,
            errorLoggerProvider
        )

        // Act
        testViewModel.onButtonClick() // This should trigger the exception

        // Assert
        val errorMessage = errorRepository.errorMessage.value
        assertEquals("Simulated Exception", errorMessage, "Exception should be caught and logged.")
    }

    @Suppress("SwallowedException")
    @Test
    fun testOnResetClickExceptionHandling() = runTest {
        // Arrange
        val timerRepository = DefaultTimerRepository()
        val timerManager = DefaultTimerManager()
        val inputValidator = DefaultInputValidator()
        val extraTimersUserInputsRepository = DefaultExtraTimersUserInputsRepository()
        val extraTimersCountdownRepository = DefaultExtraTimersCountdownRepository()
        val errorRepository = DefaultErrorRepository()
        val errorLoggerProvider = testErrorLoggerProvider // Or runtimeErrorLoggerProvider

        val viewModel = BronnBakesTimerViewModel(
            timerRepository,
            timerManager,
            inputValidator,
            extraTimersUserInputsRepository,
            extraTimersCountdownRepository,
            errorRepository,
            errorLoggerProvider
        )

        // Act
        viewModel.onResetClick(testThrowingException = true)

        // Assert
        assertNotNull(errorRepository.errorMessage.value, "Exception should be caught and logged.")
    }

    @Test
    fun testStartTimersWithValidInput() = runTest {
        // Set a valid timer duration input
        viewModel.updateTimerDurationInput("10")

        // Attempt to start timers
        viewModel.startTimersIfValid()

        // Verify that the timers started correctly
        val timerData = mainTimerRepository.timerData.value
        assertNotNull(timerData)
        assertEquals(10 * 60 * 1000, timerData.millisecondsRemaining) // 10 minutes in milliseconds
    }

    @Test
    fun testStartTimersWithInvalidInput() = runTest {
        // Set an invalid timer duration input
        viewModel.updateTimerDurationInput("invalid")

        // Attempt to start timers
        viewModel.startTimersIfValid()

        // Verify that the timers did not start
        val timerData = mainTimerRepository.timerData.value
        assertNull(timerData)
    }

    @Test
    fun testTimerResetFunctionality() = runTest {
        // Setup and start timers
        val timerData = TimerData(10_000, isPaused = false, isFinished = false, beepTriggered = false)
        mainTimerRepository.updateData(timerData)

        // Call onResetClick to reset timers
        viewModel.onResetClick()

        // Assertions to verify timers are reset
        val resetTimerData = mainTimerRepository.timerData.value
        assertNull(resetTimerData) // Assuming reset sets the timerData to null
    }

    @Test
    fun testAddTimer() = runTest {
        val initialCount = extraTimersUserInputRepository.timerData.value.size

        viewModel.onAddTimerClicked()

        val newCount = extraTimersUserInputRepository.timerData.value.size
        assertEquals(initialCount + 1, newCount)
    }

    @Test
    fun testRemoveTimer() = runTest {
        // Arrange: Add a timer to the repository
        val initialTimerData = ExtraTimerUserInputData()
        extraTimersUserInputRepository.updateData(listOf(initialTimerData))

        val initialTimers = extraTimersUserInputRepository.timerData.value
        val timerToRemove = initialTimers.first()

        // Act: Remove the timer
        viewModel.onRemoveTimerClicked(timerToRemove.id)

        // Assert: Check if the timer has been removed
        val remainingTimers = extraTimersUserInputRepository.timerData.value
        assertFalse(remainingTimers.any { it.id == timerToRemove.id }, "The timer should be removed.")
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
    fun testStartTimers_RemovesExtraTimersNotInUserInputs() = runTest {
        // Arrange: Create a timer that will not be in the user inputs
        val extraTimerIdNotInInputs = TimerUserInputDataId.randomId()
        val countdownDataNotInInputs = SingleTimerCountdownData(
            data = TimerData(
                millisecondsRemaining = 60_000,
                isPaused = false,
                beepTriggered = false,
                isFinished = false
            ),
            useInputTimerId = extraTimerIdNotInInputs
        )

        // Add this timer to the countdown repository
        extraTimersCountdownRepository.updateData(
            ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>().apply {
                put(extraTimerIdNotInInputs, countdownDataNotInInputs)
            }
        )

        // Ensure there are no timers in the user inputs repository
        extraTimersUserInputRepository.updateData(listOf())

        // Act: Call startTimers in the ViewModel
        viewModel.startTimers()

        // Assert: The timer not present in user inputs should be removed from the countdown repository
        val countdownData = extraTimersCountdownRepository.timerData.value
        assertFalse(
            countdownData.containsKey(extraTimerIdNotInInputs),
            "Extra timer not in user inputs should be removed."
        )
    }

    @Test
    fun testStartTimers_AddsNewExtraTimersFromUserInputs() = runTest {
        // Arrange: Create a new timer in the user inputs repository
        val newExtraTimerId = TimerUserInputDataId.randomId()
        val newExtraTimerInputsData = ExtraTimerInputsData().apply {
            updateTimerDurationInput("10") // Set duration input to 10 minutes
        }
        val newExtraTimerUserInputData = ExtraTimerUserInputData(
            inputs = newExtraTimerInputsData,
            id = newExtraTimerId
        )

        // Add this new timer to the user inputs repository
        extraTimersUserInputRepository.updateData(listOf(newExtraTimerUserInputData))

        // Ensure the new timer does not exist in the countdown repository
        extraTimersCountdownRepository.updateData(ConcurrentHashMap())

        // Act: Call startTimers in the ViewModel
        viewModel.startTimers()

        // Assert: The new timer from user inputs should be added to the countdown repository
        val countdownData = extraTimersCountdownRepository.timerData.value
        assertTrue(countdownData.containsKey(newExtraTimerId), "New extra timer should be added.")
        val timerData = countdownData[newExtraTimerId]
        assertNotNull(timerData)
        assertEquals(10 * 60 * 1000, timerData.data.millisecondsRemaining) // 10 minutes in milliseconds
    }

    @Test
    fun testStartTimers_UpdatesExistingExtraTimersFromUserInputs() = runTest {
        // Arrange: Create an existing timer in both user inputs and countdown repositories
        val existingExtraTimerId = TimerUserInputDataId.randomId()
        val existingExtraTimerInputsData = ExtraTimerInputsData().apply {
            updateTimerDurationInput("5") // Initially set to 5 minutes
        }
        val existingExtraTimerUserInputData = ExtraTimerUserInputData(
            inputs = existingExtraTimerInputsData,
            id = existingExtraTimerId
        )
        val existingTimerCountdownData = SingleTimerCountdownData(
            data = TimerData(millisecondsRemaining = 5 * 60 * 1000), // 5 minutes in milliseconds
            useInputTimerId = existingExtraTimerId
        )

        // Add this existing timer to both repositories
        extraTimersUserInputRepository.updateData(listOf(existingExtraTimerUserInputData))
        extraTimersCountdownRepository.updateData(
            ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>().apply {
                put(existingExtraTimerId, existingTimerCountdownData)
            }
        )

        // Change the duration input for this timer
        existingExtraTimerInputsData.updateTimerDurationInput("10") // Change to 10 minutes

        // Act: Call startTimers in the ViewModel
        viewModel.startTimers()

        // Assert: The existing timer's data in countdown repository should be updated
        val countdownData = extraTimersCountdownRepository.timerData.value
        assertTrue(countdownData.containsKey(existingExtraTimerId), "Existing extra timer should be updated.")
        val updatedTimerData = countdownData[existingExtraTimerId]
        assertNotNull(updatedTimerData)
        assertEquals(
            10 * 60 * 1000,
            updatedTimerData.data.millisecondsRemaining
        ) // Updated to 10 minutes in milliseconds
    }

    @Test
    fun testStartTimers_ThrowsInvalidTimerDurationExceptionOnInvalidInput() = runTest {
        // Arrange: Set an invalid timer duration input that will cause userInputToMillis to return Err
        val invalidInput = "invalid"
        viewModel.updateTimerDurationInput(invalidInput)

        // Act & Assert: Expect the InvalidTimerDurationException to be thrown
        val exception = assertFailsWith<InvalidTimerDurationException> {
            viewModel.startTimers()
        }

        // Verify that the exception message contains the expected error message
        assertTrue(
            exception.message?.contains("Invalid timer duration input") == true,
            "Exception message should indicate invalid timer duration input."
        )
    }

    @Test
    fun testStartTimers_ThrowsInvalidTimerDurationExceptionForInvalidExtraTimerInput() = runTest {
        // Arrange: Add an extra timer with invalid input
        val invalidInput = "invalid"
        val extraTimerUserInputData = ExtraTimerUserInputData(
            inputs = ExtraTimerInputsData().apply {
                updateTimerDurationInput(invalidInput)
            },
            id = TimerUserInputDataId.randomId()
        )
        extraTimersUserInputRepository.updateData(listOf(extraTimerUserInputData))

        // Act & Assert: Expect the InvalidTimerDurationException to be thrown for invalid extra timer input
        val exception = assertFailsWith<InvalidTimerDurationException> {
            viewModel.startTimers()
        }

        // Verify that the exception message contains the expected error message for the extra timer
        assertTrue(
            exception.message?.contains("Invalid timer duration input") == true,
            "Exception message should indicate invalid timer duration input for extra timer."
        )
    }

    @Test
    fun testGetTotalSeconds_ReturnsZeroOnInvalidInputWhenMainTimerInactive() = runTest {
        // Arrange: Create a mock StateFlow for remaining seconds and set main timer as inactive
        val mockRemainingSecondsFlow = MutableStateFlow(Seconds(30)) // Example value, adjust as needed
        val mainTimerActive = false
        val invalidInput = "invalid"
        viewModel.updateTimerDurationInput(invalidInput)

        // Act: Call getTotalSeconds on the mock StateFlow with the invalid input and inactive timer
        val totalSeconds = mockRemainingSecondsFlow.getTotalSeconds(mainTimerActive, viewModel.timerDurationInput)

        // Assert: Total seconds should be 0 when main timer is inactive and input is invalid
        assertEquals(0, totalSeconds.value, "Total seconds should be 0 for invalid input when main timer is inactive.")
    }
}

package com.example.bronnbakestimer.viewmodel

import androidx.annotation.VisibleForTesting
import com.example.bronnbakestimer.di.testModule
import com.example.bronnbakestimer.logic.DefaultInputValidator
import com.example.bronnbakestimer.logic.IInputValidator
import com.example.bronnbakestimer.model.ExtraTimerInputsData
import com.example.bronnbakestimer.model.ExtraTimerUserInputData
import com.example.bronnbakestimer.provider.IErrorLoggerProvider
import com.example.bronnbakestimer.repository.DefaultErrorRepository
import com.example.bronnbakestimer.repository.DefaultExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.DefaultExtraTimersUserInputsRepository
import com.example.bronnbakestimer.repository.DefaultMainTimerRepository
import com.example.bronnbakestimer.repository.IErrorRepository
import com.example.bronnbakestimer.repository.IExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.IExtraTimersUserInputsRepository
import com.example.bronnbakestimer.repository.IMainTimerRepository
import com.example.bronnbakestimer.service.DefaultTimerManager
import com.example.bronnbakestimer.service.ITimerManager
import com.example.bronnbakestimer.service.SingleTimerCountdownData
import com.example.bronnbakestimer.service.TimerData
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
import org.junit.Before
import org.junit.Test
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.mockito.MockitoAnnotations
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("FunctionMaxLength")
class BronnBakesTimerViewModelTest {
    private lateinit var closeable: AutoCloseable

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: BronnBakesTimerViewModel

    private lateinit var mainTimerRepository: IMainTimerRepository
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

        mainTimerRepository = DefaultMainTimerRepository()
        extraTimersUserInputRepository = DefaultExtraTimersUserInputsRepository()
        extraTimersCountdownRepository = DefaultExtraTimersCountdownRepository()
        errorRepository = DefaultErrorRepository()
        timerManager =
            DefaultTimerManager(mainTimerRepository, extraTimersCountdownRepository, extraTimersUserInputRepository)
        inputValidator = DefaultInputValidator()
        errorLoggerProvider = testErrorLoggerProvider

        viewModel =
            BronnBakesTimerViewModel(
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
    fun testUpdateTimerDurationInput() =
        runTest {
            val testValue = "10"
            viewModel.updateTimerDurationInput(testValue)
            assertEquals(testValue, viewModel.timerDurationInput.value)
        }

    @Test
    fun `configControlsEnabled is false when timer is active`() =
        runTest {
            // Arrange: Simulate an active timer
            val activeTimerData = TimerData(10_000, isPaused = false, isFinished = false)
            mainTimerRepository.updateData(activeTimerData)

            // Act: Collect the latest value of configControlsEnabled
            delay(100) // Small delay to wait for flow to emit
            val isEnabled = viewModel.configControlsEnabled.value //  value

            // Assert: configControlsEnabled should be false when the timer is active
            assertFalse(isEnabled, "configControlsEnabled should be false when timer is active")
        }

    @Test
    fun `configControlsEnabled is true when timer is inactive`() =
        runTest {
            // Arrange: Simulate no active timer
            mainTimerRepository.updateData(null)

            // Act: Collect the latest value of configControlsEnabled
            delay(100) // Small delay to wait for flow to emit
            val isEnabled = viewModel.configControlsEnabled.value

            // Assert: configControlsEnabled should be true when there is no active timer
            assertTrue(isEnabled, "configControlsEnabled should be true when there is no active timer")
        }

    @Test
    fun testOnButtonClick_StartTimers() =
        runTest {
            // Assuming 'onButtonClick' should start timers if they are not already started
            assertNull(mainTimerRepository.timerData.value) // Initial state, no timer started

            // Simulate starting timers
            viewModel.onButtonClick()

            assertNotNull(mainTimerRepository.timerData.value) // Check if the timer has started
            assertFalse(mainTimerRepository.timerData.value!!.isPaused) // Timer should not be paused
        }

    @Test
    fun testOnButtonClick_PauseAndResumeTimers() =
        runTest {
            // Start the timer first
            mainTimerRepository.updateData(TimerData(10_000, isPaused = false, isFinished = false))

            // Simulate pausing timers
            viewModel.onButtonClick()
            assertTrue(mainTimerRepository.timerData.value!!.isPaused) // Timer should be paused

            // Simulate resuming timers
            viewModel.onButtonClick()
            assertFalse(mainTimerRepository.timerData.value!!.isPaused) // Timer should be resumed
        }

    @Test
    fun testInputValidation_InvalidInput() =
        runTest {
            // Arrange
            val invalidInput = "invalid"
            viewModel.updateTimerDurationInput(invalidInput)

            // Act
            viewModel.startTimersIfValid(true)

            // Assert
            assertNotNull(viewModel.timerDurationInputError, "Invalid input should set an error message.")
        }

    @Test
    fun testExtraTimerRemainingTime_WithActiveMainTimer() =
        runTest {
            // Arrange
            val mainTimerSecondsRemaining = Seconds(30) // Assuming seconds
            val extraTimerUserInputData = ExtraTimerUserInputData() // Initialize with appropriate data
            val extraTimerRemainingSeconds = MutableStateFlow(Seconds(15)) // Example: 15 seconds

            // Mock or set timerDurationInput, as per your application's structure

            // Act
            val remainingTimeFlow =
                viewModel.extraTimerRemainingTime(
                    extraTimerUserInputData,
                    extraTimerRemainingSeconds,
                    viewModel.timerDurationInput,
                    mainTimerSecondsRemaining,
                )

            // Assert
            assertEquals(
                "00:15",
                remainingTimeFlow.value,
                "Remaining time should be correctly calculated with an active main timer.",
            )
        }

    @Test
    fun testExtraTimerRemainingTime_WithInactiveMainTimer() =
        runTest {
            // Arrange
            val extraTimerUserInputData = ExtraTimerUserInputData() // Initialize with appropriate data
            val extraTimerRemainingSeconds = MutableStateFlow(Seconds(15)) // Example: 15 seconds
            val mainTimerSecondsRemaining: Seconds? = null // Main timer inactive

            // Mock or set timerDurationInput, as per your application's structure

            // Act
            val remainingTimeFlow =
                viewModel.extraTimerRemainingTime(
                    extraTimerUserInputData,
                    extraTimerRemainingSeconds,
                    viewModel.timerDurationInput,
                    mainTimerSecondsRemaining,
                )

            // Assert
            assertEquals(
                "05:00",
                remainingTimeFlow.value,
                "Remaining time should be correctly calculated with an inactive main timer.",
            )
        }

    @Test
    fun testOnButtonClickExceptionHandling() =
        runTest {
            // Subclass of BronnBakesTimerViewModel for testing
            class TestViewModel(
                mainTimerRepository: IMainTimerRepository,
                timerManager: ITimerManager,
                inputValidator: IInputValidator,
                extraTimersUserInputsRepository: IExtraTimersUserInputsRepository,
                extraTimersCountdownRepository: IExtraTimersCountdownRepository,
                errorRepository: IErrorRepository,
                errorLoggerProvider: IErrorLoggerProvider,
            ) : BronnBakesTimerViewModel(
                    mainTimerRepository,
                    timerManager,
                    inputValidator,
                    extraTimersUserInputsRepository,
                    extraTimersCountdownRepository,
                    errorRepository,
                    errorLoggerProvider,
                ) {
                @Suppress("TooGenericExceptionThrown")
                @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
                override fun startTimersIfValid(skipUiLogic: Boolean) {
                    throw RuntimeException("Simulated Exception")
                }
            }

            // Arrange
            val timerRepository = DefaultMainTimerRepository()
            val timerManager =
                DefaultTimerManager(mainTimerRepository, extraTimersCountdownRepository, extraTimersUserInputRepository)
            val inputValidator = DefaultInputValidator()
            val extraTimersUserInputsRepository = DefaultExtraTimersUserInputsRepository()
            val extraTimersCountdownRepository = DefaultExtraTimersCountdownRepository()
            val errorRepository = DefaultErrorRepository()
            val errorLoggerProvider = testErrorLoggerProvider

            val testViewModel =
                TestViewModel(
                    timerRepository,
                    timerManager,
                    inputValidator,
                    extraTimersUserInputsRepository,
                    extraTimersCountdownRepository,
                    errorRepository,
                    errorLoggerProvider,
                )

            // Act
            testViewModel.onButtonClick() // This should trigger the exception

            // Assert
            val errorMessage = errorRepository.errorMessage.value
            assertEquals("Simulated Exception", errorMessage, "Exception should be caught and logged.")
        }

    @Suppress("SwallowedException")
    @Test
    fun testOnResetClickExceptionHandling() =
        runTest {
            // Arrange
            val timerRepository = DefaultMainTimerRepository()
            val timerManager =
                DefaultTimerManager(mainTimerRepository, extraTimersCountdownRepository, extraTimersUserInputRepository)
            val inputValidator = DefaultInputValidator()
            val extraTimersUserInputsRepository = DefaultExtraTimersUserInputsRepository()
            val extraTimersCountdownRepository = DefaultExtraTimersCountdownRepository()
            val errorRepository = DefaultErrorRepository()
            val errorLoggerProvider = testErrorLoggerProvider // Or runtimeErrorLoggerProvider

            val viewModel =
                BronnBakesTimerViewModel(
                    timerRepository,
                    timerManager,
                    inputValidator,
                    extraTimersUserInputsRepository,
                    extraTimersCountdownRepository,
                    errorRepository,
                    errorLoggerProvider,
                )

            // Act
            viewModel.onResetClick(testThrowingException = true)

            // Assert
            assertNotNull(errorRepository.errorMessage.value, "Exception should be caught and logged.")
        }

    @Test
    fun testStartTimersWithValidInput() =
        runTest {
            // Set a valid timer duration input
            viewModel.updateTimerDurationInput("10")

            // Attempt to start timers
            viewModel.startTimersIfValid(true)

            // Verify that the timers started correctly
            val timerData = mainTimerRepository.timerData.value
            assertNotNull(timerData)
            assertEquals(10 * 60 * 1000, timerData.millisecondsRemaining) // 10 minutes in milliseconds
        }

    @Test
    fun testStartTimersWithInvalidInput() =
        runTest {
            // Set an invalid timer duration input
            viewModel.updateTimerDurationInput("invalid")

            // Attempt to start timers
            viewModel.startTimersIfValid(true)

            // Verify that the timers did not start
            val timerData = mainTimerRepository.timerData.value
            assertNull(timerData)
        }

    @Test
    fun testTimerResetFunctionality() =
        runTest {
            // Setup and start timers
            val timerData = TimerData(10_000, isPaused = false, isFinished = false)
            mainTimerRepository.updateData(timerData)

            // Call onResetClick to reset timers
            viewModel.onResetClick()

            // Assertions to verify timers are reset
            val resetTimerData = mainTimerRepository.timerData.value
            assertNull(resetTimerData) // Assuming reset sets the timerData to null
        }

    @Test
    fun testAddTimer() =
        runTest {
            val initialCount = extraTimersUserInputRepository.timerData.value.size

            viewModel.onAddTimerClicked()

            val newCount = extraTimersUserInputRepository.timerData.value.size
            assertEquals(initialCount + 1, newCount)
        }

    @Test
    fun testRemoveTimer() =
        runTest {
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
    fun testInputUpdateEffects() =
        runTest {
            // Update timer duration input
            viewModel.updateTimerDurationInput("15")

            // Assertions to verify changes in ViewModel's state or properties
            assertEquals("15", viewModel.timerDurationInput.value)
            // Add additional assertions if the update affects other properties or states
        }

    @Test
    fun testStartTimersIfValid_RemovesExtraTimersNotInUserInputs() =
        runTest {
            // Arrange: Create a timer that will not be in the user inputs
            val extraTimerIdNotInInputs = TimerUserInputDataId.randomId()
            val countdownDataNotInInputs =
                SingleTimerCountdownData(
                    data =
                        TimerData(
                            millisecondsRemaining = 60_000,
                            isPaused = false,
                            isFinished = false,
                        ),
                    useInputTimerId = extraTimerIdNotInInputs,
                )

            // Add this timer to the countdown repository
            extraTimersCountdownRepository.updateData(
                ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>().apply {
                    put(extraTimerIdNotInInputs, countdownDataNotInInputs)
                },
            )

            // Ensure there are no timers in the user inputs repository
            extraTimersUserInputRepository.updateData(listOf())

            // Act: Call startTimers in the ViewModel
            viewModel.startTimersIfValid(true)

            // Assert: The timer not present in user inputs should be removed from the countdown repository
            val countdownData = extraTimersCountdownRepository.timerData.value
            assertFalse(
                countdownData.containsKey(extraTimerIdNotInInputs),
                "Extra timer not in user inputs should be removed.",
            )
        }

    @Test
    fun testStartTimersIfValid_AddsNewExtraTimersFromUserInputs() =
        runTest {
            // Arrange: Create a new timer in the user inputs repository
            val newExtraTimerId = TimerUserInputDataId.randomId()
            val newExtraTimerInputsData =
                ExtraTimerInputsData().apply {
                    updateTimerDurationInput("3") // Set duration input to 3 minutes
                }
            val newExtraTimerUserInputData =
                ExtraTimerUserInputData(
                    inputs = newExtraTimerInputsData,
                    id = newExtraTimerId,
                )

            // Add this new timer to the user inputs repository
            extraTimersUserInputRepository.updateData(listOf(newExtraTimerUserInputData))

            // Ensure the new timer does not exist in the countdown repository
            extraTimersCountdownRepository.updateData(ConcurrentHashMap())

            // Act: Call startTimers in the ViewModel
            viewModel.startTimersIfValid(true)

            // Assert: The new timer from user inputs should be added to the countdown repository
            val countdownData = extraTimersCountdownRepository.timerData.value
            assertTrue(countdownData.containsKey(newExtraTimerId), "New extra timer should be added.")
            val timerData = countdownData[newExtraTimerId]
            assertNotNull(timerData)
            assertEquals(3 * 60 * 1000, timerData.data.millisecondsRemaining) // 3 minutes in milliseconds
        }

    @Test
    fun testStartTimersIfValid_UpdatesExistingExtraTimersFromUserInputs() =
        runTest {
            // Arrange: Create an existing timer in both user inputs and countdown repositories
            val existingExtraTimerId = TimerUserInputDataId.randomId()
            val existingExtraTimerInputsData =
                ExtraTimerInputsData().apply {
                    updateTimerDurationInput("3") // Initially set to 3 minutes
                }
            val existingExtraTimerUserInputData =
                ExtraTimerUserInputData(
                    inputs = existingExtraTimerInputsData,
                    id = existingExtraTimerId,
                )
            val existingTimerCountdownData =
                SingleTimerCountdownData(
                    // 3 minutes in milliseconds:
                    data = TimerData(millisecondsRemaining = 3 * 60 * 1000),
                    useInputTimerId = existingExtraTimerId,
                )

            // Add this existing timer to both repositories
            extraTimersUserInputRepository.updateData(listOf(existingExtraTimerUserInputData))
            extraTimersCountdownRepository.updateData(
                ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>().apply {
                    put(existingExtraTimerId, existingTimerCountdownData)
                },
            )

            // Change the duration input for this timer
            existingExtraTimerInputsData.updateTimerDurationInput("4") // Change to 4 minutes

            // Act: Call startTimers in the ViewModel
            viewModel.startTimersIfValid(true)

            // Assert: The existing timer's data in countdown repository should be updated
            val countdownData = extraTimersCountdownRepository.timerData.value
            assertTrue(countdownData.containsKey(existingExtraTimerId), "Existing extra timer should be updated.")
            val updatedTimerData = countdownData[existingExtraTimerId]
            assertNotNull(updatedTimerData)
            assertEquals(
                4 * 60 * 1000,
                updatedTimerData.data.millisecondsRemaining,
            ) // Updated to 4 minutes in milliseconds
        }

    @Test
    fun testGetTotalSeconds_ReturnsZeroOnInvalidInputWhenMainTimerInactive() =
        runTest {
            // Arrange: Create a mock StateFlow for remaining seconds and set main timer as inactive
            val mockRemainingSecondsFlow = MutableStateFlow(Seconds(30)) // Example value, adjust as needed
            val mainTimerActive = false
            val invalidInput = "invalid"
            viewModel.updateTimerDurationInput(invalidInput)

            // Act: Call getTotalSeconds on the mock StateFlow with the invalid input and inactive timer
            val totalSeconds = mockRemainingSecondsFlow.getTotalSeconds(mainTimerActive, viewModel.timerDurationInput)

            // Assert: Total seconds should be 0 when main timer is inactive and input is invalid
            assertEquals(
                0,
                totalSeconds.value,
                "Total seconds should be 0 for invalid input when main timer is inactive.",
            )
        }

    @Test
    fun testStartTimersWithValidExtraTimerInput_DoesNotSetIncorrectErrorMessages() =
        runTest {
            // Arrange: Add a timer and set its minutes input to 55
            viewModel.onAddTimerClicked()
            val newTimerData = extraTimersUserInputRepository.timerData.value.last()
            newTimerData.inputs.updateTimerDurationInput("55")

            // Act: Start the timers
            viewModel.startTimersIfValid(true)

            // Assert: Error messages should not be set incorrectly
            assertNull(viewModel.timerDurationInputError, "Main Timer's input error should not be set incorrectly.")

            // Access the error message from the error repository
            val errorRepository: IErrorRepository = GlobalContext.get().get()
            assertNull(
                errorRepository.errorMessage.value,
                "Error message at the bottom of the screen should not be set incorrectly.",
            )
        }

    @Test
    fun testStartButtonClearsBottomScreenError() =
        runTest {
            // Arrange: Set an error message
            errorRepository.updateData("Test error message")

            // Pre-check: Verify that the error message is initially set
            assertNotNull(errorRepository.errorMessage.value, "Error message should initially be set.")

            // Act: Simulate pressing the Start button
            viewModel.onButtonClick() // Assuming this method is connected to the Start button functionality

            // Assert: Error message should be cleared
            assertNull(errorRepository.errorMessage.value, "Error message should be cleared after pressing Start.")
        }

    @Test
    fun testResetButtonClearsBottomScreenError() =
        runTest {
            // Arrange: Set an error message
            errorRepository.updateData("Test error message")

            // Pre-check: Verify that the error message is initially set
            assertNotNull(errorRepository.errorMessage.value, "Error message should initially be set.")

            // Act: Simulate pressing the Reset button
            viewModel.onResetClick()

            // Assert: Error message should be cleared
            assertNull(errorRepository.errorMessage.value, "Error message should be cleared after pressing Reset.")
        }

    @Test
    fun testResetButtonClearsMainTimerInputError() =
        runTest {
            // Arrange: Set an error for the main timer input
            viewModel.timerDurationInputError = "Main timer input error"

            // Pre-check: Verify that the main timer input error is initially set
            assertNotNull(viewModel.timerDurationInputError, "Main timer input error should initially be set.")

            // Act: Simulate pressing the Reset button
            viewModel.onResetClick()

            // Assert: Main timer input error should be cleared
            assertNull(
                viewModel.timerDurationInputError,
                "Main timer input error should be cleared after pressing Reset.",
            )
        }

    @Test
    fun testResetButtonClearsExtraTimersInputErrors() =
        runTest {
            // Arrange: Set errors for an extra timer's duration and name inputs
            val extraTimerId = TimerUserInputDataId.randomId()
            val extraTimerUserInputData =
                ExtraTimerUserInputData(
                    id = extraTimerId,
                    inputs =
                        ExtraTimerInputsData().apply {
                            this.timerDurationInputError = "Extra timer duration input error"
                            this.timerNameInputError = "Extra timer name input error"
                        },
                )
            extraTimersUserInputRepository.updateData(listOf(extraTimerUserInputData))

            // Pre-check: Verify that the extra timer input errors are initially set
            val firstExtraTimerInputs = extraTimersUserInputRepository.timerData.value.first().inputs
            assertNotNull(
                firstExtraTimerInputs.timerDurationInputError,
                "Extra timer duration input error should initially be set.",
            )
            assertNotNull(
                firstExtraTimerInputs.timerNameInputError,
                "Extra timer name input error should initially be set.",
            )

            // Act: Simulate pressing the Reset button
            viewModel.onResetClick()

            // Assert: Extra timer input errors should be cleared
            assertNull(
                firstExtraTimerInputs.timerDurationInputError,
                "Extra timer duration input error should be cleared after pressing Reset.",
            )
            assertNull(
                firstExtraTimerInputs.timerNameInputError,
                "Extra timer name input error should be cleared after pressing Reset.",
            )
        }

    @Test
    fun testEmptyNameInputForExtraTimerSetsValidationErrorMessage() =
        runTest {
            // Arrange: Create an extra timer with an empty name input
            val emptyNameInput = "   " // Represents a name input with only whitespace
            val extraTimerId = TimerUserInputDataId.randomId()
            val extraTimerInputsData = ExtraTimerInputsData()
            extraTimerInputsData.updateTimerNameInput(emptyNameInput) // Update the name input

            val extraTimerUserInputData =
                ExtraTimerUserInputData(
                    id = extraTimerId,
                    inputs = extraTimerInputsData,
                )
            extraTimersUserInputRepository.updateData(listOf(extraTimerUserInputData))

            // Pre-check: Make sure that the name input has not yet been focused and scrolled to:
            val preCheckResultExtraTimerUserInputData = extraTimersUserInputRepository.timerData.value.first()
            assertEquals(
                0,
                preCheckResultExtraTimerUserInputData.inputs.focusOnTimerNameInputCount,
                "Empty name input should not yet have been focused.",
            )

            // Act: Trigger validation logic
            viewModel.startTimersIfValid(skipUiLogic = true)

            // Assert: Check if the appropriate error message is set for empty name input
            val resultExtraTimerUserInputData = extraTimersUserInputRepository.timerData.value.first()
            val expectedErrorMessage = "Extra timer name cannot be blank."
            assertEquals(
                expectedErrorMessage,
                resultExtraTimerUserInputData.inputs.timerNameInputError,
                "Empty name input should set a validation error message.",
            )

            // Make sure that the name input has been focused and scrolled to:
            assertEquals(
                1,
                resultExtraTimerUserInputData.inputs.focusOnTimerNameInputCount,
                "Empty name input should have been focused.",
            )
        }

    @Test
    fun testExtraTimerDurationInputErrorTriggersScrollAndFocus() =
        runTest {
            // Arrange: Create an extra timer with an invalid duration input
            val invalidDurationInput = "invalid" // Represents an invalid duration input
            val extraTimerId = TimerUserInputDataId.randomId()
            val extraTimerInputsData = ExtraTimerInputsData()
            extraTimerInputsData.updateTimerDurationInput(invalidDurationInput) // Update the duration input

            val extraTimerUserInputData =
                ExtraTimerUserInputData(
                    id = extraTimerId,
                    inputs = extraTimerInputsData,
                )
            extraTimersUserInputRepository.updateData(listOf(extraTimerUserInputData))

            // Pre-check: Make sure that the duration input has not yet been focused and scrolled to
            val preCheckResultExtraTimerUserInputData = extraTimersUserInputRepository.timerData.value.first()
            assertEquals(
                0,
                preCheckResultExtraTimerUserInputData.inputs.focusOnTimerDurationInputCount,
                "Extra timer duration input should not yet have been focused.",
            )

            // Act: Trigger validation logic
            viewModel.startTimersIfValid(skipUiLogic = true)

            // Assert: Check if the appropriate error message is set for invalid duration input
            val resultExtraTimerUserInputData = extraTimersUserInputRepository.timerData.value.first()
            assertNotNull(
                resultExtraTimerUserInputData.inputs.timerDurationInputError,
                "Invalid duration input should set a validation error message.",
            )

            // Make sure that the duration input has been focused and scrolled to
            assertEquals(
                1,
                resultExtraTimerUserInputData.inputs.focusOnTimerDurationInputCount,
                "Extra timer duration input should have been focused.",
            )
        }

    @Test
    fun testExtraTimerDurationGreaterThanMainTimerSetsErrorAndFocusesInput() =
        runTest {
            // Arrange: Set the main timer duration to a default value (e.g., 5 minutes)
            viewModel.updateTimerDurationInput("5")

            // Create an extra timer with a duration input greater than the main timer (e.g., 10 minutes)
            val extraTimerId = TimerUserInputDataId.randomId()
            val extraTimerInputsData = ExtraTimerInputsData(initialTimerDuration = "10")
            val extraTimerUserInputData =
                ExtraTimerUserInputData(
                    id = extraTimerId,
                    inputs = extraTimerInputsData,
                )
            extraTimersUserInputRepository.updateData(listOf(extraTimerUserInputData))

            // Pre-check: Ensure the duration input has not yet been focused and scrolled to
            val precheckExtraTimerInputs = extraTimersUserInputRepository.timerData.value.first().inputs
            assertEquals(
                0,
                precheckExtraTimerInputs.focusOnTimerDurationInputCount,
                "Extra timer duration input should not yet have been focused.",
            )

            // Act: Trigger the validation logic
            viewModel.startTimersIfValid(skipUiLogic = true)

            // Assert: Check if an error message is set for the extra timer's duration input
            val resultExtraTimerInputs = extraTimersUserInputRepository.timerData.value.first().inputs
            val expectedErrorMessage = "Extra timer time cannot be greater than main timer time."
            assertEquals(
                expectedErrorMessage,
                resultExtraTimerInputs.timerDurationInputError,
                "Extra timer duration input error should be set correctly.",
            )

            // Make sure the duration input has been focused and scrolled to
            assertEquals(
                1,
                resultExtraTimerInputs.focusOnTimerDurationInputCount,
                "Extra timer duration input should have been focused.",
            )
        }

    @Test
    fun testStartWithInvalidMainAndExtraTimerInput() =
        runTest {
            // Arrange: Set invalid inputs for the main timer and an extra timer
            val invalidMainTimerInput = "invalidMain"
            val invalidExtraTimerInput = "invalidExtra"
            viewModel.updateTimerDurationInput(invalidMainTimerInput)
            viewModel.onAddTimerClicked() // Add an extra timer
            val extraTimerData = extraTimersUserInputRepository.timerData.value.last()
            extraTimerData.inputs.updateTimerDurationInput(invalidExtraTimerInput)

            // Act: Attempt to start timers
            viewModel.startTimersIfValid(skipUiLogic = true)

            // Assert: Verify error messages are set for both main and extra timer inputs
            assertNotNull(viewModel.timerDurationInputError, "Main Timer's input error should be set.")
            assertEquals(
                "Invalid input",
                viewModel.timerDurationInputError,
            )

            val extraTimerInputError = extraTimerData.inputs.timerDurationInputError
            assertNotNull(extraTimerInputError, "Extra Timer's input error should be set.")
            assertEquals(
                "Invalid input",
                extraTimerInputError,
            )

            // Assert: Main timer input field should be focused and scrolled to
            assertEquals(
                1,
                viewModel.timerDurationInputFocusCount,
                "Main timer input should be focused.",
            )

            // Assert: Extra timer input field should not be focused
            assertEquals(
                0,
                extraTimerData.inputs.focusOnTimerDurationInputCount,
                "Extra timer input should not be focused.",
            )
        }

    @Test
    fun testTwoTimersWithNumericInputErrors_OnlyFirstFocused() =
        runTest {
            // Arrange: Add two extra timers with invalid numeric inputs
            viewModel.onAddTimerClicked()
            viewModel.onAddTimerClicked()

            val extraTimers = extraTimersUserInputRepository.timerData.value
            val firstExtraTimer = extraTimers[0]
            val secondExtraTimer = extraTimers[1]

            val invalidInput = "invalid"
            firstExtraTimer.inputs.updateTimerDurationInput(invalidInput)
            secondExtraTimer.inputs.updateTimerDurationInput(invalidInput)

            // Act: Attempt to start timers
            viewModel.startTimersIfValid(skipUiLogic = true)

            // Assert: Verify error messages are set for both extra timer inputs
            assertNotNull(
                firstExtraTimer.inputs.timerDurationInputError,
                "First extra timer's input error should be set.",
            )
            assertNotNull(
                secondExtraTimer.inputs.timerDurationInputError,
                "Second extra timer's input error should be set.",
            )

            // Assert: Only the first extra timer input field should be focused
            assertEquals(
                1,
                firstExtraTimer.inputs.focusOnTimerDurationInputCount,
                "First extra timer input should be focused.",
            )
            assertEquals(
                0,
                secondExtraTimer.inputs.focusOnTimerDurationInputCount,
                "Second extra timer input should not be focused.",
            )
        }

    @Test
    fun `testEmptyStringInputSetsDisplayedTimeToZero`() =
        runTest {
            // Arrange: Set the timer duration input to an empty string
            val emptyInput = ""
            viewModel.updateTimerDurationInput(emptyInput)

            // Act: Collect the latest value of totalTimeRemainingString
            delay(100) // Small delay to wait for flow to emit
            val displayedTime = viewModel.totalTimeRemainingString.value

            // Assert: The displayed time should be "00:00" for an empty string input
            assertEquals("00:00", displayedTime, "Displayed time should be '00:00' for empty input.")
        }
}

package com.example.bronnbakestimer.service

import com.example.bronnbakestimer.provider.CoroutineScopeProviderWrapper
import com.example.bronnbakestimer.provider.IMediaPlayerWrapper
import com.example.bronnbakestimer.repository.DefaultExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.DefaultTimerRepository
import com.example.bronnbakestimer.repository.IExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.ITimerRepository
import com.example.bronnbakestimer.util.IPhoneVibrator
import com.example.bronnbakestimer.util.TimerUserInputDataId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestTimeController(
    private val scheduler: TestCoroutineScheduler,
) : BaseTimeController() {
    private var timeMillis = 0L
    private var incrementAmount = 0 // Variable to hold the increment amount

    // Method to set the increment amount
    fun setIncrementAmount(amount: Int) {
        incrementAmount = amount
    }

    // Convert time from milliseconds to nanos and return:
    override fun nanoTime(): Long {
        timeMillis += incrementAmount // Increment timeMillis by the determined amount
        return TimeUnit.MILLISECONDS.toNanos(timeMillis)
    }

    override suspend fun delay(
        delayTimeMillis: Long,
        scope: CoroutineScope,
    ) {
        super.delay(delayTimeMillis, scope)
        timeMillis += delayTimeMillis
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun advanceTimeBy(delayTimeMillis: Long) {
        scheduler.advanceTimeBy(delayTimeMillis)
    }
}

class TestMediaPlayerWrapper : IMediaPlayerWrapper {
    var beepPlayed = false
    override fun playBeep() {
        beepPlayed = true
    }

    override fun release() {
        // Do nothing
    }
}

@Suppress("FunctionMaxLength", "LongMethod")
@ExperimentalCoroutinesApi
class CountdownLogicTest {

    private lateinit var mediaPlayerWrapper: TestMediaPlayerWrapper
    private lateinit var countdownLogic: CountdownLogic
    private lateinit var timeController: TestTimeController
    private lateinit var timerRepository: ITimerRepository
    private lateinit var testScope: CoroutineScope
    private lateinit var extraTimersCountdownRepository: IExtraTimersCountdownRepository

    @After
    fun tearDown() {
        // Reset the increment amount after each test
        timeController.setIncrementAmount(0)
    }

    private fun initScope(
        scope: TestScope,
        delayLambda: (suspend (Long) -> Unit),
    ) {
        val scheduler = TestCoroutineScheduler()
        val dispatcher = StandardTestDispatcher(scheduler)
        testScope = CoroutineScope(dispatcher + Job())
        timeController = TestTimeController(scheduler)
        timeController.setDelayLambda(delayLambda, scope)

        timerRepository = DefaultTimerRepository()
        mediaPlayerWrapper = TestMediaPlayerWrapper()
        extraTimersCountdownRepository = DefaultExtraTimersCountdownRepository()
        val coroutineScopeProvider = CoroutineScopeProviderWrapper(testScope)

        class TestPhoneVibrator : IPhoneVibrator {
            override fun vibrate() {
                // Do nothing
            }
        }

        // Create the CountdownLogic instance
        countdownLogic = CountdownLogic(
            timerRepository,
            mediaPlayerWrapper,
            coroutineScopeProvider,
            timeController,
            extraTimersCountdownRepository,
            phoneVibrator = TestPhoneVibrator()
        )
    }

    @Test
    fun `tick should do nothing when timer data is not set`() = runTest {
        // Initialize test environment
        initScope(this) {
            delay(it)
        }

        // Set the main timer data to null
        timerRepository.updateData(null)

        // Invoke the tick method
        countdownLogic.tick(1000) // Tick for 1 second

        // Verify that timer state remains null and no actions are performed
        val timerState = timerRepository.timerData.value
        assertNull(timerState, "Timer state should remain null")
        assertFalse(mediaPlayerWrapper.beepPlayed, "No beep should be played")
    }

    @Test
    fun `tick should do nothing when timer is paused`() = runTest {
        // Initialize test environment
        initScope(this) {
            delay(it)
        }

        // Set the timer state to paused
        val pausedTimerData = TimerData(
            millisecondsRemaining = 5000,
            isPaused = true,
            isFinished = false,
            beepTriggered = false
        )
        timerRepository.updateData(pausedTimerData)

        // Capture the initial state for comparison
        val initialState = timerRepository.timerData.value

        // Invoke the tick method
        countdownLogic.tick(1000) // Tick for 1 second

        // Verify that the timer state remains unchanged
        val updatedState = timerRepository.timerData.value
        assertEquals(initialState, updatedState, "Timer state should remain unchanged when paused")
        assertFalse(mediaPlayerWrapper.beepPlayed, "No beep should be played when timer is paused")
    }

    @Test
    fun `tick should do nothing when timer is finished`() = runTest {
        // Initialize test environment
        initScope(this) {
            delay(it)
        }

        // Set the timer state to finished
        val finishedTimerData = TimerData(
            millisecondsRemaining = 0,
            isPaused = false,
            isFinished = true,
            beepTriggered = false
        )
        timerRepository.updateData(finishedTimerData)

        // Capture the initial state for comparison
        val initialState = timerRepository.timerData.value

        // Invoke the tick method
        countdownLogic.tick(1000) // Tick for 1 second

        // Verify that the timer state remains unchanged
        val updatedState = timerRepository.timerData.value
        assertEquals(initialState, updatedState, "Timer state should remain unchanged when finished")
        assertFalse(mediaPlayerWrapper.beepPlayed, "No beep should be played when timer is finished")
    }

    @Test
    fun `tick should not beep again if already beeped`() = runTest {
        // Initialize test environment
        initScope(this) {
            delay(it)
        }

        // Set the timer state with beep already triggered
        val beepTriggeredTimerData = TimerData(
            millisecondsRemaining = 500,
            isPaused = false,
            isFinished = false,
            beepTriggered = true // Beep has already been triggered
        )
        timerRepository.updateData(beepTriggeredTimerData)

        // Reset beep played indicator
        mediaPlayerWrapper.beepPlayed = false

        // Invoke the tick method
        countdownLogic.tick(100) // Tick for a short duration

        // Verify that no additional beep is played
        assertFalse(mediaPlayerWrapper.beepPlayed, "No additional beep should be played if already beeped")
    }

    // Unit tests for execute():

    @Test
    fun `execute should handle partial tick correctly`() = runTest {
        // Initialize test environment
        initScope(this) {
            delay(it)
        }

        // Set initial timer state with millisecondsRemaining slightly more than the increment amount
        val incrementAmount = 50 // Increment amount less than SmallDelay, as Int
        val initialMilliseconds: Int = incrementAmount + 10 // Slightly more than incrementAmount, as Int
        val timerData = TimerData(initialMilliseconds, isPaused = false, isFinished = false, beepTriggered = false)
        timerRepository.updateData(timerData)

        // Set a custom time increment
        timeController.setIncrementAmount(incrementAmount)

        // Start execute method in a controlled manner
        val job = testScope.launch {
            timeController.setDelayLambda({ delay(it) }, this)
            countdownLogic.execute(this)
        }

        // Advance time by the increment amount to trigger the partial tick logic
        timeController.advanceTimeBy(incrementAmount.toLong())

        // Assert that the partial tick logic is executed
        val expectedMillisRemaining = initialMilliseconds - incrementAmount
        assertEquals(expectedMillisRemaining, timerRepository.timerData.value!!.millisecondsRemaining)

        // Clean up
        job.cancel()
    }

    @Test
    fun `execute should manage countdown correctly`() = runTest {
        // Initialize test environment
        initScope(this) {
            delay(it)
        }

        // Define initial timer state
        val initialMilliseconds = 5000
        val timerData =
            TimerData(
                millisecondsRemaining = initialMilliseconds,
                isPaused = false,
                isFinished = false,
                beepTriggered = false
            )

        timerRepository.updateData(timerData)

        // Execute countdown logic
        val job = testScope.launch {
            timeController.setDelayLambda({ delay(it) }, this)
            countdownLogic.execute(this)
        }

        // Simulate time progression and verify state updates
        val timeToAdvance = 1000L // 1 second
        for (i in 1..5) {
            timeController.advanceTimeBy(timeToAdvance)

            // Allow our check here to be out by about 100ms, as a buffer:
            val buffer = 100
            val updatedState = timerRepository.timerData.value!!
            val expectedMaxValue = initialMilliseconds - i * timeToAdvance + buffer
            assertTrue(updatedState.millisecondsRemaining <= expectedMaxValue)
            assertFalse(updatedState.isFinished)
            if (updatedState.millisecondsRemaining < 1000) {
                assertTrue(mediaPlayerWrapper.beepPlayed)
            }
        }

        // Advance time another 100ms to really progress to the end of the timer:
        timeController.advanceTimeBy(100)

        println(timerRepository.timerData.value!!.millisecondsRemaining)

        // Cleanup and final assertions
        job.cancel()
        val finalState = timerRepository.timerData.value!!
        assertTrue(finalState.isFinished)
    }

    @Test
    fun `execute should handle paused timer correctly`() = runTest {
        // Initialize test environment
        initScope(this) {
            delay(it)
        }

        // Define initial timer state as paused
        val initialMilliseconds = 5000
        val pausedTimerData =
            TimerData(
                millisecondsRemaining = initialMilliseconds,
                isPaused = true,
                isFinished = false,
                beepTriggered = false
            )
        timerRepository.updateData(pausedTimerData)

        // Execute countdown logic
        val job = testScope.launch {
            timeController.setDelayLambda({ delay(it) }, this)
            countdownLogic.execute(this)
        }

        // Simulate time progression
        val timeToAdvance = 1000L // 1 second
        timeController.advanceTimeBy(timeToAdvance)

        // Verify that timer state remains unchanged
        val updatedState = timerRepository.timerData.value!!
        assertTrue(updatedState.isPaused)
        assertEquals(initialMilliseconds, updatedState.millisecondsRemaining)
        assertFalse(updatedState.isFinished)
        assertFalse(mediaPlayerWrapper.beepPlayed)

        // Cleanup and final assertions
        job.cancel()
    }

    @Test
    fun `execute should handle finished timer correctly`() = runTest {
        // Initialize test environment
        initScope(this) {
            delay(it)
        }

        // Define initial timer state as finished
        val finishedTimerData =
            TimerData(
                millisecondsRemaining = 0,
                isPaused = false,
                isFinished = true,
                beepTriggered = false
            )
        timerRepository.updateData(finishedTimerData)

        // Execute countdown logic
        val job = testScope.launch {
            timeController.setDelayLambda({ delay(it) }, this)
            countdownLogic.execute(this)
        }

        // Simulate time progression
        val timeToAdvance = 1000L // 1 second
        timeController.advanceTimeBy(timeToAdvance)

        // Verify that timer state remains as finished and no actions are performed
        val updatedState = timerRepository.timerData.value!!
        assertTrue(updatedState.isFinished)
        assertEquals(0, updatedState.millisecondsRemaining)
        assertFalse(mediaPlayerWrapper.beepPlayed)

        // Cleanup and final assertions
        job.cancel()
    }

    // Unit Tests for getTimerLambdasSequence()

    @Test
    fun `tick should update extra timers correctly`() = runTest {
        // Initialize test environment
        initScope(this) {
            delay(it)
        }

        // Initialize the main timer in timerRepository
        val mainTimerData = TimerData(10_000, isPaused = false, isFinished = false, beepTriggered = false)
        timerRepository.updateData(mainTimerData)

        // Create and add ExtraTimerData instances to extraTimersRepository
        val extraTimers = ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>().apply {
            put(
                TimerUserInputDataId.randomId(),
                SingleTimerCountdownData(
                    TimerData(3000, isPaused = false, isFinished = false, beepTriggered = false),
                    TimerUserInputDataId.randomId()
                )
            )
            put(
                TimerUserInputDataId.randomId(),
                SingleTimerCountdownData(
                    TimerData(5000, isPaused = false, isFinished = false, beepTriggered = false),
                    TimerUserInputDataId.randomId()
                )
            )
        }
        extraTimersCountdownRepository.updateData(extraTimers)

        // Simulate a scenario where the timers need to be updated
        val tickDuration = 1000 // Tick for 1 second
        countdownLogic.tick(tickDuration)

        // Check that the main timer is updated
        val updatedMainTimer = timerRepository.timerData.value
        assertNotNull(updatedMainTimer, "Main timer should be updated")
        assertEquals(
            10_000 - tickDuration,
            updatedMainTimer.millisecondsRemaining,
            "Main timer should decrement correctly"
        )

        // Check that the extra timers are updated
        extraTimers.forEach { (_, timerData) ->
            val updatedExtraTimer = timerData.data
            assertNotNull(updatedExtraTimer, "Updated extra timer should not be null")
            assertTrue(
                updatedExtraTimer.millisecondsRemaining <= 3000 - tickDuration ||
                    updatedExtraTimer.millisecondsRemaining <= 5000 - tickDuration,
                "Extra timers should decrement correctly"
            )
        }
    }

    @Test
    fun `getTimerLambdasSequence should handle multiple timers`() = runTest {
        // Initialize test environment
        initScope(this) {
            delay(it)
        }

        // Define the main timer state
        var mainTimerState = TimerData(
            millisecondsRemaining = 5000,
            isPaused = false,
            isFinished = false,
            beepTriggered = false
        )

        // Create main timer get/set lambda
        val mainTimerGetSetLambda = Pair(
            { mainTimerState },
            { newState: TimerData -> mainTimerState = newState }
        )

        // Define and initialize extra timers
        val extraTimers = ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>().apply {
            put(
                TimerUserInputDataId.randomId(),
                SingleTimerCountdownData(
                    TimerData(3000, isPaused = false, isFinished = false, beepTriggered = false),
                    TimerUserInputDataId.randomId()
                )
            )
            put(
                TimerUserInputDataId.randomId(),
                SingleTimerCountdownData(
                    TimerData(2000, isPaused = true, isFinished = false, beepTriggered = false),
                    TimerUserInputDataId.randomId()
                )
            )
            put(
                TimerUserInputDataId.randomId(),
                SingleTimerCountdownData(
                    TimerData(1000, isPaused = false, isFinished = true, beepTriggered = false),
                    TimerUserInputDataId.randomId()
                )
            )
        }
        extraTimersCountdownRepository.updateData(extraTimers)

        // Retrieve the sequence of timer lambdas
        val timerLambdaSequence = countdownLogic.getTimerLambdasSequence(mainTimerGetSetLambda, extraTimers)

        // Verify the lambda sequence
        val lambdaList = timerLambdaSequence.toList()
        assertEquals(
            extraTimers.size + 1,
            lambdaList.size,
            "The sequence should contain the main timer and all extra timers"
        )

        // First lambda should correspond to main timer
        val (mainGet, mainSet) = lambdaList[0]
        assertEquals(mainTimerState, mainGet(), "Main timer lambda should return correct timer state")

        // Remaining lambdas should correspond to extra timers
        extraTimers.forEach { (id, timerData) ->
            val lambdaPair = lambdaList.find { it.first() == timerData.data }
            assertNotNull(lambdaPair, "Lambda for extra timer with ID $id should be present")
            val (getLambda, setLambda) = lambdaPair
            assertEquals(timerData.data, getLambda(), "Extra timer lambda should return correct timer state")

            // Test updating the timer state
            val updatedTimerData = TimerData(1234, isPaused = true, isFinished = false, beepTriggered = false)
            setLambda(updatedTimerData)
            assertEquals(
                updatedTimerData,
                getLambda(),
                "Extra timer set lambda should update the timer state correctly"
            )
        }
    }

    @Test
    fun `getTimerLambdasSequence should work with no extra timers`() = runTest {
        // Initialize test environment
        initScope(this) {
            delay(it)
        }

        // Ensure no extra timers are present
        extraTimersCountdownRepository.updateData(ConcurrentHashMap())

        // Define main timer state
        var mainTimerState = TimerData(
            millisecondsRemaining = 5000,
            isPaused = false,
            isFinished = false,
            beepTriggered = false
        )

        // Create main timer get/set lambda
        val mainTimerGetSetLambda = Pair(
            { mainTimerState },
            { newState: TimerData -> mainTimerState = newState }
        )

        // Retrieve the sequence of timer lambdas
        val timerLambdaSequence = countdownLogic.getTimerLambdasSequence(
            mainTimerGetSetLambda,
            ConcurrentHashMap()
        )

        // Verify the lambda sequence
        val lambdaList = timerLambdaSequence.toList()
        assertEquals(1, lambdaList.size, "The sequence should only contain the main timer lambda")

        // Validate the main timer lambda
        val (mainGet, mainSet) = lambdaList[0]
        assertEquals(mainTimerState, mainGet(), "The main timer lambda should return the correct timer state")

        // Simulate a state change and ensure the set lambda updates the main timer state
        val updatedState = TimerData(
            millisecondsRemaining = 4000,
            isPaused = true,
            isFinished = false,
            beepTriggered = false
        )
        mainSet(updatedState)
        assertEquals(updatedState, mainGet(), "The main timer set lambda should update the timer state correctly")
    }
}

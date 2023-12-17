package com.example.bronnbakestimer.service

import com.example.bronnbakestimer.logic.Constants
import com.example.bronnbakestimer.provider.CoroutineScopeProviderWrapper
import com.example.bronnbakestimer.provider.IMediaPlayerWrapper
import com.example.bronnbakestimer.repository.DefaultExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.DefaultMainTimerRepository
import com.example.bronnbakestimer.repository.IExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.IMainTimerRepository
import com.example.bronnbakestimer.util.IPhoneVibrator
import com.example.bronnbakestimer.util.Nanos
import com.example.bronnbakestimer.util.TimerUserInputDataId
import com.example.bronnbakestimer.util.toIntSafe
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
    private lateinit var timerRepository: IMainTimerRepository
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

        timerRepository = DefaultMainTimerRepository()
        mediaPlayerWrapper = TestMediaPlayerWrapper()
        extraTimersCountdownRepository = DefaultExtraTimersCountdownRepository()
        val coroutineScopeProvider = CoroutineScopeProviderWrapper(testScope)

        class TestPhoneVibrator : IPhoneVibrator {
            override fun vibrate() {
                // Do nothing
            }
        }

        // Create the CountdownLogic instance
        countdownLogic =
            CountdownLogic(
                timerRepository,
                mediaPlayerWrapper,
                coroutineScopeProvider,
                timeController,
                extraTimersCountdownRepository,
                phoneVibrator = TestPhoneVibrator(),
            )
    }

    @Test
    fun `tick should do nothing when timer data is not set`() =
        runTest {
            // Initialize test environment
            initScope(this) {
                delay(it)
            }

            // Set the main timer data to null
            timerRepository.updateData(null)

            // Invoke the tick method
            countdownLogic.tick(Nanos.fromMillis(1000)) // Tick for 1 second

            // Verify that timer state remains null and no actions are performed
            val timerState = timerRepository.timerData.value
            assertNull(timerState, "Timer state should remain null")
            assertFalse(mediaPlayerWrapper.beepPlayed, "No beep should be played")
        }

    @Test
    fun `tick should do nothing when timer is paused`() =
        runTest {
            // Initialize test environment
            initScope(this) {
                delay(it)
            }

            // Set the timer state to paused
            val pausedTimerData =
                TimerData(
                    nanosRemaining = Nanos.fromMillis(5000),
                    isPaused = true,
                    isFinished = false,
                )
            timerRepository.updateData(pausedTimerData)

            // Capture the initial state for comparison
            val initialState = timerRepository.timerData.value

            // Invoke the tick method
            countdownLogic.tick(Nanos.fromMillis(1000)) // Tick for 1 second

            // Verify that the timer state remains unchanged
            val updatedState = timerRepository.timerData.value
            assertEquals(initialState, updatedState, "Timer state should remain unchanged when paused")
            assertFalse(mediaPlayerWrapper.beepPlayed, "No beep should be played when timer is paused")
        }

    @Test
    fun `tick should do nothing when timer is finished`() =
        runTest {
            // Initialize test environment
            initScope(this) {
                delay(it)
            }

            // Set the timer state to finished
            val finishedTimerData =
                TimerData(
                    nanosRemaining = Nanos(0),
                    isPaused = false,
                    isFinished = true,
                )
            timerRepository.updateData(finishedTimerData)

            // Capture the initial state for comparison
            val initialState = timerRepository.timerData.value

            // Invoke the tick method
            countdownLogic.tick(Nanos.fromMillis(1000)) // Tick for 1 second

            // Verify that the timer state remains unchanged
            val updatedState = timerRepository.timerData.value
            assertEquals(initialState, updatedState, "Timer state should remain unchanged when finished")
            assertFalse(mediaPlayerWrapper.beepPlayed, "No beep should be played when timer is finished")
        }

    @Test
    fun `tick should not beep again if already beeped`() =
        runTest {
            // Initialize test environment
            initScope(this) {
                delay(it)
            }

            // Set the timer state with beep already triggered
            val beepTriggeredTimerData =
                TimerData(
                    nanosRemaining = Nanos.fromMillis(500),
                    isPaused = false,
                    isFinished = false,
                    // Beep has already been triggered:
                    beepTriggered = true,
                )
            timerRepository.updateData(beepTriggeredTimerData)

            // Reset beep played indicator
            mediaPlayerWrapper.beepPlayed = false

            // Invoke the tick method
            countdownLogic.tick(Nanos.fromMillis(100)) // Tick for a short duration

            // Verify that no additional beep is played
            assertFalse(mediaPlayerWrapper.beepPlayed, "No additional beep should be played if already beeped")
        }

    // Unit tests for execute():

    @Test
    fun `execute should manage countdown correctly`() =
        runTest {
            // 1. Setup Test Environment
            initScope(this) {
                delay(it)
            }

            val initialNanos = Nanos.fromMillis(10_000) // 10 seconds
            val mainTimerData = TimerData(nanosRemaining = initialNanos, isPaused = false, isFinished = false)
            timerRepository.updateData(mainTimerData)

            // 2. Simulate Countdown in 1-second intervals
            testScope.launch {
                timeController.setDelayLambda({ delay(it) }, this)
                countdownLogic.execute(this)
            }

            repeat(5) { iteration ->
                // Advance time by 1 second
                val timeToAdvance = 1000L // 1 second
                timeController.advanceTimeBy(timeToAdvance)

                // 3. Verify Countdown Behavior after each second
                val buffer = Nanos.fromMillis(100)
                val expectedNanosRemaining =
                    initialNanos - Nanos.fromMillis(timeToAdvance.toIntSafe() * (iteration + 1)) + buffer
                val updatedState = timerRepository.timerData.value!!

                assertEquals(
                    expectedNanosRemaining,
                    updatedState.nanosRemaining,
                    "Timer should decrement correctly after ${iteration + 1} second(s)",
                )

                val shouldPlayBeep = updatedState.nanosRemaining < Nanos(Constants.NANOSECONDS_PER_SECOND.toLong())
                if (shouldPlayBeep) {
                    assertTrue(
                        mediaPlayerWrapper.beepPlayed,
                        "Beep should have played after ${iteration + 1} second(s)",
                    )
                }
            }
        }

    @Test
    fun `execute should handle paused timer correctly`() =
        runTest {
            // Initialize test environment
            initScope(this) {
                delay(it)
            }

            // Define initial timer state as paused
            val initialMilliseconds = 5000
            val pausedTimerData =
                TimerData(
                    nanosRemaining = Nanos.fromMillis(initialMilliseconds),
                    isPaused = true,
                    isFinished = false,
                )
            timerRepository.updateData(pausedTimerData)

            // Execute countdown logic
            val job =
                testScope.launch {
                    timeController.setDelayLambda({ delay(it) }, this)
                    countdownLogic.execute(this)
                }

            // Simulate time progression
            val timeToAdvance = 1000L // 1 second
            timeController.advanceTimeBy(timeToAdvance)

            // Verify that timer state remains unchanged
            val updatedState = timerRepository.timerData.value!!
            assertTrue(updatedState.isPaused)
            assertEquals(initialMilliseconds.toLong(), updatedState.nanosRemaining.toMillisLong())
            assertFalse(updatedState.isFinished)
            assertFalse(mediaPlayerWrapper.beepPlayed)

            // Cleanup and final assertions
            job.cancel()
        }

    @Test
    fun `execute should handle finished timer correctly`() =
        runTest {
            // Initialize test environment
            initScope(this) {
                delay(it)
            }

            // Define initial timer state as finished
            val finishedTimerData =
                TimerData(
                    nanosRemaining = Nanos(0),
                    isPaused = false,
                    isFinished = true,
                )
            timerRepository.updateData(finishedTimerData)

            // Execute countdown logic
            val job =
                testScope.launch {
                    timeController.setDelayLambda({ delay(it) }, this)
                    countdownLogic.execute(this)
                }

            // Simulate time progression
            val timeToAdvance = 1000L // 1 second
            timeController.advanceTimeBy(timeToAdvance)

            // Verify that timer state remains as finished and no actions are performed
            val updatedState = timerRepository.timerData.value!!
            assertTrue(updatedState.isFinished)
            assertEquals(Nanos(0), updatedState.nanosRemaining)
            assertFalse(mediaPlayerWrapper.beepPlayed)

            // Cleanup and final assertions
            job.cancel()
        }

    // Unit Tests for getTimerLambdasSequence()

    @Test
    fun `tick should update extra timers correctly`() =
        runTest {
            // Initialize test environment
            initScope(this) {
                delay(it)
            }

            // Initialize the main timer in timerRepository
            val mainTimerData = TimerData(Nanos.fromMillis(10_000), isPaused = false, isFinished = false)
            timerRepository.updateData(mainTimerData)

            // Create and add ExtraTimerData instances to extraTimersRepository
            val extraTimers =
                ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>().apply {
                    put(
                        TimerUserInputDataId.randomId(),
                        SingleTimerCountdownData(
                            TimerData(Nanos.fromMillis(3000), isPaused = false, isFinished = false),
                            TimerUserInputDataId.randomId(),
                        ),
                    )
                    put(
                        TimerUserInputDataId.randomId(),
                        SingleTimerCountdownData(
                            TimerData(Nanos.fromMillis(5000), isPaused = false, isFinished = false),
                            TimerUserInputDataId.randomId(),
                        ),
                    )
                }
            extraTimersCountdownRepository.updateData(extraTimers)

            // Simulate a scenario where the timers need to be updated
            val tickDuration = 1000 // Tick for 1 second
            countdownLogic.tick(Nanos.fromMillis(tickDuration))

            // Check that the main timer is updated
            val updatedMainTimer = timerRepository.timerData.value
            assertNotNull(updatedMainTimer, "Main timer should be updated")
            assertEquals(
                10_000L - tickDuration,
                updatedMainTimer.nanosRemaining.toMillisLong(),
                "Main timer should decrement correctly",
            )

            // Check that the extra timers are updated
            extraTimers.forEach { (_, timerData) ->
                val updatedExtraTimer = timerData.data
                assertNotNull(updatedExtraTimer, "Updated extra timer should not be null")
                assertTrue(
                    updatedExtraTimer.nanosRemaining.toMillisLong() <= 3000 - tickDuration ||
                        updatedExtraTimer.nanosRemaining.toMillisLong() <= 5000 - tickDuration,
                    "Extra timers should decrement correctly",
                )
            }
        }

    @Test
    fun `getTimerLambdasSequence should handle multiple timers`() =
        runTest {
            // Initialize test environment
            initScope(this) {
                delay(it)
            }

            // Define the main timer state
            var mainTimerState =
                TimerData(
                    nanosRemaining = Nanos.fromMillis(5000),
                    isPaused = false,
                    isFinished = false,
                )

            // Create main timer get/set lambda
            val mainTimerGetSetLambda =
                Pair(
                    { mainTimerState },
                    { newState: TimerData -> mainTimerState = newState },
                )

            // Define and initialize extra timers
            val extraTimers =
                ConcurrentHashMap<TimerUserInputDataId, SingleTimerCountdownData>().apply {
                    put(
                        TimerUserInputDataId.randomId(),
                        SingleTimerCountdownData(
                            TimerData(Nanos.fromMillis(3000), isPaused = false, isFinished = false),
                            TimerUserInputDataId.randomId(),
                        ),
                    )
                    put(
                        TimerUserInputDataId.randomId(),
                        SingleTimerCountdownData(
                            TimerData(Nanos.fromMillis(2000), isPaused = true, isFinished = false),
                            TimerUserInputDataId.randomId(),
                        ),
                    )
                    put(
                        TimerUserInputDataId.randomId(),
                        SingleTimerCountdownData(
                            TimerData(Nanos.fromMillis(1000), isPaused = false, isFinished = true),
                            TimerUserInputDataId.randomId(),
                        ),
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
                "The sequence should contain the main timer and all extra timers",
            )

            // First lambda should correspond to main timer
            val (mainGet, _) = lambdaList[0]
            assertEquals(mainTimerState, mainGet(), "Main timer lambda should return correct timer state")

            // Remaining lambdas should correspond to extra timers
            extraTimers.forEach { (id, timerData) ->
                val lambdaPair = lambdaList.find { it.first() == timerData.data }
                assertNotNull(lambdaPair, "Lambda for extra timer with ID $id should be present")
                val (getLambda, setLambda) = lambdaPair
                assertEquals(timerData.data, getLambda(), "Extra timer lambda should return correct timer state")

                // Test updating the timer state
                val updatedTimerData = TimerData(Nanos.fromMillis(1234), isPaused = true, isFinished = false)
                setLambda(updatedTimerData)
                assertEquals(
                    updatedTimerData,
                    getLambda(),
                    "Extra timer set lambda should update the timer state correctly",
                )
            }
        }

    @Test
    fun `getTimerLambdasSequence should work with no extra timers`() =
        runTest {
            // Initialize test environment
            initScope(this) {
                delay(it)
            }

            // Ensure no extra timers are present
            extraTimersCountdownRepository.updateData(ConcurrentHashMap())

            // Define main timer state
            var mainTimerState =
                TimerData(
                    nanosRemaining = Nanos.fromMillis(5000),
                    isPaused = false,
                    isFinished = false,
                )

            // Create main timer get/set lambda
            val mainTimerGetSetLambda =
                Pair(
                    { mainTimerState },
                    { newState: TimerData -> mainTimerState = newState },
                )

            // Retrieve the sequence of timer lambdas
            val timerLambdaSequence =
                countdownLogic.getTimerLambdasSequence(
                    mainTimerGetSetLambda,
                    ConcurrentHashMap(),
                )

            // Verify the lambda sequence
            val lambdaList = timerLambdaSequence.toList()
            assertEquals(1, lambdaList.size, "The sequence should only contain the main timer lambda")

            // Validate the main timer lambda
            val (mainGet, mainSet) = lambdaList[0]
            assertEquals(mainTimerState, mainGet(), "The main timer lambda should return the correct timer state")

            // Simulate a state change and ensure the set lambda updates the main timer state
            val updatedState =
                TimerData(
                    nanosRemaining = Nanos.fromMillis(4000),
                    isPaused = true,
                    isFinished = false,
                )
            mainSet(updatedState)
            assertEquals(updatedState, mainGet(), "The main timer set lambda should update the timer state correctly")
        }
}

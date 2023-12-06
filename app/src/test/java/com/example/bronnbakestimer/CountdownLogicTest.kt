package com.example.bronnbakestimer

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.coroutines.CoroutineContext

@Suppress("FunctionMaxLength")
@ExperimentalCoroutinesApi
class CountdownLogicTest {

    private lateinit var timerRepository: ITimerRepository
    private lateinit var mediaPlayerWrapper: IMediaPlayerWrapper
    private lateinit var countdownLogic: CountdownLogic
    private lateinit var testCoroutineScope: TestScope
    private lateinit var coroutineExceptionHandler: CoroutineExceptionHandler
    private lateinit var timerDataFlow: MutableStateFlow<TimerData?>

    @Before
    fun setup() {
        timerRepository = mock()
        mediaPlayerWrapper = mock()
        timerDataFlow = MutableStateFlow(null)
        whenever(timerRepository.timerData).thenReturn(timerDataFlow)
        coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            println("Coroutine Exception: $throwable")
        }
        testCoroutineScope = TestScope(StandardTestDispatcher())
        val delayProvider = TestDelayProvider()
        val coroutineScopeProvider = TestCoroutineScopeProvider(testCoroutineScope)
        countdownLogic = CountdownLogic(timerRepository, mediaPlayerWrapper, coroutineScopeProvider, delayProvider)
    }

    @Test
    fun `execute should play beep when timer reaches below one second`() = runBlocking {
        // Arrange
        val initialState = TimerData(
            millisecondsRemaining = 900,
            isPaused = false,
            isFinished = false,
            beepTriggered = false,
        )
        val timerDataFlow = MutableStateFlow<TimerData?>(initialState)
        whenever(timerRepository.timerData).thenReturn(timerDataFlow)

        // Act
        countdownLogic.tick() // Manually invoke tick

        // Assert
        verify(mediaPlayerWrapper).playBeep()
        verify(timerRepository).updateData(
            TimerData(
                millisecondsRemaining = 800,
                isPaused = false,
                isFinished = false,
                beepTriggered = true,
            )
        )
    }

    @Test
    fun `tick should do nothing when timer data is not set`() = runBlocking {
        // Arrange
        timerDataFlow.value = null

        // Act
        countdownLogic.tick()

        // Assert
        verify(timerRepository, never()).updateData(any())
        verify(mediaPlayerWrapper, never()).playBeep()
    }

    @Test
    fun `tick should do nothing when timer is paused`() = runBlocking {
        // Arrange
        val pausedState = TimerData(1000, isPaused = true, isFinished = false, beepTriggered = false)
        timerDataFlow.value = pausedState

        // Act
        countdownLogic.tick()

        // Assert
        verify(timerRepository, never()).updateData(any())
        verify(mediaPlayerWrapper, never()).playBeep()
    }

    @Test
    fun `tick should do nothing when timer is finished`() = runBlocking {
        // Arrange
        val finishedState = TimerData(0, isPaused = false, isFinished = true, beepTriggered = false)
        timerDataFlow.value = finishedState

        // Act
        countdownLogic.tick()

        // Assert
        verify(timerRepository, never()).updateData(any())
        verify(mediaPlayerWrapper, never()).playBeep()
    }

    @Test
    fun `tick should not beep again if already beeped`() = runBlocking {
        // Arrange
        val beepTriggeredState = TimerData(0, isPaused = false, isFinished = false, beepTriggered = true)
        timerDataFlow.value = beepTriggeredState

        // Act
        countdownLogic.tick()

        // Assert
        verify(mediaPlayerWrapper, never()).playBeep()
    }
}

class TestCoroutineScopeProvider(private val testScope: TestScope) : CoroutineScopeProvider {
    override val isActive: Boolean
        get() = testScope.coroutineContext[Job]?.isActive ?: false

    override fun launch(
        context: CoroutineContext,
        start: CoroutineStart,
        block: suspend CoroutineScope.() -> Unit
    ) {
        testScope.launch(context, start) { block() }
    }
}

class TestDelayProvider : DelayProvider {
    // The below can be made public if needed for testing
    private var totalElapsedTime = 0L
    private var totalCalls = 0
    private val callLog = mutableListOf<Long>()

    override suspend fun delay(timeMillis: Long) {
        // Logic for testing
        totalElapsedTime += timeMillis
        totalCalls++
        callLog.add(timeMillis)
        // No actual delay, just logging and updating state
    }
}

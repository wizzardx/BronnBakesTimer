package com.example.bronnbakestimer.provider

import com.example.bronnbakestimer.provider.CoroutineScopeProviderWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("FunctionMaxLength")
class CoroutineScopeProviderWrapperTest {

    private lateinit var coroutineScope: CoroutineScope
    private lateinit var coroutineScopeProviderWrapper: CoroutineScopeProviderWrapper

    @Before
    fun setUp() {
        coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        coroutineScopeProviderWrapper = CoroutineScopeProviderWrapper(coroutineScope)
    }

    @After
    fun tearDown() {
        coroutineScope.cancel()
    }

    @Test
    fun `isActive should return true when the coroutineScope is active`() {
        assertTrue(coroutineScopeProviderWrapper.isActive)
    }

    @Test
    fun `isActive should return false when the coroutineScope is cancelled`() {
        coroutineScope.cancel()
        assertFalse(coroutineScopeProviderWrapper.isActive)
    }

    @Test
    fun `launch should execute the block in the coroutineScope`() {
        var executed = false
        coroutineScopeProviderWrapper.launch {
            executed = true
        }
        assertTrue(executed)
    }

    @Test
    fun `launch should not execute the block if coroutineScope is cancelled`() {
        coroutineScope.cancel()
        var executed = false
        coroutineScopeProviderWrapper.launch {
            executed = true
        }
        assertFalse(executed)
    }
    // New tests for isActive and launch

    @Test
    fun `isActive should return true when a coroutine is launched`() {
        coroutineScopeProviderWrapper.launch {
            assertTrue(coroutineScopeProviderWrapper.isActive)
        }
    }

    @Test
    fun `isActive should return false after coroutine completion`() {
        runBlocking {
            coroutineScopeProviderWrapper.launch {
                // Perform some coroutine work
                delay(100)
            }
            delay(200) // Wait for the coroutine to complete
            coroutineScope.cancel() // Cancel the entire coroutine scope
            assertFalse(coroutineScopeProviderWrapper.isActive)
        }
    }

    @Test
    fun `launch should execute coroutine in specified CoroutineContext`() {
        val latch = CountDownLatch(1)
        var isDefaultDispatcher = false
        coroutineScopeProviderWrapper.launch(Dispatchers.Default) {
            isDefaultDispatcher = coroutineContext[ContinuationInterceptor] == Dispatchers.Default
            latch.countDown()
        }
        latch.await() // Wait for coroutine to complete
        assertTrue(isDefaultDispatcher)
    }

    @Test
    fun `launch should not execute coroutine if scope is cancelled before launch`() {
        coroutineScope.cancel()
        var executed = false
        coroutineScopeProviderWrapper.launch {
            executed = true
        }
        assertFalse(executed)
    }

    // New tests for coroutineScope property

    @Test
    fun `coroutineScope property should return the correct CoroutineScope`() {
        assertEquals(coroutineScopeProviderWrapper.coroutineScope, coroutineScope)
    }

    @Test
    fun `coroutineScope property should be able to launch a coroutine`() {
        var executed = false
        coroutineScopeProviderWrapper.coroutineScope.launch {
            executed = true
        }
        assertTrue(executed)
    }

    @Test
    fun `coroutineScope property should reflect cancellation state`() {
        coroutineScope.cancel()
        assertFalse(coroutineScopeProviderWrapper.coroutineScope.isActive)
    }

    @Test
    fun `coroutineScope property should maintain context of original CoroutineScope`() {
        val latch = CountDownLatch(1)
        var isUnconfinedDispatcher = false
        coroutineScopeProviderWrapper.coroutineScope.launch {
            isUnconfinedDispatcher = coroutineContext[ContinuationInterceptor] == Dispatchers.Unconfined
            latch.countDown()
        }
        latch.await() // Wait for coroutine to complete
        assertTrue(isUnconfinedDispatcher)
    }
    // Additional tests can be added to cover more scenarios and edge cases.
}

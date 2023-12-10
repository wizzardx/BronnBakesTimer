package com.example.bronnbakestimer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Before
import org.junit.Test
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

    // Additional tests can be added to cover more scenarios and edge cases.
}

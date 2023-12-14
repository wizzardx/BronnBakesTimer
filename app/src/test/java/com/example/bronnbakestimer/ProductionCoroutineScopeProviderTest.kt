package com.example.bronnbakestimer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("FunctionMaxLength")
@ExperimentalCoroutinesApi
class ProductionCoroutineScopeProviderTest {

    private lateinit var productionCoroutineScopeProvider: ProductionCoroutineScopeProvider
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        productionCoroutineScopeProvider = ProductionCoroutineScopeProvider()
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset main dispatcher to its original state
    }

    @Test
    fun `launch should execute a coroutine on the main thread`() = runTest(testDispatcher) {
        var executed = false

        productionCoroutineScopeProvider.launch {
            delay(10) // Simulating some work
            executed = true
        }

        advanceUntilIdle() // Advance time until all coroutines are idle
        assertTrue(executed) // Verify that the coroutine was executed
    }

    @Test
    fun `isActive should reflect coroutine scope status`() = runTest(testDispatcher) {
        assertTrue(productionCoroutineScopeProvider.isActive) // Check isActive before launching a coroutine

        val job = productionCoroutineScopeProvider.coroutineScope.launch {
            delay(10) // Simulate some work
        }

        assertTrue(productionCoroutineScopeProvider.isActive) // Check isActive while coroutine is running

        job.join() // Wait for coroutine to complete

        assertTrue(productionCoroutineScopeProvider.isActive) // Check isActive after coroutine has completed

        productionCoroutineScopeProvider.coroutineScope.cancel() // Cancel the coroutine scope

        assertFalse(productionCoroutineScopeProvider.isActive) // Check isActive after scope is cancelled
    }
}

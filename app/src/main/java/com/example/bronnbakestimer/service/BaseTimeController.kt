package com.example.bronnbakestimer.service

import kotlinx.coroutines.CoroutineScope

/**
 * Abstract base class for managing time in coroutine testing and runtime.
 *
 * Provides an interface for controlling time-related functions, especially in coroutine contexts.
 * Ideal for use in unit tests for time manipulation and coroutine testing. Extend with specific
 * implementations like LiveTimeController or TestTimeController for runtime or test use.
 */
abstract class BaseTimeController {
    // _delayLambda is a dist-type structure, keyed by scope, and the value is a lambda
    private val delayLambdas = mutableMapOf<CoroutineScope, (suspend (Long) -> Unit)>()

    /**
     * Suspends execution for a specified duration within a given CoroutineScope.
     *
     * Uses a lambda function specific to the provided CoroutineScope to implement the delay.
     * @param delayTimeMillis The time to delay in milliseconds.
     * @param scope The CoroutineScope in which the delay is to be applied.
     * @throws IllegalStateException If the delay lambda is not set for the provided scope.
     */
    open suspend fun delay(
        delayTimeMillis: Long,
        scope: CoroutineScope,
    ) {
        // Error out if the lambda is not set for this coroutine scope.
        check(delayLambdas.containsKey(scope)) { "Delay lambda not set for this scope. Call setDelayLambda() first." }
        val lambda = delayLambdas.getValue(scope)
        lambda.invoke(delayTimeMillis)
    }

    /**
     * Retrieves the current system time in nanoseconds.
     *
     * This function is abstract and should be implemented to return the current time in nanoseconds.
     * @return The current time in nanoseconds.
     */
    abstract fun nanoTime(): Long

    /**
     * Advances the simulated time by a specified duration.
     *
     * This abstract function is designed to be overridden to simulate time advancement in milliseconds.
     * @param delayTimeMillis The duration in milliseconds by which to advance the time.
     */
    abstract suspend fun advanceTimeBy(delayTimeMillis: Long)

    /**
     * Assigns a delay lambda to a specific CoroutineScope.
     *
     * Sets a lambda function for delaying within a CoroutineScope, ensuring unique lambdas per scope.
     * @param lambda The delay lambda function.
     * @param scope The CoroutineScope for which the lambda is being set.
     * @throws IllegalArgumentException If a lambda is already set for the provided scope.
     */
    fun setDelayLambda(
        lambda: (suspend (Long) -> Unit),
        scope: CoroutineScope,
    ) {
        // Not allowed to set the lambda twice:
        require(!delayLambdas.containsKey(scope)) { "Lambda already set for this scope" }
        // Now add the lambda:
        delayLambdas[scope] = lambda
    }
}

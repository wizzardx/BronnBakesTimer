package com.example.bronnbakestimer.provider

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * An interface that defines the contract for providing a [CoroutineScope] and launching
 * coroutines within it. Implementations of this interface should facilitate the management
 * of coroutine execution and allow checking the activity status of the underlying coroutine scope.
 */
interface CoroutineScopeProvider {
    /**
     * The coroutine scope associated with this provider.
     *
     * This property exposes a CoroutineScope that is tied to the lifecycle and context of the
     * implementation. It is used to launch coroutines in a controlled environment, ensuring
     * that they operate within the defined scope. For example, in a production implementation,
     * this might be tied to the main thread of an Android application, while in testing, it
     * could be configured to run coroutines in a more predictable and synchronous manner.
     *
     * The provided CoroutineScope should be used for all coroutine operations that require
     * context-awareness and lifecycle management, such as operations related to UI updates,
     * database transactions, or any asynchronous task that needs structured concurrency.
     *
     * Proper management of this CoroutineScope is crucial to prevent memory leaks and ensure
     * proper cancellation of ongoing tasks when the scope is no longer valid or when the
     * lifecycle it is attached to is destroyed.
     */
    val coroutineScope: CoroutineScope

    /**
     * Indicates whether the underlying coroutine scope is still active.
     *
     * @return `true` if the coroutine scope is active, `false` otherwise.
     */
    val isActive: Boolean
        // Manually check if the CoroutineScope's Job is active
        get() = coroutineScope.coroutineContext[Job]?.isActive ?: false

    /**
     * Launches a coroutine within the provided [CoroutineContext] and [CoroutineStart].
     * This method abstracts the coroutine launching mechanism, allowing for more control over
     * coroutine behavior and lifecycle.
     *
     * @param context The [CoroutineContext] in which to execute the coroutine. Defaults to [EmptyCoroutineContext].
     * @param start The [CoroutineStart] strategy for coroutine execution. Defaults to [CoroutineStart.DEFAULT].
     * @param block The suspending lambda to be executed within the coroutine.
     */
    fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit,
    ) {
        coroutineScope.launch(context, start, block)
    }
}

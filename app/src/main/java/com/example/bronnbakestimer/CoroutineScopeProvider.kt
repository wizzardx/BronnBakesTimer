package com.example.bronnbakestimer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * An interface that defines the contract for providing a [CoroutineScope] and launching
 * coroutines within it. Implementations of this interface should facilitate the management
 * of coroutine execution and allow checking the activity status of the underlying coroutine scope.
 */
interface CoroutineScopeProvider {

    /**
     * Indicates whether the underlying coroutine scope is still active.
     *
     * @return `true` if the coroutine scope is active, `false` otherwise.
     */
    val isActive: Boolean

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
    )
}

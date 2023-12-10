package com.example.bronnbakestimer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * A wrapper class that implements the [CoroutineScopeProvider] interface by delegating
 * to an underlying [CoroutineScope]. This allows for compatibility with classes or functions
 * that expect a [CoroutineScopeProvider] while utilizing a [CoroutineScope].
 *
 * @param scope The underlying [CoroutineScope] to be wrapped.
 */
class CoroutineScopeProviderWrapper(private val scope: CoroutineScope) : CoroutineScopeProvider {
    override val isActive: Boolean
        get() = scope.isActive

    override fun launch(
        context: CoroutineContext,
        start: CoroutineStart,
        block: suspend CoroutineScope.() -> Unit
    ) {
        scope.launch(context, start) { block() }
    }
}

package com.example.bronnbakestimer.provider

import com.example.bronnbakestimer.provider.CoroutineScopeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Provides a production-grade CoroutineScope for use in the main application.
 *
 * This class implements the CoroutineScopeProvider interface, offering a CoroutineScope
 * that operates on the main thread of the application. It utilizes the Main dispatcher,
 * ensuring that coroutine execution is properly scheduled on the main thread of the Android
 * application. This is critical for operations that require interaction with the UI or other
 * main-thread-only operations.
 *
 * Usage:
 * Should be used in the main application to provide a CoroutineScope compatible with UI
 * operations and main-thread tasks. Ideal for scenarios where coroutines need to update UI
 * components or interact with Android lifecycle components.
 */
class ProductionCoroutineScopeProvider : CoroutineScopeProvider {
    override val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    // The rest of the implementation is inherited from the CoroutineScopeProvider interface
}

package com.example.bronnbakestimer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope

/**
 * Provides a test-specific CoroutineScope for use in unit tests.
 *
 * This class implements the CoroutineScopeProvider interface, offering a CoroutineScope
 * with a TestDispatcher. The UnconfinedTestDispatcher is used to ensure that coroutines
 * run immediately on the current thread, facilitating easier testing of asynchronous code.
 *
 * This provider is intended for use in test environments where control over coroutine
 * execution is necessary for precise and predictable testing outcomes.
 *
 * Usage:
 * Should be injected in tests in place of the standard CoroutineScopeProvider to provide
 * a controlled coroutine environment. Suitable for unit tests that require the execution
 * of coroutines in a synchronous and immediate manner.
 */
class TestCoroutineScopeProvider : CoroutineScopeProvider {
    override val coroutineScope: CoroutineScope = TestScope()

    // The rest of the implementation is inherited from the CoroutineScopeProvider interface
}

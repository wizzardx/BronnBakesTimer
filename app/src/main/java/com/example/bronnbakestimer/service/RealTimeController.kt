package com.example.bronnbakestimer.service

/**
 * A concrete implementation of BaseTimeController for use in runtime environments.
 *
 * This class provides a real-time behavior for time-related functions, particularly useful for coroutine contexts
 * in production. It extends BaseTimeController and overrides the necessary methods to interact with the actual
 * system time. It includes an implementation of nanoTime to return the current system time in nanoseconds,
 * and an override of advanceTimeBy to introduce a real delay based on the system's capabilities.
 *
 * Functions:
 * - nanoTime: Returns the current system time in nanoseconds.
 * - advanceTimeBy: Delays the coroutine for a specified time in milliseconds in real-time.
 *
 * Usage:
 * This class is meant to be used where real-time control and testing of coroutines are required,
 * particularly in a live environment. It is not suitable for unit testing where simulated time control is needed.
 */
class RealTimeController : BaseTimeController() {
    override fun nanoTime(): Long = System.nanoTime()

    override suspend fun advanceTimeBy(delayTimeMillis: Long) {
        // In live mode, we just delay (to avoid busy looping).
        kotlinx.coroutines.delay(delayTimeMillis)
    }
}

package com.example.bronnbakestimer.util

/**
 * Measures the average execution time of a given block of code over a specified number of iterations.
 *
 * This profiling function executes a specified block of code multiple times, as defined by the `times` parameter,
 * and calculates the average execution time for these executions. It is useful for identifying performance bottlenecks
 * and for getting a general sense of how long a block of code takes to execute.
 *
 * Dependency injection for time measurement and output functions is used to enhance flexibility and testability,
 * allowing the function to adapt to different environments and requirements.
 *
 * The function is primarily intended for basic performance testing and profiling. For more detailed and comprehensive
 * performance analysis, dedicated profiling tools or libraries are recommended.
 *
 * @param times The number of times to execute the block of code. Defaults to 1. If a non-positive value is provided,
 *              it is defaulted to 1 execution.
 * @param timeProvider A function providing the current time in milliseconds, used for measuring execution time.
 *                     Defaults to `System.currentTimeMillis()`. It can be replaced with a custom function in
 *                     testing or other specific scenarios.
 * @param printFunction A function to output the results, defaults to `println`. It can be replaced with a custom
 *                      function to direct output to a different destination or format.
 * @param block The block of code to be profiled.
 *
 * Usage:
 * ```
 * myProfiler(times = 5) {
 *     // Code to be profiled
 * }
 * ```
 */
fun myProfiler(
    times: Int = 1,
    timeProvider: () -> Long = System::currentTimeMillis,
    printFunction: (String) -> Unit = ::println,
    block: () -> Unit,
) {
    val totalTimes = if (times > 0) times else 1 // Ensure at least one execution
    val startTime = timeProvider()

    repeat(totalTimes) {
        block()
    }

    val endTime = timeProvider()
    val averageTime = (endTime - startTime) / totalTimes.toFloat()
    printFunction("Average Execution Time for $totalTimes runs: $averageTime ms")
}

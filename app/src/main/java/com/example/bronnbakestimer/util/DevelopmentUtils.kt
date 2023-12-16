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

/**
 * Prints debugging information including the module, file name, and line number of the call location.
 * Optionally includes a custom message. The function allows for flexible output handling by accepting
 * an output function parameter.
 *
 * @param message An optional custom message to be included in the debug information. If provided,
 *                it is appended to the base debug information. Defaults to `null`, meaning no additional
 *                message will be appended.
 * @param outputFunction A function that takes a String and returns Unit (no return value). This function
 *                       is used to output the generated debug information. It defaults to `::println`,
 *                       meaning it will print to the standard output if no other function is provided.
 *                       This parameter allows for custom handling of the debug output, such as logging
 *                       to a file or displaying in a GUI element.
 *
 * Usage Example:
 * ```
 * printDebugInfo("Custom message") // Prints debug info with a custom message using standard output
 * printDebugInfo(outputFunction = ::customPrintFunction) // Prints debug info using a custom print function
 * printDebugInfo("Custom message", ::customPrintFunction) // Custom message and custom print function
 * ```
 */
@Suppress("ThrowingExceptionsWithoutMessageOrCause")
fun printDebugInfo(
    message: String? = null,
    // Default to using println if no function is provided
    outputFunction: (String) -> Unit = ::println,
) {
    // Create a Throwable to access the stack trace
    val throwable = Throwable()

    // Get the stack trace element corresponding to the caller
    val stackTraceElement = throwable.stackTrace[2]

    // Extract file name, line number, and class name
    val fileName = stackTraceElement.fileName
    val lineNumber = stackTraceElement.lineNumber
    val className = stackTraceElement.className

    // Extract the package name from the class name
    val packageName = className.substringBeforeLast('.')

    // Create the base message
    val baseMessage = "TESTING (Module: $packageName, $fileName: $lineNumber)"

    // Use the output function to print the formatted message with or without the additional message
    if (message != null) {
        outputFunction("$baseMessage: $message")
    } else {
        outputFunction(baseMessage)
    }
}

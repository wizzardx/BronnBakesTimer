package com.example.bronnbakestimer

/**
 * Constants for our app.
 */
object Constants {
    /**
     * There are 60 seconds in every minute.
     */
    const val SecondsPerMinute = 60

    /**
     * There are 1000 milliseconds in every second.
     */
    const val MillisecondsPerSecond = 1000

    /**
     * In our logic we delay (in milliseconds) for short periods of time and then perform timer-related logic.
     */
    const val SmallDelay = 100L

    /**
     * For the sake of simplicity, we limit the user to inputting numbers (timer minutes) between 1 and 500.
     */
    const val MaxUserInputNum = 500

    /**
     * A constant value representing the default time unit for user input, which is [TimeUnit.MINUTES].
     * User input values are typically provided in minutes.
     */
    val UserInputTimeUnit = TimeUnit.MINUTES
}

/**
 * Enumeration representing time units for user input.
 * It includes [MINUTES] and [SECONDS] to specify the time unit.
 */
enum class TimeUnit {
    MINUTES, SECONDS,
}

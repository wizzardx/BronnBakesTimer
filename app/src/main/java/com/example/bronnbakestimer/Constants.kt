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
     * For the sake of simplicity, we limit the user to inputting numbers (timer duration) between 1 and 500.
     */
    const val MaxUserInputNum = 500

    /**
     * The application's version number displayed to users.
     * This constant holds the version number of the application, which is shown at the bottom center
     * of the main screen to provide users with information about the app's current version.
     */
    const val AppVersion = BuildConfig.VERSION_NAME

    /**
     * A constant value representing the default time unit for user input, which is [UserInputTimeUnitType.MINUTES].
     * User input values are typically provided in minutes.
     */
    val UserInputTimeUnit = UserInputTimeUnitType.MINUTES
}

/**
 * Enumeration representing time units for user input.
 * It includes [MINUTES] and [SECONDS] to specify the time unit.
 */
enum class UserInputTimeUnitType {
    MINUTES, SECONDS;

    fun getName(): String {
        return when (this) {
            MINUTES -> "Minutes"
            SECONDS -> "Seconds"
        }
    }
}

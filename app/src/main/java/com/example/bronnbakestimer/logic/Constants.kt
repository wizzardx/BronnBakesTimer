package com.example.bronnbakestimer.logic

import com.example.bronnbakestimer.BuildConfig

/**
 * Constants for our app.
 */
object Constants {
    /**
     * There are 60 seconds in every minute.
     */
    const val SECONDS_PER_MINUTE: Int = 60

    /**
     * There are 1000 milliseconds in every second.
     */
    const val MILLISECONDS_PER_SECOND: Int = 1000

    /**
     * There are 1,000,000 milliseconds in every second.
     */
    const val NANOSECONDS_PER_MILLISECOND: Int = 1_000_000

    /**
     * There are 1,000,000,000 (one billion) nanoseconds in every second.
     */
    const val NANOSECONDS_PER_SECOND: Int = 1_000_000_000

    /**
     * In our logic we delay (in milliseconds) for short periods of time and then perform timer-related logic.
     */
    const val SMALL_DELAY_MILLIS: Int = 100

    /**
     * For the sake of simplicity, we limit the user to inputting numbers (timer duration) between 1 and 500.
     */
    const val MAX_USER_INPUT_NUM: Int = 500

    /**
     * The application's version number displayed to users.
     * This constant holds the version number of the application, which is shown at the bottom center
     * of the main screen to provide users with information about the app's current version.
     */
    const val APP_VERSION = BuildConfig.VERSION_NAME

    /**
     * A constant value representing the default time unit for user input, which is [UserInputTimeUnit.MINUTES].
     * User input values are typically provided in minutes.
     */
    val USER_INPUT_TIME_UNIT: UserInputTimeUnit = UserInputTimeUnit.MINUTES

    /**
     * The duration of half a second in milliseconds.
     *
     * This constant represents the length of time equivalent to half a second, expressed in milliseconds.
     * It is commonly used in scenarios where a brief delay is required, such as in animations, user interface
     * feedback, or handling certain asynchronous events like phone vibrations. The value of 500 milliseconds
     * is chosen to provide a noticeable yet quick duration, suitable for creating smooth transitions or
     * responsive interactions.
     *
     * Value: 500 milliseconds (0.5 seconds)
     */
    const val HALF_SECOND_MILLIS: Long = 500

    /**
     * Number of updates per second for the timer's internal data.
     *
     * This constant defines the frequency at which the timer's internal data is refreshed or updated.
     * Setting this value to 10 means that the timer will undergo 10 updates or 'ticks' per second.
     * This frequency is crucial for ensuring that the timer's display and internal state remain
     * synchronized and provide a smooth user experience. A higher value would result in more frequent
     * updates, potentially making the display smoother, but at the cost of higher computational load.
     * Conversely, a lower value would reduce the load but might make the timer's display less responsive
     * or smooth. The chosen value of 10 strikes a balance, offering a reasonable update rate without
     * overburdening the system.
     *
     * Value: 10 updates per second
     */

    const val TICKS_PER_SECOND = 10
}

/**
 * Enumeration representing time units for user input.
 * It includes [MINUTES] and [SECONDS] to specify the time unit.
 */
enum class UserInputTimeUnit {
    MINUTES,
    SECONDS,
    ;

    /**
     * Retrieves the display name of the time unit.
     *
     * This function provides a user-friendly string representation of the time unit. For example,
     * it returns "Minutes" for MINUTES and "Seconds" for SECONDS.
     * This is useful for displaying time unit options in a user interface or for logging purposes.
     *
     * @return The display name of the time unit as a [String].
     */
    fun getName(): String {
        return when (this) {
            MINUTES -> "Minutes"
            SECONDS -> "Seconds"
        }
    }
}

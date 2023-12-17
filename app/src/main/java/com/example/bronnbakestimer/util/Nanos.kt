package com.example.bronnbakestimer.util

import com.example.bronnbakestimer.logic.Constants

/**
 * Represents a time duration in nanoseconds.
 *
 * This value class wraps a value representing a duration in nanoseconds and allows both positive
 * and negative values. It is designed to represent time durations as well as differences in time.
 * The class implements `Comparable` to allow comparison between different `Nanos` instances
 * and provides methods for arithmetic operations (addition, subtraction) and conversion to `Seconds`.
 *
 * The `Nanos` class supports immutability; all operations on instances return new instances
 * without modifying the existing ones.
 *
 * @property value The duration in nanoseconds as a long. Can be negative or positive.
 */
@JvmInline
value class Nanos(val value: Long) : Comparable<Nanos> {
    /**
     * Secondary constructor that accepts an integer value representing nanoseconds.
     *
     * This constructor allows the creation of `Nanos` instances using an integer value.
     * It converts the provided integer to a long, thereby enabling the use of an Int for
     * values that are within the Int range. This can be convenient when dealing with durations
     * that are known to be within the Int range and avoids the need for explicit long literals.
     *
     * @param value The duration in nanoseconds as an integer. The value is converted to a long.
     */
    constructor(value: Int) : this(value.toLong())

    /**
     * Compares this `Nanos` instance with another instance.
     *
     * This method implements the `Comparable` interface, allowing `Nanos` instances to be compared with each
     * other. The comparison is based on the underlying `value` properties of the `Nanos` instances. It returns
     * an integer representing the comparison result:
     *
     * - A positive integer if this instance is greater than the other instance.
     * - Zero if this instance is equal to the other instance.
     * - A negative integer if this instance is less than the other instance.
     *
     * This method is useful for sorting, comparing, or checking equality between two `Nanos` instances. It
     * adheres to the general contract of the `compareTo` method as specified in the `Comparable` interface.
     *
     * @param other The `Nanos` instance to be compared with this instance.
     * @return An integer indicating the result of the comparison.
     */
    override fun compareTo(other: Nanos): Int = value.compareTo(other.value)

    /**
     * Adds two Nanos instances.
     *
     * @param other Another Nanos instance to be added to this instance.
     * @return A new Nanos instance representing the sum of this and the other instance.
     */
    operator fun plus(other: Nanos): Nanos {
        val sum = this.value + other.value
        return Nanos(sum)
    }

    /**
     * Returns a string representation of the `Nanos` instance.
     *
     * This method provides a human-readable form of the `Nanos`, primarily for debugging and logging purposes.
     * It represents the `Nanos` instance as the nanoseconds value followed by the unit "ns".
     *
     * @return A string representation of the `Nanos` instance.
     */
    override fun toString(): String = "$value ns"

    /**
     * Converts this `Nanos` instance to `Seconds`.
     *
     * This method converts the duration from nanoseconds to seconds. Since there are 1,000,000,000 nanoseconds
     * in a second, this conversion involves dividing the `Nanos` value by 1,000,000,000. Any fractional
     * part of the result is discarded since `Seconds` holds an integer value.
     *
     * @return A `Seconds` instance representing the equivalent duration in seconds.
     */
    fun toSeconds(): Seconds {
        val secondsValue = (this.value / Constants.NANOSECONDS_PER_SECOND).toInt()
        return Seconds(secondsValue)
    }

    /**
     * Subtracts another Nanos instance from this instance.
     *
     * This method subtracts the `value` of the given Nanos instance from this instance's `value`
     * and returns a new Nanos instance with the result. The result can be negative if this instance's
     * `value` is smaller than the `other` instance's `value`.
     *
     * @param other The Nanos instance to subtract from this instance.
     * @return A new Nanos instance representing the difference.
     */
    operator fun minus(other: Nanos): Nanos = Nanos(this.value - other.value)

    /**
     * Converts the `Nanos` value to milliseconds and returns it as a `Long`.
     *
     * This method converts the duration from nanoseconds to milliseconds by dividing
     * the `Nanos` value by 1,000,000. The conversion is straightforward as the resulting
     * milliseconds value easily fits within the range of a `Long`.
     *
     * @return The duration in milliseconds as a `Long`.
     */
    fun toMillisLong(): Long = this.value / Constants.NANOSECONDS_PER_MILLISECOND

    /**
     * Divides the `Nanos` instance by an integer divisor.
     *
     * @param divisor The integer value to divide by.
     * @return A new `Nanos` instance representing the result of the division.
     * @throws ArithmeticException if the divisor is zero.
     */
    operator fun div(divisor: Int): Nanos {
        if (divisor == 0) {
            throw ArithmeticException("Cannot divide by zero")
        }
        return Nanos(value / divisor)
    }

    companion object {
        /**
         * Converts an integer value representing minutes to `Nanos`.
         *
         * This static-like method in the companion object facilitates the conversion of a duration from minutes
         * to nanoseconds.
         * It multiplies the provided minute value by 60 (to convert minutes to seconds) and then by
         * 1,000,000,000 (to convert seconds to nanoseconds). This method is useful when you need to
         * work with minute-based durations in the context of nanoseconds.
         *
         * @param minutes An integer representing the duration in minutes.
         * @return A `Nanos` instance representing the equivalent duration in nanoseconds.
         */
        fun fromMinutes(minutes: Int): Nanos {
            return Nanos(
                minutes.toLong() *
                    Constants.SECONDS_PER_MINUTE * Constants.NANOSECONDS_PER_SECOND,
            )
        }

        /**
         * Converts an integer value representing seconds to `Nanos`.
         *
         * This method in the companion object facilitates the conversion of a duration from seconds to nanoseconds.
         * It multiplies the provided seconds value by 1,000,000,000 (the number of nanoseconds in a second).
         * This method is useful when working with durations that are initially measured in seconds but need to be
         * represented or processed as nanoseconds.
         *
         * @param seconds An integer representing the duration in seconds.
         * @return A `Nanos` instance representing the equivalent duration in nanoseconds.
         */
        fun fromSeconds(seconds: Int): Nanos = Nanos(seconds.toLong() * Constants.NANOSECONDS_PER_SECOND.toLong())

        /**
         * Converts an integer value representing milliseconds to `Nanos`.
         *
         * This method in the companion object facilitates the conversion of a duration from milliseconds to
         * nanoseconds. It multiplies the provided milliseconds value by 1,000,000 (the number of nanoseconds in a
         * millisecond). This method is useful when working with durations that are initially measured in milliseconds
         * but need to be represented or processed as nanoseconds.
         *
         * @param milliseconds An integer representing the duration in milliseconds.
         * @return A `Nanos` instance representing the equivalent duration in nanoseconds.
         */
        fun fromMillis(milliseconds: Int): Nanos = Nanos(milliseconds.toLong() * Constants.NANOSECONDS_PER_MILLISECOND)
    }
}

/**
 * Returns the smaller of two `Nanos` instances.
 *
 * This function compares two `Nanos` instances and returns the one with the lesser
 * nanosecond value. If both instances have the same value, it returns the first instance.
 *
 * @param a The first `Nanos` instance to compare.
 * @param b The second `Nanos` instance to compare.
 * @return The `Nanos` instance with the lesser value, or the first instance if they are equal.
 */
fun min(
    a: Nanos,
    b: Nanos,
): Nanos = if (a <= b) a else b

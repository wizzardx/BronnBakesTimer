package com.example.bronnbakestimer.util

import java.util.UUID

/**
 * A value class representing the unique identifier for timer user input data.
 * It wraps a [UUID].
 *
 * @property value The [UUID] representing the unique identifier.
 */
@JvmInline
value class TimerUserInputDataId(val value: UUID) : Comparable<TimerUserInputDataId> {
    override fun compareTo(other: TimerUserInputDataId): Int = value.compareTo(other.value)

    companion object {
        /**
         * Generates a random [TimerUserInputDataId].
         *
         * @return A new instance of [TimerUserInputDataId] with a unique [UUID].
         */
        fun randomId(): TimerUserInputDataId = TimerUserInputDataId(UUID.randomUUID())
    }
}

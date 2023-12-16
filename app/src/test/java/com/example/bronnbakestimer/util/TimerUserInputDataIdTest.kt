package com.example.bronnbakestimer.util

import org.junit.Test
import java.util.UUID
import kotlin.test.assertTrue

@Suppress("FunctionMaxLength")
class TimerUserInputDataIdTest {
    @Test
    fun `compareTo returns positive when first UUID is greater than second`() {
        val uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val uuid2 = UUID.fromString("00000000-0000-0000-0000-000000000000")
        val timerId1 = TimerUserInputDataId(uuid1)
        val timerId2 = TimerUserInputDataId(uuid2)

        assertTrue { timerId1 > timerId2 }
    }

    @Test
    fun `compareTo returns negative when first UUID is less than second`() {
        val uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000000")
        val uuid2 = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val timerId1 = TimerUserInputDataId(uuid1)
        val timerId2 = TimerUserInputDataId(uuid2)

        assertTrue { timerId1 < timerId2 }
    }

    @Test
    fun `compareTo returns zero when UUIDs are equal`() {
        val uuid = UUID.fromString("00000000-0000-0000-0000-000000000000")
        val timerId1 = TimerUserInputDataId(uuid)
        val timerId2 = TimerUserInputDataId(uuid)

        assertTrue { timerId1.compareTo(timerId2) == 0 }
    }
}

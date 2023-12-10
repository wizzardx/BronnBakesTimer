package com.example.bronnbakestimer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Mock implementation of the [ITimerRepository] interface for testing purposes.
 * This class provides a mock [timerData] and does not perform actual data updates.
 */
// TODO: Use DefaultTimerRepository instead of this mock implementation?
class MockTimerRepository : ITimerRepository {

    override val timerData: StateFlow<TimerData?>
        get() = MutableStateFlow(null)

    override fun updateData(newData: TimerData?) {
        // Do nothing here
    }
}

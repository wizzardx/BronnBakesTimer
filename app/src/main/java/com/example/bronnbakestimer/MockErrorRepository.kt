package com.example.bronnbakestimer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Mock implementation of the [IErrorRepository] interface for testing purposes.
 * This class provides a mock [errorMessage] and does not perform actual error message updates.
 */
// TODO: Use DefaultErrorRepository instead of this mock implementation?
class MockErrorRepository : IErrorRepository {
    override val errorMessage: StateFlow<String?>
        get() = MutableStateFlow("Mock Error Message")

    override fun updateData(newMessage: String?) {
        // Do nothing here
    }
}

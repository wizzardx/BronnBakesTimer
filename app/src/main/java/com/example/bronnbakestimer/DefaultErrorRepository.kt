package com.example.bronnbakestimer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A repository for managing error messages in an application.
 *
 * This class implements the IErrorRepository interface and provides functionality for storing and retrieving error
 * messages.
 * It uses a MutableStateFlow to internally update and store error messages, ensuring thread safety and atomic updates.
 * The stored error messages can be observed via a read-only StateFlow, allowing other components in the application
 * to react to changes in the error state.
 *
 * The `updateData` function allows updating the current error message, with a `null` value indicating the absence of
 * errors.
 */
class DefaultErrorRepository : IErrorRepository {
    // MutableStateFlow for internal updates
    private val _errorMessage = MutableStateFlow<String?>(null)

    /**
     * A read-only [StateFlow] that emits the current error message.
     * It's `null` when there's no error, allowing observers to react to changes in the error state.
     */
    override val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * Updates the current error message.
     *
     * This function is thread-safe and can be called from any coroutine context to update the error message.
     * The update is atomic, ensuring that all observers see a consistent state.
     *
     * @param newMessage The new error message to be emitted. If `null`, it indicates that there is no current error.
     */
    override fun updateData(newMessage: String?) {
        _errorMessage.value = newMessage // Atomic and thread-safe update
    }
}

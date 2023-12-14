package com.example.bronnbakestimer.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for managing error messages within the application.
 * Implementations of this interface provide methods to update and access error messages.
 */
interface IErrorRepository {
    /**
     * A read-only [StateFlow] that emits the current error message.
     * It's `null` when there's no error, allowing observers to react to changes in the error state.
     */
    val errorMessage: StateFlow<String?>

    /**
     * Updates the current error message.
     *
     * This function is thread-safe and can be called from any coroutine context to update the error message.
     * The update is atomic, ensuring that all observers see a consistent state.
     *
     * @param newMessage The new error message to be emitted. If `null`, it indicates that there is no current error.
     */
    fun updateData(newMessage: String?)
}

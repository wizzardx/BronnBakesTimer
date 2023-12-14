package com.example.bronnbakestimer.provider

/**
 * Provides an interface for logging errors.
 *
 * This interface allows for different implementations of error logging,
 * facilitating testing and custom logging strategies.
 */
fun interface IErrorLoggerProvider {
    /**
     * Logs an error with an associated throwable.
     *
     * @param tag A string tag used to identify the source of the error.
     * @param message The error message to be logged.
     * @param throwable The Throwable associated with the error.
     */
    fun logError(tag: String, message: String, throwable: Throwable)

    /**
     * Logs an error without an associated throwable.
     *
     * This method provides a convenient way to log errors that are not
     * associated with a specific throwable.
     *
     * @param tag A string tag used to identify the source of the error.
     * @param message The error message to be logged.
     */
    fun logError(tag: String, message: String) {
        // This version is called when no Throwable is provided
    }
}

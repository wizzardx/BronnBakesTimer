package com.example.bronnbakestimer.util

import android.util.Log
import com.example.bronnbakestimer.provider.IErrorLoggerProvider
import com.example.bronnbakestimer.repository.IErrorRepository

/**
 * Logs an exception and reports its message to the ErrorRepository.
 *
 * This function takes an exception as input and performs two actions:
 * 1. It updates the ErrorRepository with the message of the exception. This allows other components of the application
 *    to react to the error state as needed.
 * 2. It logs the exception at the "Error" level using the provided ErrorLoggerProvider. The log is tagged with
 *    "BronnBakesTimer" and prefixed with "Error occurred: " to distinguish it in the application's log output.
 *
 * This function is a useful utility for uniform exception handling across the application, promoting consistency and
 * ease of debugging.
 *
 * @param exception The exception to be logged and reported.
 * @param errorRepository The IErrorRepository instance where the exception message will be reported.
 * @param logger The ErrorLoggerProvider used for logging the exception.
 */
fun logException(
    exception: Throwable,
    errorRepository: IErrorRepository,
    logger: IErrorLoggerProvider,
) {
    errorRepository.updateData(exception.message)
    logger.logError("BronnBakesTimer", "Error occurred: ", exception)
}

/**
 * Logs an error message and reports it to the ErrorRepository.
 *
 * This function takes an error message as input and performs two actions:
 * 1. It updates the ErrorRepository with the error message. This allows other components of the application
 *    to react to the error state as needed.
 * 2. It logs the error message at the "Error" level using the provided ErrorLoggerProvider. The log is tagged with
 *    "BronnBakesTimer" and the error message is used as the log message.
 *
 * This function is a useful utility for uniform error handling across the application, promoting consistency and
 * ease of debugging.
 *
 * @param msg The error message to be logged and reported.
 * @param errorRepository The IErrorRepository instance where the error message will be reported.
 * @param logger The ErrorLoggerProvider used for logging the error. If null then no exception to log
 */
fun logError(
    msg: String,
    errorRepository: IErrorRepository,
    logger: IErrorLoggerProvider,
) {
    errorRepository.updateData(msg)
    logger.logError("BronnBakesTimer", msg, null)
}

/**
 * Provides a runtime implementation of the ErrorLoggerProvider interface.
 *
 * This variable is an instance of ErrorLoggerProvider that uses Android's Log.e method to log error messages.
 * It is intended to be used in a runtime environment, where logs are written to the Android log output.
 *
 * The ErrorLoggerProvider interface takes three parameters: a tag, a message, and a Throwable. The tag is used
 * to identify the source of the log message. The message is the actual content to be logged. The Throwable is
 * the exception that caused the error.
 *
 * In this implementation, the tag, message, and Throwable are passed directly to Log.e. This writes an error
 * message to the Android log output, which can be viewed and filtered in the Logcat window in Android Studio.
 */
val runtimeErrorLoggerProvider =
    IErrorLoggerProvider { tag, message, throwable ->
        // It's hard to unit test this, since Log.e is an android internal, so we can't mock it.
        // We can manually check it though, by getting an error to show in the UI.
        Log.e(tag, message, throwable)
    }

/**
 * Provides a runtime implementation of the ErrorLoggerProvider interface.
 *
 * This variable is an instance of ErrorLoggerProvider that uses Android's Log.e method to log error messages.
 * It is intended to be used in a runtime environment, where logs are written to the Android log output.
 *
 * The ErrorLoggerProvider interface takes three parameters: a tag, a message, and a Throwable. The tag is used
 * to identify the source of the log message. The message is the actual content to be logged. The Throwable is
 * the exception that caused the error.
 *
 * In this implementation, the tag, message, and Throwable are passed directly to Log.e. This writes an error
 * message to the Android log output, which can be viewed and filtered in the Logcat window in Android Studio.
 */
val testErrorLoggerProvider =
    IErrorLoggerProvider { tag, message, throwable ->
        // Custom implementation for testing, e.g., print to console or use a mock logger
        var msg = "[$tag] $message"
        if (throwable != null) {
            msg += " - ${throwable.message}"
        }
        println(msg)
    }

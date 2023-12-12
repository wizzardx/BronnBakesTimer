package com.example.bronnbakestimer

import kotlinx.coroutines.CoroutineExceptionHandler
import org.koin.core.context.GlobalContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * Utility object containing shared elements for coroutine handling.
 */
object CoroutineUtils {
    private val errorRepository: IErrorRepository = GlobalContext.get().get()
    private val errorLoggerProvider: IErrorLoggerProvider = GlobalContext.get().get()

    /**
     * A CoroutineExceptionHandler for handling uncaught exceptions in coroutines.
     * Logs the exception and terminates the application.
     *
     * The handler first logs the exception details, including the coroutine context.
     * Then, it forcefully stops the process and exits the application.
     *
     * Usage of this handler is suitable for cases where any uncaught exception in a coroutine
     * is considered critical and should result in a complete application shutdown.
     */
    val sharedExceptionHandler =
        CoroutineExceptionHandler { _, exception ->
            if (exception !is CancellationException) {
                // Log the exception
                logException(exception, errorRepository, errorLoggerProvider)
            }
        }
}

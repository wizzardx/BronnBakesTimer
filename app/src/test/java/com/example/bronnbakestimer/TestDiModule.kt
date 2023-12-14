package com.example.bronnbakestimer

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module used for providing test implementations of dependencies during testing.
 * This module is intended for use in unit testing scenarios to replace real implementations with mocks.
 */
val testModule = module {
    // Provide a test implementation of TimerRepository
    single<ITimerRepository> { DefaultTimerRepository() }

    // Provide DefaultExtraTimersCountdownRepository for IExtraTimersCountdownRepository
    single<IExtraTimersCountdownRepository> { DefaultExtraTimersCountdownRepository() }

    // Provide a test implementation of ErrorRepository
    single<IErrorRepository> { DefaultErrorRepository() }

    // Just use the original viewmodel here, it works fine in preview mode.
    viewModel { BronnBakesTimerViewModel(get(), get(), get(), get(), get(), get(), get()) }

    // Provide a test implementation of ErrorLoggerProvider
    single<IErrorLoggerProvider> { testErrorLoggerProvider }

    // Provide a Test CoroutineScope using TestScope
    single<CoroutineScopeProvider> { TestCoroutineScopeProvider() }
}

package com.example.bronnbakestimer.di

import com.example.bronnbakestimer.provider.CoroutineScopeProvider
import com.example.bronnbakestimer.provider.IErrorLoggerProvider
import com.example.bronnbakestimer.provider.TestCoroutineScopeProvider
import com.example.bronnbakestimer.repository.DefaultErrorRepository
import com.example.bronnbakestimer.repository.DefaultExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.DefaultExtraTimersUserInputsRepository
import com.example.bronnbakestimer.repository.DefaultTimerRepository
import com.example.bronnbakestimer.repository.IErrorRepository
import com.example.bronnbakestimer.repository.IExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.IExtraTimersUserInputsRepository
import com.example.bronnbakestimer.repository.ITimerRepository
import com.example.bronnbakestimer.util.testErrorLoggerProvider
import com.example.bronnbakestimer.viewmodel.BronnBakesTimerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module used for providing test implementations of dependencies during testing.
 * This module is intended for use in unit testing scenarios to replace real implementations with mocks.
 */
val testModule = module {
    // Provide a test implementation of TimerRepository
    single<ITimerRepository> { DefaultTimerRepository() }

    // Bind DefaultExtraTimersUserInputsRepository instance to IExtraTimersUserInputsRepository
    single<IExtraTimersUserInputsRepository> { DefaultExtraTimersUserInputsRepository() }

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

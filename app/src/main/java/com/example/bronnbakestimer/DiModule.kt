package com.example.bronnbakestimer

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for configuring dependencies in the BronnBakesTimer application.
 * This module defines various singletons for dependency injection, including:
 * - Singleton instance of MediaPlayerWrapper for handling media playback.
 * - Singleton instance of TimerRepository for managing timer data.
 * - Singleton instance of ErrorRepository for handling errors.
 * - Definition of the IBronnBakesTimerViewModel for the application's view model's Interface.
 */
val appModule = module {
    // Singleton instance of MediaPlayerWrapper
    single<IMediaPlayerWrapper> { MediaPlayerWrapper(androidContext(), R.raw.buzzer) }

    // Bind TimerRepository instance to ITimerRepository
    single<ITimerRepository> { DefaultTimerRepository() }

    // Bind ExtraTimersRepository instance to IExtraTimersRepository
    single<IExtraTimersRepository> { DefaultExtraTimersRepository() }

    // Bind DefaultRepository instance to IErrorRepository
    single<IErrorRepository> { DefaultErrorRepository() }

    // Bind TimerManager instance to TimerManager
    single<ITimerManager> { DefaultTimerManager() }

    // Bind InputValidator instance to InputValidator
    single<IInputValidator> { DefaultInputValidator() }

    // Define the ViewModel
    viewModel { BronnBakesTimerViewModel(get(), get(), get(), get(), get()) }
}

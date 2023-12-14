package com.example.bronnbakestimer.di

import com.example.bronnbakestimer.R
import com.example.bronnbakestimer.logic.DefaultInputValidator
import com.example.bronnbakestimer.logic.DefaultTimerManager
import com.example.bronnbakestimer.logic.IInputValidator
import com.example.bronnbakestimer.provider.CoroutineScopeProvider
import com.example.bronnbakestimer.provider.IErrorLoggerProvider
import com.example.bronnbakestimer.provider.IMediaPlayerWrapper
import com.example.bronnbakestimer.provider.ProductionCoroutineScopeProvider
import com.example.bronnbakestimer.repository.DefaultErrorRepository
import com.example.bronnbakestimer.repository.DefaultExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.DefaultExtraTimersUserInputsRepository
import com.example.bronnbakestimer.repository.DefaultTimerRepository
import com.example.bronnbakestimer.repository.IErrorRepository
import com.example.bronnbakestimer.repository.IExtraTimersCountdownRepository
import com.example.bronnbakestimer.repository.IExtraTimersUserInputsRepository
import com.example.bronnbakestimer.repository.ITimerRepository
import com.example.bronnbakestimer.service.ITimerManager
import com.example.bronnbakestimer.service.NotificationHelper
import com.example.bronnbakestimer.util.MediaPlayerWrapper
import com.example.bronnbakestimer.util.runtimeErrorLoggerProvider
import com.example.bronnbakestimer.viewmodel.BronnBakesTimerViewModel
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

    // Bind DefaultExtraTimersUserInputsRepository instance to IExtraTimersUserInputsRepository
    single<IExtraTimersUserInputsRepository> { DefaultExtraTimersUserInputsRepository() }

    // Bind DefaultExtraTimersCountdownRepository instance to IExtraTimersCountdownRepository
    single<IExtraTimersCountdownRepository> { DefaultExtraTimersCountdownRepository() }

    // Bind DefaultRepository instance to IErrorRepository
    single<IErrorRepository> { DefaultErrorRepository() }

    // Bind TimerManager instance to TimerManager
    single<ITimerManager> { DefaultTimerManager() }

    // Bind InputValidator instance to InputValidator
    single<IInputValidator> { DefaultInputValidator() }

    // Bind ErrorLoggerProvider instance to runtimeErrorLoggerProvider
    single<IErrorLoggerProvider> { runtimeErrorLoggerProvider }

    // Define the ViewModel
    viewModel { BronnBakesTimerViewModel(get(), get(), get(), get(), get(), get(), get()) }

    // Provide CoroutineScope for TimerService
    single<CoroutineScopeProvider> { ProductionCoroutineScopeProvider() }

    // Provide NotificationHelper for TimerService
    single<NotificationHelper> { NotificationHelper(androidContext()) }
}

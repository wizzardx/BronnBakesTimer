package com.example.bronnbakestimer

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * Custom Application class for the BronnBakesTimer application.
 *
 * This class extends the Android `Application` class and is responsible for initializing the BronnBakesTimer app.
 * It sets up the Koin dependency injection framework and configures application-wide dependencies by declaring
 * Koin modules.
 *
 * @see startKoin
 * @see androidLogger
 * @see androidContext
 * @see appModule
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}

/**
 * Koin module used for providing mock implementations of dependencies during testing.
 * This module is intended for use in unit testing scenarios to replace real implementations with mocks.
 */
val testModule = module {
    // Provide mock implementations for your dependencies

    // Provide a mock implementation of TimerRepository
    single<ITimerRepository> { MockTimerRepository() }

    // Bind ExtraTimersRepository instance to IExtraTimersRepository
    single<IExtraTimersRepository> { DefaultExtraTimersRepository() }

    // Provide a mock implementation of ErrorRepository
    single<IErrorRepository> { MockErrorRepository() }

    // Just use the original viewmodel here, it works fine in preview mode.
    viewModel { BronnBakesTimerViewModel(get(), get(), get(), get(), get()) }
}

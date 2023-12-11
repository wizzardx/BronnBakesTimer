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

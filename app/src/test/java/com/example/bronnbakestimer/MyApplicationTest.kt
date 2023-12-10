package com.example.bronnbakestimer

import android.app.Application
import android.content.Context
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.lang.ref.WeakReference
import kotlin.test.assertSame

fun testModule(application: Application) = module {
    // Mock or specific implementations for your dependencies
    single<Application> { application }

    // Other mock definitions as needed
    single<ITimerRepository> { MockTimerRepository() }
    single<IExtraTimersRepository> { DefaultExtraTimersRepository() }
    single<IErrorRepository> { MockErrorRepository() }
    viewModel { BronnBakesTimerViewModel(get(), get(), get()) }
    // ... add other mocks or specific implementations ...
}

@Suppress("FunctionMaxLength")
@RunWith(MockitoJUnitRunner::class)
class MyApplicationTest : KoinTest {

    private lateinit var application: MyApplication

    @Before
    fun setup() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this)
        // Create a mock context
        val context = Mockito.mock(Context::class.java)
        MyApplication.mockContext = WeakReference(context)
        // Create a spy of MyApplication
        application = Mockito.spy(MyApplication::class.java)

        // Start Koin with the test-specific module
        startKoin {
            androidContext(context)
            modules(testModule(application)) // Pass the application instance
        }

        application.initializeKoin(isTest = true)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `test MyApplication initializes Koin`() {
        // Now, check that Koin is initialized correctly
        val koin = GlobalContext.get()
        assertSame(koin.get<Application>(), application, "Koin should be initialized with MyApplication context")
    }
}

package com.example.bronnbakestimer

import android.content.Context
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.check.checkModules
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertSame

@Suppress("FunctionMaxLength")
@RunWith(MockitoJUnitRunner::class)
class MyApplicationTest : KoinTest {

    @Mock
    lateinit var mockContext: Context

    private val testModule: Module = module {
        // Define your mocked dependencies here
    }

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        startKoin {
            androidContext(mockContext)
            modules(testModule)
        }
    }

    @Test
    fun `test Koin modules initialization`() {
        GlobalContext.get().checkModules()
    }

    @Test
    fun `test Android context in Koin`() {
        val koinApplication = GlobalContext.get()
        assertSame(koinApplication.get<Context>(), mockContext, "Android context should be the same as mockContext")
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}

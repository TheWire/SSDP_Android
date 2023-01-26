package com.thewire.ssdp_android

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun ssdp_discover_should_emit_discovering_without_error() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val ssdp = SSDP()
        runBlocking {
            val result = ssdp.discover(appContext).first()
            assertEquals(DiscoverResult.Discovering, result)
        }
    }
    @Test
    fun ssdp_discover_should_emit_some_services() {
        Log.d("TEST","test start")
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val ssdp = SSDP()
        var first = true
        runBlocking {
            ssdp.discover(appContext,timeout=1).collect { result ->
                if(first) {
                    Log.d("TEST","first collect $result")
                    assertEquals(DiscoverResult.Discovering, result)
                    first = false
                } else {
                    Log.d("TEST","subsequent collect $result")
                    assert(result is DiscoverResult.Result)
                }
            }
        }
        Log.d("TEST","test end")
    }
}
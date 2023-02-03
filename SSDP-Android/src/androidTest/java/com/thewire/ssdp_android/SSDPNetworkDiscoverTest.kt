package com.thewire.ssdp_android

import android.content.Context
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class SSDPNetworkDiscoverTest {

    private val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val ssdp = SSDP(appContext)

    @Test
    fun ssdp_discover_should_emit_discovering_without_error() {
        // Context of the app under test.
        runBlocking {
            val result = ssdp.discover(probe = false, duration = 1).first()
            assertEquals(DiscoverMessage.Discovering, result)
        }
    }

    @Test
    fun ssdp_probe_should_run_and_cancel_without_error() {
        runBlocking {
            val job = launch {
                ssdp.probe()
            }
            delay(1000)
            job.cancel()
        }
    }

    @Test
    fun ssdp_discover_should_emit_some_services() {
        var first = true
        runBlocking {
            ssdp.lockMulticast()
            ssdp.discover(probe = true, duration = 5).collect { result ->
                if (first) {
                    Log.d("TEST", "first collect $result")
                    assertEquals(DiscoverMessage.Discovering, result)
                    first = false
                } else {
                    assertEquals(DiscoverMessage.Message::class, result::class)
                    val message = (result as DiscoverMessage.Message)
                    Log.d("TEST", "subsequent collect ${message.service.location}")
//                    Log.d("TEST", message.service.responseString)
                }
            }
            ssdp.releaseMulticast()
        }
    }

    @Test
    fun ssdp_discover_should_emit_some_services_cont_probe() {
        var first = true
        runBlocking {
            ssdp.lockMulticast()
            val job = launch {
                ssdp.probe()
            }
            ssdp.discover(probe = false, duration = 60).collect { result ->
                if (first) {
                    Log.d("TEST", "first collect $result")
                    assertEquals(DiscoverMessage.Discovering, result)
                    first = false
                } else {
                    assertEquals(DiscoverMessage.Message::class, result::class)
                    val message = (result as DiscoverMessage.Message)
                    Log.d(
                        "TEST",
                        "subsequent collect ${message.service.host} ${message.service.location}"
                    )
//                    Log.d("TEST", message.service.responseString)
                }
            }
            job.cancel()
            ssdp.releaseMulticast()
        }
    }

    @Test
    fun ssdp_discover_should_emit_unique_results() {
        val results = mutableSetOf<String>()
        runBlocking {
            ssdp.lockMulticast()
            ssdp.discover(probe = true, duration = 120).collect { result ->
                if (result::class == DiscoverMessage.Message::class) {
                    val message = (result as DiscoverMessage.Message)
                    assertTrue(results.add(message.service.responseString))
                }
            }
            ssdp.releaseMulticast()
        }
    }
}
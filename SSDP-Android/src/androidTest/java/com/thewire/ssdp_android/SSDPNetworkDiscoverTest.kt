package com.thewire.ssdp_android

import android.content.Context
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.thewire.ssdp_android.model.DiscoverMessage
import com.thewire.ssdp_android.network.DeviceProfileXmlParser
import com.thewire.ssdp_android.network.SSDPRepository
import com.thewire.ssdp_android.ssdp.SSDP
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class SSDPNetworkDiscoverTest {

    private val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val repository = SSDPRepository()
    private val ssdp = SSDP(appContext, repository)
    private val parser = DeviceProfileXmlParser()

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
    @Test
    fun parser_should_parse_xml_correctly() {
        val testProfile = """
        <root xmlns="urn:schemas-upnp-org:device-1-0">
            <specVersion>
                <major>1</major>
                <minor>0</minor>
            </specVersion>
            <URLBase>http://192.168.0.250</URLBase>
            <device xmlns:pnpx="http://schemas.microsoft.com/windows/pnpx/2005/11">
                <pnpx:X_deviceCategory>Storage.NAS</pnpx:X_deviceCategory>
                <deviceType>urn:schemas-upnp-org:device:networkstoragedevice:1</deviceType>
                <friendlyName>My Device</friendlyName>
                <manufacturer>Generic Manufacturer</manufacturer>
                <modelName>Device MK1</modelName>
                <modelURL>http://www.google.com/</modelURL>
                <modelNumber>1</modelNumber>
                <serialNumber>01:02:03:04:05:06</serialNumber>
                <UDN>uuid:a7387a40-2402-11ed-861d-0242ac120002</UDN>
            </device>
        </root>
        """.trimIndent().encodeToByteArray()
        val stream = ByteArrayInputStream(testProfile)
        val device = parser.parseStream(stream)
        assertEquals("1", device.specVersion?.first)
        assertEquals("0", device.specVersion?.second)
        assertEquals("uuid:a7387a40-2402-11ed-861d-0242ac120002", device.device?.UDN)
        assertEquals("Storage.NAS", device.device?.X_deviceCategory)
        assertEquals("urn:schemas-upnp-org:device:networkstoragedevice:1", device.device?.deviceType)
        assertEquals("My Device", device.device?.friendlyName)
        assertEquals("Generic Manufacturer", device.device?.manufacturer)
        assertEquals("Device MK1", device.device?.modelName)
        assertEquals("http://www.google.com/", device.device?.modelURL)
        assertEquals("1", device.device?.modelNumber)
        assertEquals("01:02:03:04:05:06", device.device?.serialNumber)
        assertEquals("http://192.168.0.250", device.URLBase)
    }

    @Test
    fun should_get_some_profile_data() {
        var first = true
        runBlocking {
            ssdp.lockMulticast()
            ssdp.discover(probe = true, duration = 60).collect { result ->
                if (first) {
                    first = false
                } else {
                    if(result::class == DiscoverMessage.Error::class) {
                        val error = (result as DiscoverMessage.Error)
                        Log.d("TEST", "error $error")
                    }
                    assertEquals(DiscoverMessage.Message::class, result::class)
                    val message = (result as DiscoverMessage.Message)
                    Log.d("TEST", "subsequent collect ${message.service.deviceProfile?.values}")
//                    Log.d("TEST", message.service.responseString)
                }
            }
            ssdp.releaseMulticast()
        }
    }
}
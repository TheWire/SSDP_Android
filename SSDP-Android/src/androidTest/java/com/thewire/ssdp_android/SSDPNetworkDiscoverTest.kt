package com.thewire.ssdp_android

import android.content.Context
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.thewire.ssdp_android.model.DiscoverMessage
import com.thewire.ssdp_android.network.DeviceProfileXmlParser
import com.thewire.ssdp_android.network.SSDPDeviceHTTPService
import com.thewire.ssdp_android.ssdp.SSDP
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParserException
import java.io.ByteArrayInputStream

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

private const val TAG_TEST = "SSDP_TEST"

@RunWith(AndroidJUnit4::class)
class SSDPNetworkDiscoverTest {

    private val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val repository = SSDPDeviceHTTPService()
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
                    Log.i(TAG_TEST, "first collect $result")
                    assertEquals(DiscoverMessage.Discovering, result)
                    first = false
                } else {
                    assertEquals(DiscoverMessage.Message::class, result::class)
                    val message = (result as DiscoverMessage.Message)
                    Log.i(TAG_TEST, "subsequent collect ${message.service.location}")
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
                    Log.i(TAG_TEST, "first collect $result")
                    assertEquals(DiscoverMessage.Discovering, result)
                    first = false
                } else {
                    assertEquals(DiscoverMessage.Message::class, result::class)
                    val message = (result as DiscoverMessage.Message)
                    Log.i(
                        TAG_TEST,
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
    fun parser_should_throw_XmlPullParserException_if_non_xml() {
        val testProfile = """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus faucibus augue quis 
            nisi porttitor tempor. Integer lectus quam, volutpat id sollicitudin dignissim, 
            porttitor sed magna. Nam gravida, nulla quis tincidunt interdum, risus purus sodales mi, 
            ultrices molestie ipsum dui vitae urna. Suspendisse aliquet enim ante, pulvinar vehicula 
            libero pretium a.
        """.trimIndent().encodeToByteArray()
        val stream = ByteArrayInputStream(testProfile)
        try {
            parser.parseStream(stream)
        } catch (e: XmlPullParserException) {
            Log.i(TAG_TEST, e.message ?: "parser exception")
        }

    }

    @Test
    fun parser_should_throw_XmlPullParserException_if_invalid_device_profile_xml() {
        val testProfile = """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                <uses-permission android:name="android.permission.INTERNET" />
                <uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE" />
            
                <application android:usesCleartextTraffic="true"/>
            </manifest>
        """.trimIndent().encodeToByteArray()
        val stream = ByteArrayInputStream(testProfile)
        try {
            parser.parseStream(stream)
        } catch (e: XmlPullParserException) {
            Log.i(TAG_TEST, e.message ?: "parser exception")
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
        assertEquals(
            "urn:schemas-upnp-org:device:networkstoragedevice:1",
            device.device?.deviceType
        )
        assertEquals("My Device", device.device?.friendlyName)
        assertEquals("Generic Manufacturer", device.device?.manufacturer)
        assertEquals("Device MK1", device.device?.modelName)
        assertEquals("http://www.google.com/", device.device?.modelURL)
        assertEquals("1", device.device?.modelNumber)
        assertEquals("01:02:03:04:05:06", device.device?.serialNumber)
        assertEquals("http://192.168.0.250", device.URLBase)
    }

    @Test
    fun parser_should_parse_more_complex_xml_without_error() {
        val testProfile = """
            <root configId="1234">
        <specVersion>
        <major>1</major>
        <minor>0</minor>
        </specVersion>
        <device>
        <deviceType>
                urn:schemas-upnp-org:device:InternetGatewayDevice:1
        </deviceType>
        <friendlyName>My Device</friendlyName>
        <manufacturer>Computer Inc.</manufacturer>
        <manufacturerURL>http://www.example.com/</manufacturerURL>
        <modelDescription>Description of device</modelDescription>
        <modelName>Example Model Name</modelName>
        <modelNumber>1.0.0.1</modelNumber>
        <modelURL>http://www.example.com/</modelURL>
        <serialNumber>01:d2:c3:c4:e5:96</serialNumber>
        <UDN>uuid:1234567890</UDN>
        <serviceList>
        <service>
        <serviceType>urn:schemas-upnp-org:service:Layer3Forwarding:1</serviceType>
        <serviceId>urn:upnp-org:serviceId:Layer3Forwarding1</serviceId>
        <controlURL>/ctl/F</controlURL>
        <eventSubURL>/evt/F</eventSubURL>
        <SCPDURL>/f.xml</SCPDURL>
        </service>
        <service>
        <serviceType>urn:schemas-upnp-org:service:1</serviceType>
        <serviceId>urn:upnp-org:serviceId:service1</serviceId>
        <controlURL>/ctl/1</controlURL>
        <eventSubURL>/evt/1</eventSubURL>
        <SCPDURL>/sd.xml</SCPDURL>
        </service>
        </serviceList>
        <deviceList>
        <device>
        <deviceType>urn:schemas-upnp-org:device:WANDevice:1</deviceType>
        <friendlyName>WANDevice</friendlyName>
        <manufacturer>MiniUPnP</manufacturer>
        <manufacturerURL>http://example.com/</manufacturerURL>
        <modelDescription>WAN Device</modelDescription>
        <modelName>WAN Device</modelName>
        <modelNumber>123456</modelNumber>
        <modelURL>http://example.com/</modelURL>
        <serialNumber>01:d2:c3:c4:e5:96</serialNumber>
        <UDN>uuid:1234567890</UDN>
        <UPC>000000000000</UPC>
        <serviceList>
        <service>
        <serviceType>
                urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1
        </serviceType>
        <serviceId>urn:upnp-org:serviceId:WANCommonIFC1</serviceId>
        <controlURL>/ctl/x</controlURL>
        <eventSubURL>/evt/x</eventSubURL>
        <SCPDURL>/g.xml</SCPDURL>
        </service>
        </serviceList>
        <deviceList>
        <device>
        <deviceType>urn:schemas-upnp-org:device:WANConnectionDevice:1</deviceType>
        <friendlyName>WANConnectionDevice</friendlyName>
        <manufacturer>MiniUPnP</manufacturer>
        <manufacturerURL>http://example.com/</manufacturerURL>
        <modelDescription>MiniUPnP daemon</modelDescription>
        <modelName>MiniUPnPd</modelName>
        <modelNumber>123456</modelNumber>
        <modelURL>http://miniupnp.free.fr/</modelURL>
        <serialNumber>01:d2:c3:c4:e5:96</serialNumber>
        <UDN>uuid:1234567890</UDN>
        <UPC>000000000000</UPC>
        <serviceList>
        <service>
        <serviceType>urn:schemas-upnp-org:service:WANIPConnection:1</serviceType>
        <serviceId>urn:upnp-org:serviceId:WANIPConn1</serviceId>
        <controlURL>/ctl/c</controlURL>
        <eventSubURL>/evt/c</eventSubURL>
        <SCPDURL>/n.xml</SCPDURL>
        </service>
        </serviceList>
        </device>
        </deviceList>
        </device>
        </deviceList>
        <presentationURL>http://192.168.0.1:80/</presentationURL>
        </device>
        </root>
        """.trimIndent().encodeToByteArray()
        val stream = ByteArrayInputStream(testProfile)
        val device = parser.parseStream(stream)
        assertEquals("My Device", device.device?.friendlyName)
        assertEquals("1.0.0.1", device.device?.modelNumber)
        assertEquals("Example Model Name", device.device?.modelName)
        assertEquals("Description of device", device.device?.modelDescription)
        assertEquals("http://192.168.0.1:80/", device.device?.presentationURL)
        assertEquals("Computer Inc.", device.device?.manufacturer)

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
                    if (result::class == DiscoverMessage.Error::class) {
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
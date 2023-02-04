package com.thewire.ssdp_android.network

import android.util.Xml
import com.thewire.ssdp_android.model.DeviceProfile
import com.thewire.ssdp_android.model.SSDPDevice
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

class DeviceProfileXmlParser {
    fun parseStream(inputStream: InputStream): DeviceProfile {
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            return parse(parser)
        }
    }

    private fun parseToMap(parser: XmlPullParser, map: MutableMap<String, String>) {
        if (parser.text != "") {
            map[parser.name] = parser.text
        }
    }

    private fun parse(parser: XmlPullParser): DeviceProfile {
        val values = mutableMapOf<String, String>()
        val deviceProfile = DeviceProfile.Builder()
        parser.require(XmlPullParser.START_TAG, null, "root")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            parseToMap(parser, values)
            when (parser.name) {
                "device" -> deviceProfile.device(parseDevice(parser, values))
                "URLBase" -> deviceProfile.URLBase(parser.text)
            }
        }
        deviceProfile.values(values)
        return deviceProfile.build()
    }

    private fun parseDevice(parser: XmlPullParser, values: MutableMap<String, String>): SSDPDevice {
        parser.require(XmlPullParser.START_TAG, null, "device")
        val device = SSDPDevice.Builder()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                throw IllegalStateException()
            }
            parseToMap(parser, values)
            when (parser.name) {
                "pnpx:X_deviceCategory" -> device.X_deviceCategory(parser.text)
                "deviceType" -> {}
                "friendlyName" -> {}
                "manufacturer" -> {}
                "modelName" -> {}
                "modelURL" -> {}
                "modelNumber" -> {}
                "serialNumber" -> {}
                "UDN" -> {}
            }
        }
        return device.build()
    }
}
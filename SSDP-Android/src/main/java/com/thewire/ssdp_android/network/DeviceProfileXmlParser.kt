package com.thewire.ssdp_android.network

import android.util.Xml
import com.thewire.ssdp_android.model.DeviceProfile
import com.thewire.ssdp_android.model.SSDPDevice
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

class DeviceProfileXmlParser {
    fun parseStream(inputStream: InputStream): DeviceProfile {
        inputStream.use { stream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(stream, null)
            parser.nextTag()
            return parse(parser)
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
            when (parser.name) {
                "device" -> deviceProfile.device(parseDevice(parser, values))
                "URLBase" -> deviceProfile.URLBase(parseElement(parser, "URLBase", values))
                "specVersion" -> deviceProfile.specVersion(parseSpec(parser, values))
            }
        }
        deviceProfile.values(values)
        return deviceProfile.build()
    }

    private fun parseElement(parser: XmlPullParser, tag: String, values: MutableMap<String, String>): String {
        parser.require(XmlPullParser.START_TAG, null, tag)
        val text = parseText(parser)
        values[tag] = text
        parser.require(XmlPullParser.END_TAG, null, tag)
        return text
    }

    private fun parseText(parser: XmlPullParser): String {
        var result = ""
        if(parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    private fun parseSpec(parser: XmlPullParser, values: MutableMap<String, String>): Pair<String, String> {
        parser.require(XmlPullParser.START_TAG, null, "specVersion")
        var major = ""
        var minor = ""
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when(parser.name) {
                "major" -> major = parseElement(parser, "major", values)
                "minor" -> minor = parseElement(parser, "minor", values)
            }
        }
        return Pair(major, minor)
    }

    private fun parseDevice(parser: XmlPullParser, values: MutableMap<String, String>): SSDPDevice {
        parser.require(XmlPullParser.START_TAG, null, "device")
        val device = SSDPDevice.Builder()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "pnpx:X_deviceCategory" -> device.X_deviceCategory(parseElement(parser, "pnpx:X_deviceCategory", values))
                "deviceType" -> device.deviceType(parseElement(parser, "deviceType", values))
                "friendlyName" -> device.friendlyName(parseElement(parser, "friendlyName", values))
                "manufacturer" -> device.manufacturer(parseElement(parser, "manufacturer", values))
                "modelName" -> device.modelName(parseElement(parser, "modelName", values))
                "modelURL" -> device.modelURL(parseElement(parser, "modelURL", values))
                "modelNumber" -> device.modelNumber(parseElement(parser, "modelNumber", values))
                "serialNumber" -> device.serialNumber(parseElement(parser, "serialNumber", values))
                "UDN" -> device.UDN(parseElement(parser, "UDN", values))
            }
        }
        return device.build()
    }
}
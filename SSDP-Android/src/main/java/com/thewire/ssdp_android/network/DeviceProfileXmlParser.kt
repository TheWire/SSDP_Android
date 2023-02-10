package com.thewire.ssdp_android.network

import android.util.Xml
import com.thewire.ssdp_android.model.DeviceProfile
import com.thewire.ssdp_android.model.DeviceDescription
import com.thewire.ssdp_android.model.ServiceInformation
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
            println("parse ${parser.name}")
            when (parser.name) {
                "device" -> deviceProfile.device(parseDevice(parser))
                "deviceList" -> deviceProfile.deviceList(parseDeviceList(parser))
                "service" -> deviceProfile.service(parseService(parser))
                "serviceList" -> deviceProfile.serviceList(parseServiceList(parser))
                "URLBase" -> deviceProfile.URLBase(parseElement(parser, "URLBase"))
                "specVersion" -> deviceProfile.specVersion(parseSpec(parser))
                else -> values[parser.name] = parseElement(parser, parser.name)
            }
        }
        deviceProfile.values(values)
        return deviceProfile.build()
    }

    private fun parseElement(parser: XmlPullParser, tag: String): String {
        parser.require(XmlPullParser.START_TAG, null, tag)
        val text = parseText(parser)
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

    private fun parseSpec(parser: XmlPullParser): Pair<String, String> {
        parser.require(XmlPullParser.START_TAG, null, "specVersion")
        var major = ""
        var minor = ""
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when(parser.name) {
                "major" -> major = parseElement(parser, "major")
                "minor" -> minor = parseElement(parser, "minor")
            }
        }
        return Pair(major, minor)
    }

    private fun parseService(parser: XmlPullParser): ServiceInformation {
        parser.require(XmlPullParser.START_TAG, null, "service")
        val service = ServiceInformation.Builder()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "serviceType" -> service.serviceType(parseElement(parser, "serviceType"))
                "serviceId" -> service.serviceId(parseElement(parser, "serviceId"))
                "SCPDURL" -> service.SCPDURL(parseElement(parser, "SCPDURL"))
                "controlURL" -> service.controlURL(parseElement(parser, "controlURL"))
                "eventSubURL" -> service.eventSubURL(parseElement(parser, "eventSubURL"))
            }
        }
        return service.build()
    }

    private fun parseDeviceList(parser: XmlPullParser): List<DeviceDescription> {
        parser.require(XmlPullParser.START_TAG, null, "deviceList")
        val list = mutableListOf<DeviceDescription>()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == "device") {
                list.add(parseDevice(parser))
            }
        }
        return list
    }

    private fun parseServiceList(parser: XmlPullParser): List<ServiceInformation> {
        parser.require(XmlPullParser.START_TAG, null, "serviceList")
        val list = mutableListOf<ServiceInformation>()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == "service") {
                list.add(parseService(parser))
            }
        }
        return list
    }

    private fun parseDevice(parser: XmlPullParser): DeviceDescription {
        parser.require(XmlPullParser.START_TAG, null, "device")
        val device = DeviceDescription.Builder()
        println(XmlPullParser.END_TAG)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            println("parse device ${parser.name}")
            when (parser.name) {
                "pnpx:X_deviceCategory" -> device.X_deviceCategory(parseElement(parser, "pnpx:X_deviceCategory"))
                "deviceType" -> device.deviceType(parseElement(parser, "deviceType"))
                "friendlyName" -> device.friendlyName(parseElement(parser, "friendlyName"))
                "manufacturer" -> device.manufacturer(parseElement(parser, "manufacturer"))
                "manufacturerURL" -> device.manufacturerURL(parseElement(parser, "manufacturerURL"))
                "modelName" -> device.modelName(parseElement(parser, "modelName"))
                "modelURL" -> device.modelURL(parseElement(parser, "modelURL"))
                "modelNumber" -> device.modelNumber(parseElement(parser, "modelNumber"))
                "serialNumber" -> device.serialNumber(parseElement(parser, "serialNumber"))
                "UDN" -> device.UDN(parseElement(parser, "UDN"))
                "UPC" -> device.UPC(parseElement(parser, "UPC"))
                "presentationURL" -> device.presentationURL(parseElement(parser, "presentationURL"))
                "device" -> device.device(parseDevice(parser))
                "deviceList" -> device.deviceList(parseDeviceList(parser))
                "service" -> device.service(parseService(parser))
                "serviceList" -> device.serviceList(parseServiceList(parser))
            }
        }
        return device.build()
    }
}
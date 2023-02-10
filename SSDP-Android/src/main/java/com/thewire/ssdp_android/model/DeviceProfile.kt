package com.thewire.ssdp_android.model

class DeviceProfile private constructor(
    val values: Map<String, String>?,
    val device: DeviceDescription?,
    val deviceList: List<DeviceDescription>?,
    val service: ServiceInformation?,
    val serviceList: List<ServiceInformation>?,
    val URLBase: String?,
    val specVersion: Pair<String, String>?
) {
    data class Builder(
        var values: Map<String, String>? = null,
        var device: DeviceDescription? = null,
        var deviceList: List<DeviceDescription>? = null,
        var service: ServiceInformation? = null,
        var serviceList: List<ServiceInformation>? = null,
        var URLBase: String? = null,
        var specVersion: Pair<String, String>? = null
    ) {
        fun values(values: Map<String, String>) = apply { this.values = values }
        fun device(device: DeviceDescription) = apply { this.device = device }
        fun deviceList(deviceList: List<DeviceDescription>) = apply { this.deviceList = deviceList }
        fun service(service: ServiceInformation) = apply { this.service = service }
        fun serviceList(serviceList: List<ServiceInformation>) = apply { this.serviceList = serviceList }
        fun URLBase(URLBase: String) = apply { this.URLBase = URLBase }
        fun specVersion(specVersion: Pair<String, String>) = apply { this.specVersion = specVersion }

        fun build() = DeviceProfile(
            values = values,
            device = device,
            deviceList = deviceList,
            service = service,
            serviceList = serviceList,
            URLBase,
            specVersion
        )
    }
}

package com.thewire.ssdp_android.model

class DeviceProfile private constructor(
    val values: Map<String, String>?,
    val device: SSDPDevice?,
    val URLBase: String?,
    val specVersion: Pair<String, String>?
) {
    data class Builder(
        var values: Map<String, String>? = null,
        var device: SSDPDevice? = null,
        var URLBase: String? = null,
        var specVersion: Pair<String, String>? = null
    ) {
        fun values(values: Map<String, String>) = apply { this.values = values }
        fun device(device: SSDPDevice) = apply { this.device = device }
        fun URLBase(URLBase: String) = apply { this.URLBase = URLBase }
        fun specVersion(specVersion: Pair<String, String>) = apply { this.specVersion = specVersion }
        fun build() = DeviceProfile(
            values,
            device,
            URLBase,
            specVersion
        )
    }
}

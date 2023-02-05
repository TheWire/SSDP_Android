package com.thewire.ssdp_android.model

class SSDPDevice private constructor(
    val X_deviceCategory: String?,
    val deviceType: String?,
    val friendlyName: String?,
    val manufacturer: String?,
    val modelName: String?,
    val modelURL: String?,
    val modelNumber: String?,
    val serialNumber: String?,
    val UDN: String?,
) {
    data class Builder(
        var X_deviceCategory: String? = null,
        var deviceType: String? = null,
        var friendlyName: String? = null,
        var manufacturer: String? = null,
        var modelName: String? = null,
        var modelURL: String? = null,
        var modelNumber: String? = null,
        var serialNumber: String? = null,
        var UDN: String? = null,
    ) {
        fun X_deviceCategory(X_deviceCategory: String) = apply {
            this.X_deviceCategory = X_deviceCategory
        }
        fun deviceType(deviceType: String) = apply {
            this.deviceType = deviceType
        }
        fun friendlyName(friendlyName: String) = apply {
            this.friendlyName = friendlyName
        }
        fun manufacturer(manufacturer: String) = apply {
            this.manufacturer = manufacturer
        }
        fun modelName(modelName: String) = apply {
            this.modelName = modelName
        }
        fun modelURL(modelURL: String) = apply {
            this.modelURL = modelURL
        }
        fun modelNumber(modelNumber: String) = apply {
            this.modelNumber = modelNumber
        }
        fun serialNumber(serialNumber: String) = apply {
            this.serialNumber = serialNumber
        }
        fun UDN(UDN: String) = apply {
            this.UDN = UDN
        }
        fun build() = SSDPDevice(
            X_deviceCategory,
            deviceType,
            friendlyName,
            manufacturer,
            modelName,
            modelURL,
            modelNumber,
            serialNumber,
            UDN,
        )
    }
}

package com.thewire.ssdp_android.model

class DeviceDescription private constructor(
    val X_deviceCategory: String?,
    val deviceType: String?,
    val friendlyName: String?,
    val manufacturer: String?,
    val manufacturerURL: String?,
    val modelName: String?,
    val modelNumber: String?,
    val modelURL: String?,
    val serialNumber: String?,
    val UDN: String?,
    val UPC: String?,
    val presentationURL: String?,
    val service: ServiceInformation? = null,
    val serviceList: List<ServiceInformation>? = null,
    val device: DeviceDescription? = null,
    val deviceList: List<DeviceDescription>? = null
) {
    data class Builder(
        var X_deviceCategory: String? = null,
        var deviceType: String? = null,
        var friendlyName: String? = null,
        var manufacturer: String? = null,
        var manufacturerURL: String? = null,
        var modelName: String? = null,
        var modelNumber: String? = null,
        var modelURL: String? = null,
        var serialNumber: String? = null,
        var UDN: String? = null,
        var UPC: String? = null,
        var presentationURL: String? = null,
        var service: ServiceInformation? = null,
        var serviceList: List<ServiceInformation>? = null,
        var device: DeviceDescription? = null,
        var deviceList: List<DeviceDescription>? = null
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
        fun manufacturerURL(manufacturerURL: String) = apply {
            this.manufacturerURL = manufacturerURL
        }
        fun modelName(modelName: String) = apply {
            this.modelName = modelName
        }
        fun modelNumber(modelNumber: String) = apply {
            this.modelNumber = modelNumber
        }
        fun modelURL(modelURL: String) = apply {
            this.modelURL = modelURL
        }
        fun serialNumber(serialNumber: String) = apply {
            this.serialNumber = serialNumber
        }
        fun UDN(UDN: String) = apply {
            this.UDN = UDN
        }
        fun UPC(UPC: String) = apply {
            this.UPC = UPC
        }
        fun presentationURL(presentationURL: String) = apply {
            this.presentationURL = presentationURL
        }
        fun service(service: ServiceInformation) = apply {
            this.service = service
        }
        fun serviceList(serviceList: List<ServiceInformation>) = apply {
            this.serviceList = serviceList
        }
        fun device(device: DeviceDescription) = apply {
            this.device = device
        }
        fun deviceList(deviceList: List<DeviceDescription>) = apply {
            this.deviceList = deviceList
        }
        fun build() = DeviceDescription(
            X_deviceCategory = X_deviceCategory,
            deviceType = deviceType,
            friendlyName = friendlyName,
            manufacturer = manufacturer,
            manufacturerURL = manufacturerURL,
            modelName = modelName,
            modelNumber = modelNumber,
            modelURL = modelURL,
            serialNumber = serialNumber,
            UDN = UDN,
            UPC = UPC,
            presentationURL = presentationURL,
            service = service,
            serviceList = serviceList,
            device = device,
            deviceList = deviceList
        )
    }
}

package com.thewire.ssdp_android.model

class ServiceInformation private constructor(
    val serviceType: String?,
    val serviceId: String?,
    val SCPDURL: String?,
    val controlURL: String?,
    val eventSubURL: String?,
) {
    data class Builder(
        var serviceType: String? = null,
        var serviceId: String? = null,
        var SCPDURL: String? = null,
        var controlURL: String? = null,
        var eventSubURL: String? = null,
    ) {
        fun serviceType(serviceType: String) = apply {
            this.serviceType = serviceType
        }
        fun serviceId(serviceId: String) = apply {
            this.serviceId = serviceId
        }
        fun SCPDURL(SCPDURL: String) = apply {
            this.SCPDURL = SCPDURL
        }
        fun controlURL(controlURL: String) = apply {
            this.controlURL = controlURL
        }
        fun eventSubURL(eventSubURL: String) = apply {
            this.eventSubURL = eventSubURL
        }
        fun build() = ServiceInformation(
            serviceType = serviceType,
            serviceId = serviceId,
            SCPDURL = SCPDURL,
            controlURL = controlURL,
            eventSubURL = eventSubURL,
        )
    }
}

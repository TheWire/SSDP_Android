package com.thewire.ssdp_android.model

sealed class DiscoverMessage {
    data class Message(val service: SSDPService): DiscoverMessage()
    data class Error(val message: String): DiscoverMessage()
    object Discovering: DiscoverMessage()
}

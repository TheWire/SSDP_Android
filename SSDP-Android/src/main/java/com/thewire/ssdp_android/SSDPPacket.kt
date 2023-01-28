package com.thewire.ssdp_android

internal sealed class SSDPPacket {
    object start: SSDPPacket()
    data class packet(val responseString: String): SSDPPacket()
}

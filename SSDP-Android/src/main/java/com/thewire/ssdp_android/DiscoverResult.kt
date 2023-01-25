package com.thewire.ssdp_android

sealed class DiscoverResult {
    data class Result(val service: SSDPService): DiscoverResult()
    data class Error(val message: String): DiscoverResult()
    object Discovering: DiscoverResult()
}

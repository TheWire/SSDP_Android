package com.thewire.ssdp_android

import java.net.InetAddress
import java.util.*

data class SSDPService(
    val address: InetAddress,
    val responseString: String,
    val service: Map<String, String>,
    val date: String? = null,
    val host: String? = null,
    val st: String? = null,
    val man: String? = null,
    val mx: String? = null,
    val cache: String? = null,
    val location: String? = null,
    val opt: String? = null,
    val nls_01: String? = null,
    val nt: String? = null,
    val nts: String? = null,
    val server: String? = null,
    val x_user_agent: String? = null,
    val usn: String? = null,
    val ext: String? = null,
    val bootId: String? = null,
    val configId: String? = null,
) {
    override fun hashCode(): Int {
        return Objects.hash(
            address, host, st, man, mx, cache, opt, nls_01, nt, nts,
            server, x_user_agent, usn, ext, bootId, configId
        )
    }
}

package com.thewire.ssdp_android

import java.net.InetAddress
import java.util.*

class SSDPService(
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

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(other == null) return false
        if(this::class.java != other::class.java) return false
        val otherSSDPService = other as SSDPService
        if(address != otherSSDPService.address) return false
        if(host != otherSSDPService.host) return false
        if(st != otherSSDPService.st) return false
        if(man != otherSSDPService.man) return false
        if(mx != otherSSDPService.mx) return false
        if(cache != otherSSDPService.cache) return false
        if(opt != otherSSDPService.opt) return false
        if(nls_01 != otherSSDPService.nls_01) return false
        if(nt != otherSSDPService.nt) return false
        if(nts != otherSSDPService.nts) return false
        if(server != otherSSDPService.server) return false
        if(x_user_agent != otherSSDPService.x_user_agent) return false
        if(usn != otherSSDPService.usn) return false
        if(ext != otherSSDPService.ext) return false
        if(bootId != otherSSDPService.bootId) return false
        if(configId != otherSSDPService.configId) return false
        return true
    }
}

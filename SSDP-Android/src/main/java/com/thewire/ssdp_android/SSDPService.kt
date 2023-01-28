package com.thewire.ssdp_android

data class SSDPService(
    val responseString: String,
    val service: Map<String, String>,
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
    )

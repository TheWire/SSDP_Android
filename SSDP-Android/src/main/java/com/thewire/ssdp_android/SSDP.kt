package com.thewire.ssdp_android

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.DatagramSocket

class SSDP {

    private val SSDP_PORT = 1900
    private val SSDP_ADDRESS = "239.255.255.250"
    private val LOCK_TAG = "SSDP-ANDROID"

    private fun discoverSetup(context: Context) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.createMulticastLock(LOCK_TAG)
        val socket = DatagramSocket()
    }
    fun discover(context: Context, timeout: Int = 20): Flow<DiscoverResult> = flow {
        try {

        }
    }
}
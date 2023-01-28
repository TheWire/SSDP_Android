package com.thewire.ssdp_android

import android.content.Context
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.MulticastLock
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

class SSDP(context: Context) {

    private val SSDP_PORT = 1900
    private val SSDP_ADDRESS = "239.255.255.250"
    private val LOCK_TAG = "SSDP-ANDROID"
    private var cache = mutableMapOf<Int, SSDPService>()
    private var lock: MulticastLock? = null
    private val socket = DatagramSocket()

    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

//    private cache

    fun discover(duration: Int = 5): Flow<DiscoverMessage> = flow {

        val address = InetAddress.getByName(SSDP_ADDRESS)

        cache.clear()

        var currentTime = System.currentTimeMillis()
        val endTime = if (duration == 0) {
            Long.MAX_VALUE
        } else {
            currentTime + (duration * 1000)
        }
        socket.soTimeout = 1000
        emit(DiscoverMessage.Discovering)
        while (currentTime < endTime) {
            val buffer = ByteArray(2048)
            val receivePacket = DatagramPacket(buffer, buffer.size, address, SSDP_PORT)
            try {
                socket.receive(receivePacket)
                val responseString = String(receivePacket.data, 0, receivePacket.length)
                Log.d("SSDP", responseString)
                parseServiceResponse(responseString)?.let { service ->
                    if (cache[service.hashCode()] == null) {
                        cache[service.hashCode()] = service
                        emit(DiscoverMessage.Message(service))
                    }
                }

            } catch (e: SocketTimeoutException) {
                Log.d("SSDP", "timeout")
            } //if timeout do nothing

            currentTime = System.currentTimeMillis()
        }


    }.catch { e ->
        emit(DiscoverMessage.Error(e.message ?: "unknown ssdp discover error"))
    }

    fun lockMulticast() {
        if(lock == null) {
            lock = wifiManager.createMulticastLock(LOCK_TAG)
        }
        lock!!.acquire()
    }

    fun releaseMulticast() {
        lock?.release()
    }

    suspend fun probe(service: String = "ssdp:all", probeInterval: Int = 5) {
        val address = InetAddress.getByName(SSDP_ADDRESS)
        val newLine = "\r\n"
        val discoverString = """
                M-SEARCH * HTTP/1.1
                HOST: $SSDP_ADDRESS:$SSDP_PORT
                MAN: "ssdp:discover"
                MX: 1
                ST: $service$newLine
            """.trimIndent().toByteArray()

        val packet = DatagramPacket(discoverString, discoverString.size, address, SSDP_PORT)
        while (true) {
            socket.send(packet)
            delay(probeInterval.toLong() * 1000)
        }
    }

    private fun parseServiceResponse(responseString: String): SSDPService? {
        val service = mutableMapOf<String, String>()
        responseString.split("\r\n").forEach { line ->
            val kv = line.split(":", limit = 2)
            if (kv.size < 2) return@forEach
            service[kv[0].uppercase()] = kv[1]
        }
        return SSDPService(
            responseString = responseString,
            service = service,
            host = service["HOST"],
            st = service["ST"],
            man = service["MAN"],
            mx = service["MX"],
            cache = service["CACHE"],
            location = service["LOCATION"],
            opt = service["OPT"],
            nls_01 = service["01-NLS"],
            nt = service["NT"],
            nts = service["NTS"],
            server = service["SERVER"],
            x_user_agent = service["X-USER-AGENT"],
            usn = service["USN"],
        )
    }


}
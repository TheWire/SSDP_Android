package com.thewire.ssdp_android

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

class SSDP {

    private val SSDP_PORT = 1900
    private val SSDP_ADDRESS = "239.255.255.250"
    private val LOCK_TAG = "SSDP-ANDROID"

    fun discover(context: Context, service: String = "ssdp:all", timeout: Int = 5): Flow<DiscoverResult> = flow<DiscoverResult> {
        val newLine = "\r\n"
        val discoverString = """
                M-SEARCH * HTTP/1.1
                HOST: $SSDP_ADDRESS:$SSDP_PORT
                MAN: "ssdp:discover"
                MX: 1
                ST: $service$newLine
            """.trimIndent().toByteArray()
            val address = InetAddress.getByName(SSDP_ADDRESS)
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager.createMulticastLock(LOCK_TAG)
            val socket = DatagramSocket()

            val packet = DatagramPacket(discoverString, discoverString.size, address, SSDP_PORT)
            socket.send(packet)
            emit(DiscoverResult.Discovering)

            var currentTime = System.currentTimeMillis()
            val endTime = if(timeout == 0) {
                Long.MAX_VALUE
            } else {
                currentTime + (timeout * 1000)
            }
            socket.soTimeout = 1
            while(currentTime < endTime) {
                val buffer = ByteArray(2048)
                val receivePacket = DatagramPacket(buffer,buffer.size, address, SSDP_PORT)
                try {
                    socket.receive(receivePacket)
                    val data = String(receivePacket.data, 0 , receivePacket.length)
                    emit(DiscoverResult.Result(SSDPService(data)))
                } catch(e: SocketTimeoutException){} //if timeout do nothing

                currentTime = System.currentTimeMillis()
            }

        }.catch { e ->
            emit(DiscoverResult.Error(e.message ?: "unknown ssdp discover error"))
        }
}
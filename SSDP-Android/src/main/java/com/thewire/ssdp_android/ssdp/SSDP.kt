package com.thewire.ssdp_android.ssdp

import android.content.Context
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.MulticastLock
import com.thewire.ssdp_android.model.SSDPService
import com.thewire.ssdp_android.model.DiscoverMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.net.*

class SSDP(context: Context) {

    private val SSDP_PORT = 1900
    private val SSDP_ADDRESS = "239.255.255.250"
    private val LOCK_TAG = "SSDP-ANDROID"
    private var cache = HashSet<SSDPService>()
    private var lock: MulticastLock? = null

    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

//    private cache

    fun discover(
        probe: Boolean,
        service: String = "ssdp:all",
        duration: Int = 5
    ): Flow<DiscoverMessage> = flow {
        val address = InetAddress.getByName(SSDP_ADDRESS)
        val socket = MulticastSocket(InetSocketAddress("0.0.0.0", SSDP_PORT))
        socket.joinGroup(address)
        if (probe) {
            singleProbe(service)
        }

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
                parseServiceResponse(receivePacket)?.let { service ->
                    if (cache.add(service)) {
                        emit(DiscoverMessage.Message(service))
                    }
                }

            } catch (e: SocketTimeoutException) {
            } //if timeout do nothing

            currentTime = System.currentTimeMillis()
        }
        socket.close()
    }.flowOn(Dispatchers.IO)
        .catch { e ->
            emit(DiscoverMessage.Error(e.message ?: "unknown ssdp discover error"))
        }

    fun lockMulticast() {
        if (lock == null) {
            lock = wifiManager.createMulticastLock(LOCK_TAG)
        }
        lock!!.acquire()
    }

    fun releaseMulticast() {
        lock?.release()
    }

    suspend fun probe(service: String = "ssdp:all", probeInterval: Int = 5) {
        withContext(Dispatchers.IO) {
            while (true) {
                singleProbe(service)
                delay(probeInterval.toLong() * 1000)
            }
        }
    }

    private fun singleProbe(service: String) {
        val newLine = "\r\n"
        val discoverString = """
                M-SEARCH * HTTP/1.1
                HOST: $SSDP_ADDRESS:$SSDP_PORT
                MAN: "ssdp:discover"
                MX:1
                ST: $service$newLine$newLine
            """.trimIndent().replace("\n", "\r\n").toByteArray()
//        Log.d("SSDP", discoverString.decodeToString())
        val address = InetAddress.getByName(SSDP_ADDRESS)
        val socket = MulticastSocket()
        socket.joinGroup(address)
        val packet = DatagramPacket(discoverString, discoverString.size, address, SSDP_PORT)
        socket.send(packet)
        socket.close()
    }

    private fun parseServiceResponse(packet: DatagramPacket): SSDPService? {
        val responseString = String(packet.data, 0, packet.length)
        if (!Regex("""^(NOTIFY \* HTTP)/(1\.0|1\.1|2\.0)\r\n""", RegexOption.IGNORE_CASE)
                .containsMatchIn(responseString)
        ) {
            return null
        }
        val service = mutableMapOf<String, String>()
        responseString.split("\r\n").forEach { line ->
            val kv = line.split(":", limit = 2)
            if (kv.size < 2) return@forEach
            service[kv[0].uppercase()] = kv[1]
        }
        return SSDPService(
            address = packet.address,
            responseString = responseString,
            service = service,
            date = service["DATE"],
            host = service["HOST"],
            st = service["ST"],
            man = service["MAN"],
            mx = service["MX"],
            cache = service["CACHE-CONTROL"],
            location = service["LOCATION"],
            opt = service["OPT"],
            nls_01 = service["01-NLS"],
            nt = service["NT"],
            nts = service["NTS"],
            server = service["SERVER"],
            x_user_agent = service["X-USER-AGENT"],
            usn = service["USN"],
            ext = service["EXT"],
            bootId = service["BOOTID.UPNP.ORG"],
            configId = service["CONFIGID.UPNP.ORG"],
        )
    }


}
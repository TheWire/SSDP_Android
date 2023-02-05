package com.thewire.ssdp_android.network

import com.thewire.ssdp_android.model.DeviceProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL


class SSDPRepository() {

    private val parser = DeviceProfileXmlParser()
    suspend fun getDeviceProfile(location: String): DeviceProfile {
        return withContext(Dispatchers.IO) {
            val url = URL(location)
            val urlConnection = url.openConnection() as HttpURLConnection
            try {
                val stream = BufferedInputStream(urlConnection.inputStream)
                return@withContext parser.parseStream(stream)
            } finally {
                urlConnection.disconnect()
            }
        }
    }
}


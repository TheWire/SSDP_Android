package com.thewire.ssdp_android.network

import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

fun getDeviceProfile(location: String) {
    val url = URL(location)
    val urlConnection = url.openConnection() as HttpURLConnection
    try {
        val stream = BufferedInputStream(urlConnection.inputStream)
        parseProfile(stream);
    } finally {
        urlConnection.disconnect();
    }
}

fun parseProfile(stream: InputStream) {

}


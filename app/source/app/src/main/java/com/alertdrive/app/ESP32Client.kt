
package com.alertdrive.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import java.net.URL
import java.net.HttpURLConnection
import java.net.Proxy

object ESP32Client {

    private const val BASE_URL =
        "http://192.168.4.1"

    @Volatile
    private lateinit var connectivityManager:
            ConnectivityManager

    fun init(context: Context) {

        connectivityManager =
            context.applicationContext.getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager
    }

    @Suppress("DEPRECATION")
    private fun connect(
        path: String,
        connectTimeoutMs: Int = 3000,
        readTimeoutMs: Int = 5000
    ): HttpURLConnection {

        check(::connectivityManager.isInitialized) {
            "Network manager not initialized"
        }

        val network = connectivityManager.allNetworks
            .firstOrNull { network ->

                val capabilities =
                    connectivityManager
                        .getNetworkCapabilities(network)

                val properties =
                    connectivityManager
                        .getLinkProperties(network)

                val isWiFi =
                    capabilities?.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI
                    ) == true

                val matchesAlertDriveSubnet =
                    properties?.linkAddresses?.any {
                        it.address.hostAddress
                            ?.startsWith("192.168.4.") == true
                    } == true

                isWiFi && matchesAlertDriveSubnet

            } ?: throw IllegalStateException(
            "AlertDrive WiFi network unavailable"
        )

        val connection = network.openConnection(
            URL("$BASE_URL$path"),
            Proxy.NO_PROXY
        ) as HttpURLConnection

        connection.connectTimeout = connectTimeoutMs
        connection.readTimeout = readTimeoutMs
        connection.useCaches = false
        connection.instanceFollowRedirects = false

        connection.setRequestProperty(
            "Connection", "close"
        )

        return connection
    }

    // CHECK DEVICE CONNECTION

    fun checkHealth(): Boolean {

        return try {

            val connection = connect(
                "/health",
                1200,
                1500
            )

            try {
                connection.responseCode == 200
            } finally {
                connection.disconnect()
            }

        } catch (e: Exception) {

            Log.d(
                "AlertDrive",
                "Health check failed: ${e.message}"
            )

            false
        }
    }

    // RECEIVE CAMERA FRAME

    fun getFrame(): Bitmap? {

        val connection = connect("/frame")

        return try {

            val status = connection.responseCode

            Log.d(
                "AlertDrive",
                "Frame HTTP=$status"
            )

            check(status == 200) {
                "Camera HTTP error: $status"
            }

            connection.inputStream.use {
                BitmapFactory.decodeStream(it)
            }

        } finally {
            connection.disconnect()
        }
    }

    // SEND DETECTION STATE

    fun sendAlert(state: String) {

        require(
            state in setOf(
                "NORMAL",
                "CLOSING",
                "WARNING",
                "DROWSY",
                "NO_FACE"
            )
        ) {
            "Invalid alert state"
        }

        val connection =
            connect("/alert?state=$state")

        try {

            val status = connection.responseCode

            Log.d(
                "AlertDrive",
                "Alert=$state HTTP=$status"
            )

            check(status == 200) {
                "Alert HTTP error: $status"
            }

            connection.inputStream.use {
                it.readBytes()
            }

        } finally {
            connection.disconnect()
        }
    }
}

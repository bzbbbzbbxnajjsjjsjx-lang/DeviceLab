package com.example.devicelab.platform.system

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import com.example.devicelab.domain.model.NetworkSnapshot
import com.example.devicelab.domain.model.NetworkTransport
import com.example.devicelab.domain.repository.NetworkDataSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidNetworkDataSource(
    private val context: Context
) : NetworkDataSource {

    private val connectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    }

    override fun getNetworkSnapshot(): NetworkSnapshot {
        val manager = connectivityManager ?: return offlineSnapshot()
        val activeNetwork = manager.activeNetwork ?: return offlineSnapshot()
        val capabilities = manager.getNetworkCapabilities(activeNetwork) ?: return offlineSnapshot()

        return mapCapabilitiesToSnapshot(capabilities)
    }

    override fun observeNetworkSnapshot(): Flow<NetworkSnapshot> = callbackFlow {
        val manager = connectivityManager
        if (manager == null) {
            trySend(offlineSnapshot())
            close()
            return@callbackFlow
        }

        trySend(getNetworkSnapshot())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(getNetworkSnapshot())
            }

            override fun onLost(network: Network) {
                trySend(getNetworkSnapshot())
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(mapCapabilitiesToSnapshot(capabilities))
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            manager.registerDefaultNetworkCallback(callback)
        }

        awaitClose {
            try {
                manager.unregisterNetworkCallback(callback)
            } catch (_: Exception) {}
        }
    }

    private fun mapCapabilitiesToSnapshot(capabilities: NetworkCapabilities): NetworkSnapshot {
        val transport = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkTransport.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkTransport.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkTransport.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetworkTransport.VPN
            else -> NetworkTransport.UNKNOWN
        }

        val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val isMetered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        val downBandwidth = capabilities.linkDownstreamBandwidthKbps.takeIf { it > 0 }
        val upBandwidth = capabilities.linkUpstreamBandwidthKbps.takeIf { it > 0 }

        return NetworkSnapshot(
            transport = transport,
            isConnected = true,
            isValidated = isValidated,
            isMetered = isMetered,
            downlinkBandwidthKbps = downBandwidth,
            uplinkBandwidthKbps = upBandwidth
        )
    }

    private fun offlineSnapshot(): NetworkSnapshot = NetworkSnapshot(
        transport = NetworkTransport.NONE,
        isConnected = false,
        isValidated = false,
        isMetered = false,
        downlinkBandwidthKbps = null,
        uplinkBandwidthKbps = null
    )
}

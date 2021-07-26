package com.laupdev.yocabulary.network

import android.content.ContentValues.TAG
import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.laupdev.yocabulary.ui.MainActivity

class CheckNetwork(val context: Context) {

    @RequiresApi(Build.VERSION_CODES.N)
    fun registerNetworkCallback() {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network : Network) {
                    Log.e(TAG, "The default network is now: " + network)
//                    MainActivity.isConnected = true
                }

                override fun onLost(network : Network) {
                    Log.e(TAG, "The application no longer has a default network. The last default network was " + network)
//                    MainActivity.isConnected = false
                }

                override fun onCapabilitiesChanged(network : Network, networkCapabilities : NetworkCapabilities) {
                    Log.e(TAG, "The default network changed capabilities: " + networkCapabilities)
//                    MainActivity.isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                }

                override fun onLinkPropertiesChanged(network : Network, linkProperties : LinkProperties) {
                    Log.e(TAG, "The default network changed link properties: " + linkProperties)
                }
            })
        } catch (error: Exception) {
//            MainActivity.isConnected = false
        }
    }
}

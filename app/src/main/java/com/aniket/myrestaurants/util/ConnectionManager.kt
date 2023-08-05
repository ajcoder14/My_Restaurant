package com.aniket.myrestaurants.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo

class ConnectionManager {
    fun checkConnectivity(context:Context): Boolean {

        // Here we get connected to connectivity manager to check status
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork : NetworkInfo? = connectivityManager.activeNetworkInfo

        // we check network value is true or false
        if(activeNetwork?.isConnected != null){
            return activeNetwork.isConnected
        }else {
            return false
        }
    }
}
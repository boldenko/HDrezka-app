package com.falcofemoralis.hdrezkaapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.system.exitProcess

object ConnectionManager {
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        var connection = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }

        if (!connection) {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
            connection = wm?.isWifiEnabled ?: false
        }

        return connection
    }

    fun showConnectionErrorDialog(context: Context, type: IConnection.ErrorType, retryCallback: () -> Unit) {
        if (type == IConnection.ErrorType.NO_INTERNET) {
            val dialog = MaterialAlertDialogBuilder(context)
            dialog.setTitle(context.getString(R.string.no_connection))
            dialog.setPositiveButton(context.getString(R.string.exit)) { dialog, id ->
                exitProcess(0)
            }
            dialog.setNegativeButton(context.getString(R.string.retry)) { dialog, id ->
                retryCallback
            }
            val d = dialog.create()
            d.show()
        }
    }
}
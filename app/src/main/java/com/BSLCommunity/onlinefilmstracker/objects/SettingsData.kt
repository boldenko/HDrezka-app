package com.BSLCommunity.onlinefilmstracker.objects

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import com.BSLCommunity.onlinefilmstracker.R

object SettingsData {
    private var prefs: SharedPreferences? = null
    private var provider: String? = null
    private var mainScreen: Int? = null

    fun init(context: Context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context)

        provider = prefs?.getString("providers", context.resources.getStringArray(R.array.providers)[0])
        mainScreen = prefs?.getString("screens", "0")?.toInt()
    }

    fun setProvider(context: Context) {
        Log.d("SETT_TEST", "changed prov $provider")
        provider = prefs?.getString("providers", context.resources.getStringArray(R.array.providers)[0])
        Log.d("SETT_TEST", "changed prov $provider")
    }
}
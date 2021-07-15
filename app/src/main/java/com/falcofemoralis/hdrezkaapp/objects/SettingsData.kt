package com.falcofemoralis.hdrezkaapp.objects

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.falcofemoralis.hdrezkaapp.R

object SettingsData {
    private var prefs: SharedPreferences? = null
    var provider: String? = null
    val staticProvider: String = "https://static.hdrezka.ac"
    var mainScreen: Int? = null

    fun init(context: Context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context)

        provider = prefs?.getString("providers", context.resources.getStringArray(R.array.providersIds)[0])
        mainScreen = prefs?.getString("screens", "0")?.toInt()
    }

    fun setProvider(context: Context) {
      //  val providerTmp = provider
        provider = prefs?.getString("providers", context.resources.getStringArray(R.array.providersIds)[0])
     /*   val cm: CookieManager = CookieManager.getInstance()
        val test = cm.getCookie(providerTmp)
        cm.setCookie(provider, cm.getCookie(providerTmp))*/
    }
}
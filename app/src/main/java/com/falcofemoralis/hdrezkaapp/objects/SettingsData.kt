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
    var isPlayer: Boolean? = null
    var isMaxQuality: Boolean? = null
    var isPlayerChooser: Boolean? = null

    fun init(context: Context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context)

        provider = prefs?.getString("ownProvider", "")
        if (provider == null || provider!!.isEmpty()) {
            provider = prefs?.getString("providers", context.resources.getStringArray(R.array.providersIds)[0])
            mainScreen = prefs?.getString("screens", "0")?.toInt()
        }

        isPlayer = prefs?.getBoolean("isPlayer", false)
        isMaxQuality = prefs?.getBoolean("isMaxQuality", false)
        isPlayerChooser = prefs?.getBoolean("isPlayerChooser", false)
    }

    /*  fun setProviderFromSettings(provider: String) {
        //  val providerTmp = provider
          this.provider = provider
       *//*   val cm: CookieManager = CookieManager.getInstance()
        val test = cm.getCookie(providerTmp)
        cm.setCookie(provider, cm.getCookie(providerTmp))*//*
    }*/
}
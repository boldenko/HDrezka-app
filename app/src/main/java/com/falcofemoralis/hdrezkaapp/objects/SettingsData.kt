package com.falcofemoralis.hdrezkaapp.objects

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.falcofemoralis.hdrezkaapp.constants.DeviceType
import com.falcofemoralis.hdrezkaapp.constants.GridLayoutSizes
import com.falcofemoralis.hdrezkaapp.utils.FileManager

object SettingsData {
    var provider: String? = null
    var mainScreen: Int? = null
    var isPlayer: Boolean? = null
    var isMaxQuality: Boolean? = null
    var isPlayerChooser: Boolean? = null
    var isExternalDownload: Boolean? = null
    var deviceType: DeviceType? = null
    var filmsInRow: Int? = null
    var isAutorotate: Boolean? = null
    var rowMultiplier: Int? = null
    var autoPlayNextEpisode: Boolean? = null
    const val PROVIDER_FILE = "provider"
    var defaultQuality: String? = null
    var isSubtitlesDownload: Boolean? = null

    fun initProvider(context: Context) {
        if (provider == null || provider == "") {
            try {
                provider = FileManager.readFile(PROVIDER_FILE, context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun init(context: Context, deviceType: DeviceType) {
        this.deviceType = deviceType

        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(context)

        mainScreen = prefs?.getString("screens", "0")?.toInt()
        isPlayer = prefs?.getBoolean("isPlayer", false)
        isMaxQuality = prefs?.getBoolean("isMaxQuality", false)
        isPlayerChooser = prefs?.getBoolean("isPlayerChooser", false)
        isExternalDownload = prefs?.getBoolean("isExternalDownload", false)
        isAutorotate = prefs?.getBoolean("isAutorotate", true)
        autoPlayNextEpisode = prefs?.getBoolean("autoPlayNextEpisode", true)
        isSubtitlesDownload = prefs?.getBoolean("isSubtitlesDownload", true)
        (prefs?.getString("filmsInRow", (if (deviceType == DeviceType.TV) GridLayoutSizes.TV else GridLayoutSizes.MOBILE).toString())).let {
            if (it != null) {
                filmsInRow = it.toInt()
            }
        }
        rowMultiplier = prefs?.getString("rowMultiplier", "3")?.toInt()
        var defq = prefs?.getString("defaultQuality", null)
        if (defq == "Авто") {
            defq = null
        }
        defaultQuality = defq
    }

    fun setProvider(newProvider: String, context: Context, updateSettings: Boolean) {
        if(updateSettings){
            val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(context)
            prefs?.edit()?.putString("ownProvider", newProvider)?.apply()
        }

        try {
            FileManager.writeFile(PROVIDER_FILE, newProvider, false, context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        provider = newProvider
    }
}
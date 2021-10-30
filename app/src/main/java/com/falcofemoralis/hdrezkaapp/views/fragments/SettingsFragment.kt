package com.falcofemoralis.hdrezkaapp.views.fragments

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.constants.DeviceType
import com.falcofemoralis.hdrezkaapp.constants.GridLayoutSizes
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import com.falcofemoralis.hdrezkaapp.objects.UserData
import com.falcofemoralis.hdrezkaapp.views.MainActivity

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var preferences: SharedPreferences
    private var mActivity: FragmentActivity? = null
    private var mContext: Context? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.registerOnSharedPreferenceChangeListener(this)
        mActivity = requireActivity()
        mContext = requireContext()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View? = super.onCreateView(inflater, container, savedInstanceState)
        view?.findViewById<View>(android.R.id.list)?.setBackgroundColor(Color.MAGENTA);
        view?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background))
        return view
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "ownProvider" -> {
                val newProvider: String? = preferences.getString("ownProvider", "")
                mContext?.let {
                    if (newProvider != null) {
                        SettingsData.setProvider(newProvider, it, false)
                    }
                }
                applyProvider()
            }
            "isPlayer" -> {
                SettingsData.isPlayer = preferences.getBoolean("isPlayer", false)
            }
            "isMaxQuality" -> {
                SettingsData.isMaxQuality = preferences.getBoolean("isMaxQuality", false)
            }
            "isPlayerChooser" -> {
                SettingsData.isPlayerChooser = preferences.getBoolean("isPlayerChooser", false)
            }
            "isExternalDownload" -> {
                SettingsData.isExternalDownload = preferences.getBoolean("isExternalDownload", false)
            }
            "filmsInRow" -> {
                (preferences.getString("filmsInRow", (if (SettingsData.deviceType == DeviceType.TV) GridLayoutSizes.TV else GridLayoutSizes.MOBILE).toString())).let {
                    if (it != null) {
                        SettingsData.filmsInRow = it.toInt()
                        applyInterfaceChange()
                    }
                }
            }
            "isAutorotate" -> {
                SettingsData.isAutorotate = preferences.getBoolean("isAutorotate", true)
                mActivity?.requestedOrientation = if (SettingsData.isAutorotate == true) {
                    ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
            "rowMultiplier" -> {
                SettingsData.rowMultiplier = preferences.getString("rowMultiplier", "3")?.toInt()
                applyInterfaceChange()
            }
            "autoPlayNextEpisode" -> {
                SettingsData.autoPlayNextEpisode = preferences.getBoolean("autoPlayNextEpisode", true)
            }
            "defaultQuality" -> {
                var defaultQuality: String? = preferences.getString("defaultQuality", null)
                if (defaultQuality == "Авто") {
                    defaultQuality = null
                }
                SettingsData.defaultQuality = defaultQuality
            }
        }
    }

    private fun applyProvider() {
        mContext?.let { UserData.reset(it) }
        mActivity?.let { (it as MainActivity).updatePager() }
        mActivity?.let { (it as MainActivity).setUserAvatar() }
    }

    private fun applyInterfaceChange() {
        mActivity?.let { (it as MainActivity).updatePager() }
    }
}
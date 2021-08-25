package com.falcofemoralis.hdrezkaapp.views.fragments

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
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
            "providers" -> {
                SettingsData.provider = preferences.getString("providers", mActivity?.resources?.getStringArray(R.array.providersIds)?.get(0))
                applyProvider()
            }
            "ownProvider" -> {
                SettingsData.provider = preferences.getString("ownProvider", "")
                if (SettingsData.provider.isNullOrEmpty()) {
                    SettingsData.provider = preferences.getString("providers", mActivity?.resources?.getStringArray(R.array.providersIds)?.get(0))
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
                SettingsData.isAutorotate = preferences.getBoolean("isAutorotate", false)
                mActivity?.requestedOrientation = if (SettingsData.isAutorotate == true) {
                    ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
        }
    }

    private fun applyProvider() {
        mContext?.let { UserData.reset(it) }
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        mActivity?.let { (it as MainActivity).updatePager() }
    }

    private fun applyInterfaceChange() {
        mActivity?.let { (it as MainActivity).updatePager() }
    }
}
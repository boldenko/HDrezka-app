package com.falcofemoralis.hdrezkaapp.views.fragments

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import com.falcofemoralis.hdrezkaapp.objects.UserData
import com.falcofemoralis.hdrezkaapp.views.MainActivity

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var preferences: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View? = super.onCreateView(inflater, container, savedInstanceState)
        view?.findViewById<View>(android.R.id.list)?.setBackgroundColor(Color.MAGENTA);
        return view
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        activity.let {
            when (key) {
                "providers" -> {
                    if (it != null) {
                        SettingsData.provider = PreferenceManager.getDefaultSharedPreferences(it).getString("providers", it.resources.getStringArray(R.array.providersIds)[0])
                        applyProvider(it)
                    }

                }
                "ownProvider" -> {
                    if (it != null) {
                        SettingsData.provider = PreferenceManager.getDefaultSharedPreferences(it).getString("ownProvider", "")
                        if (SettingsData.provider.isNullOrEmpty()) {
                            SettingsData.provider = PreferenceManager.getDefaultSharedPreferences(it).getString("providers", it.resources.getStringArray(R.array.providersIds)[0])
                        }
                        applyProvider(it)
                    }

                }
                "isPlayer" -> {
                    SettingsData.isPlayer = PreferenceManager.getDefaultSharedPreferences(it).getBoolean("isPlayer", false)
                }
                "isMaxQuality" -> {
                    SettingsData.isMaxQuality = PreferenceManager.getDefaultSharedPreferences(it).getBoolean("isMaxQuality", false)
                }
            }
        }
    }

    private fun applyProvider(it: FragmentActivity) {
        UserData.reset(it)
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        (it as MainActivity).updatePager()
    }
}
package com.falcofemoralis.hdrezkaapp.views.fragments

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
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
        when (key) {
            "providers" -> {
                requireActivity().let {
                    SettingsData.setProvider(it.applicationContext)
                    UserData.reset(it)
                    CookieManager.getInstance().removeAllCookies(null)
                    CookieManager.getInstance().flush()
                    (it as MainActivity).updatePager()
                }
            }
        }
    }
}
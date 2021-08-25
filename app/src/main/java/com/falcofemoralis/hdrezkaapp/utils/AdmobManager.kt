package com.falcofemoralis.hdrezkaapp.utils

import android.content.Context
import android.widget.LinearLayout
import com.falcofemoralis.hdrezkaapp.constants.DeviceType
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

object AdmobManager {
    private var DEBUG_MODE = true
    const val BANNER_TEST = "ca-app-pub-3940256099942544/6300978111"
    const val BANNER_FILM = "ca-app-pub-1493075645303607/4733655302"
    const val BANNER_ACTOR = "ca-app-pub-1493075645303607/2451106899"

    fun setAd(container: LinearLayout, context: Context, bannerId: String) {
        val adView = AdView(context)
        adView.adSize = if(SettingsData.deviceType == DeviceType.TV) AdSize.LEADERBOARD else AdSize.BANNER
        adView.adUnitId = if(!DEBUG_MODE) bannerId else BANNER_TEST
        container.addView(adView)

        MobileAds.initialize(context) {}
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
}
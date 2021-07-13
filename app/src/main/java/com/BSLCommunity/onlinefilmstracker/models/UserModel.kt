package com.BSLCommunity.onlinefilmstracker.models

import android.webkit.CookieManager
import com.BSLCommunity.onlinefilmstracker.objects.SettingsData
import com.BSLCommunity.onlinefilmstracker.utils.CookieStorage
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object UserModel {
    private const val USER_PAGE: String = "/user/"

    fun getUserAvatarLink(): String {
        val userId: String? = CookieStorage.getCookie(SettingsData.provider, "dle_user_id")
        val doc: Document = Jsoup.connect(SettingsData.provider + USER_PAGE + userId).header("Cookie", CookieManager.getInstance().getCookie(SettingsData.provider)).get()
        return SettingsData.provider + "/" + doc.select("div.b-userprofile__avatar_holder img").attr("src")
    }
}
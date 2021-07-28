package com.falcofemoralis.hdrezkaapp.models

import android.webkit.CookieManager
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import com.falcofemoralis.hdrezkaapp.utils.CookieStorage
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object UserModel {
    private const val USER_PAGE: String = "/user/"

    fun getUserAvatarLink(): String? {
        val userId: String? = CookieStorage.getCookie(SettingsData.provider, "dle_user_id")
        val doc: Document = Jsoup.connect(SettingsData.provider + USER_PAGE + userId).header("Cookie", CookieManager.getInstance().getCookie(SettingsData.provider)).get()
        val str = doc.select("div.b-userprofile__avatar_holder img").attr("src")
        return if(str != "https://static.hdrezka.ac/templates/hdrezka/images/noavatar.png"){
            SettingsData.provider + "/" + str
        } else{
            null
        }
    }
}
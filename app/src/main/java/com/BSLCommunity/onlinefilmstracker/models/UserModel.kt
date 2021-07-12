package com.BSLCommunity.onlinefilmstracker.models

import android.content.Context
import android.webkit.CookieManager
import com.BSLCommunity.onlinefilmstracker.utils.CookieStorage
import com.BSLCommunity.onlinefilmstracker.utils.FileManager
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object UserModel {
    private const val USER_PAGE: String = "http://hdrezka.tv/user/"
    private const val PROVIDER: String = "http://hdrezka.tv"

    fun getUserAvatarLink(): String {
        val userId: String? = CookieStorage.getCookie(BookmarksModel.MAIN_PAGE, "dle_user_id")
        val doc: Document = Jsoup.connect(USER_PAGE + userId).header("Cookie", CookieManager.getInstance().getCookie(BookmarksModel.MAIN_PAGE)).get()
        return PROVIDER + doc.select("div.b-userprofile__avatar_holder img").attr("src")
    }
}
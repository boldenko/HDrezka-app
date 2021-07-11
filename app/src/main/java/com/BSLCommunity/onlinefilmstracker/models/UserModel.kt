package com.BSLCommunity.onlinefilmstracker.models

import android.content.Context
import android.webkit.CookieManager
import com.BSLCommunity.onlinefilmstracker.utils.CookieStorage
import com.BSLCommunity.onlinefilmstracker.utils.FileManager
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object UserModel {
    private const val USER_FILE: String = "user"
    const val USER_AVATAR: String = "avatar"
    private const val USER_PAGE: String = "http://hdrezka.tv/user/"
    private const val PROVIDER: String = "http://hdrezka.tv"

    var isLoggedIn: Boolean? = null

    fun saveLoggedIn(state: Boolean, context: Context) {
        isLoggedIn = state
        FileManager.writeFile(USER_FILE, if (state) "1" else "0", false, context)
    }

    fun loadLoggedIn(context: Context) {
        if (isLoggedIn == null) {
            val data: String? = FileManager.readFile(USER_FILE, context)
            isLoggedIn = if (data != null) {
                data == "1"
            } else {
                false
            }
        }
    }

    fun getUserAvatarLink(): String {
        val userId: String? = CookieStorage.getCookie(BookmarksModel.MAIN_PAGE, "dle_user_id")
        val doc: Document = Jsoup.connect(USER_PAGE + userId).header("Cookie", CookieManager.getInstance().getCookie(BookmarksModel.MAIN_PAGE)).get()
        return PROVIDER + doc.select("div.b-userprofile__avatar_holder img").attr("src")
    }
}
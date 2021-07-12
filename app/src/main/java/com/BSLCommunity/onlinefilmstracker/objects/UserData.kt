package com.BSLCommunity.onlinefilmstracker.objects

import android.content.Context
import com.BSLCommunity.onlinefilmstracker.utils.FileManager

object UserData {
    private const val USER_FILE: String = "user"
    private const val USER_AVATAR: String = "avatar"

    var isLoggedIn: Boolean? = null
    var avatarLink: String? = null

    fun init(context: Context) {
        val data: String? = FileManager.readFile(USER_FILE, context)
        isLoggedIn = if (data != null) {
            data == "1"
        } else {
            false
        }

        avatarLink = FileManager.readFile(USER_AVATAR, context)
    }

    fun setLoggedIn(state: Boolean, context: Context) {
        isLoggedIn = state
        FileManager.writeFile(USER_FILE, if (state) "1" else "0", false, context)
    }

    fun setAvatar(avatarLink: String, context: Context) {
        FileManager.writeFile(USER_AVATAR, avatarLink, false, context)
    }
}
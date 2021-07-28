package com.falcofemoralis.hdrezkaapp.objects

import android.content.Context
import com.falcofemoralis.hdrezkaapp.utils.FileManager

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

    fun setLoggedIn(context: Context) {
        isLoggedIn = true
        FileManager.writeFile(USER_FILE, "1", false, context)
    }

    fun setAvatar(avatarLink: String?, context: Context) {
        if(avatarLink != null){
            FileManager.writeFile(USER_AVATAR, avatarLink, false, context)
        }
    }

    fun reset(context: Context) {
        isLoggedIn = false
        FileManager.writeFile(USER_FILE, "0", false, context)
        FileManager.writeFile(USER_AVATAR, "", false, context)
    }
}
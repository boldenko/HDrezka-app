package com.falcofemoralis.hdrezkaapp.objects

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.falcofemoralis.hdrezkaapp.utils.CookieStorage
import com.falcofemoralis.hdrezkaapp.utils.FileManager

object UserData {
    private const val USER_FILE: String = "user"
    private const val USER_AVATAR: String = "avatar"
    private const val USER_ID: String = "id"
    private const val USER_HASH: String = "hash"

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

        if (isLoggedIn == true) {
            try {
                val dle_user_id = CookieStorage.getCookie(SettingsData.provider, "dle_user_id")
                if (dle_user_id.isNullOrEmpty()) {
                    Log.d("COOOOKIES", "dle_user_id IS EMPTY")
                    // load from internal storage
                } else {
                    Log.d("COOOOKIES", "dle_user_id IS OK")
                }
            } catch (e: Exception) {
                Toast.makeText(context, "dle_user_id failed!!! + $e", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun setLoggedIn(context: Context) {
        isLoggedIn = true
        FileManager.writeFile(USER_FILE, "1", false, context)
    }

    fun setAvatar(avatarLink: String?, context: Context) {
        if (avatarLink != null) {
            FileManager.writeFile(USER_AVATAR, avatarLink, false, context)
        }
    }

    fun setUserData(context: Context) {

    }

    fun reset(context: Context) {
        isLoggedIn = false
        FileManager.writeFile(USER_FILE, "0", false, context)
        FileManager.writeFile(USER_AVATAR, "", false, context)
    }
}
package com.BSLCommunity.onlinefilmstracker.models

import android.content.Context
import com.BSLCommunity.onlinefilmstracker.managers.FileManager

object UserModel {
    private const val FILE_NAME: String = "user"
    var isLoggedIn: Boolean? = null

    fun saveLoggedIn(state: Boolean, context: Context) {
        isLoggedIn = state
        FileManager.writeFile(FILE_NAME, if (state) "1" else "0", false, context)
    }

    fun loadLoggedIn(context: Context) {
        if (isLoggedIn == null) {
            val data: String? = FileManager.readFile(FILE_NAME, context)
            data?.let {
                isLoggedIn = data == "1"
            }
        }
    }
}
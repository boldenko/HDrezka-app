package com.BSLCommunity.onlinefilmstracker.models

import android.content.Context
import com.BSLCommunity.onlinefilmstracker.managers.FileManager

object UserModel {
    private val fileName: String = "user"
    var isLoggedIn: Boolean? = null

    fun saveLoggedIn(state: Boolean, context: Context) {
        isLoggedIn = state
        FileManager.writeFile(fileName, if (state) "1" else "0", false, context)
    }

    fun loadLoggedIn(context: Context) {
        if (isLoggedIn == null) {
            val data: String = FileManager.readFile(fileName, context)
            isLoggedIn = data == "1"
        }
    }
}
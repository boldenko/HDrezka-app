package com.BSLCommunity.onlinefilmstracker.presenters

import android.content.Context
import android.webkit.CookieManager
import com.BSLCommunity.onlinefilmstracker.models.UserModel
import com.BSLCommunity.onlinefilmstracker.objects.SettingsData
import com.BSLCommunity.onlinefilmstracker.objects.UserData
import com.BSLCommunity.onlinefilmstracker.utils.ExceptionHelper.catchException
import com.BSLCommunity.onlinefilmstracker.views.viewsInterface.UserView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserPresenter(private val userView: UserView, private val context: Context) {
    enum class WindowType {
        LOGIN,
        REGISTRATION
    }

    fun setAuthWindow(type: WindowType) {
        SettingsData.provider?.let { userView.showAuthWindow(type, it) }
    }

    fun getUserAvatar() {
        GlobalScope.launch {
            try {
                val link: String = UserModel.getUserAvatarLink()
                UserData.setAvatar(link, context)

                withContext(Dispatchers.Main) {
                    userView.setUserAvatar(link)
                }
            } catch (e: Exception) {
                catchException(e, userView)
                return@launch
            }
        }
    }

    fun enter(){
        UserData.setLoggedIn(context)
    }

    fun exit(){
        UserData.reset(context)
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }
}
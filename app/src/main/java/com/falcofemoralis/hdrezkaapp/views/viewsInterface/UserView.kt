package com.falcofemoralis.hdrezkaapp.views.viewsInterface

import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.presenters.UserPresenter

interface UserView : IConnection {
    fun showAuthWindow(type: UserPresenter.WindowType, link: String)

    fun setUserAvatar()
}
package com.BSLCommunity.onlinefilmstracker.views.viewsInterface

import com.BSLCommunity.onlinefilmstracker.interfaces.IConnection
import com.BSLCommunity.onlinefilmstracker.presenters.UserPresenter

interface UserView : IConnection{
    fun showAuthWindow(type: UserPresenter.WindowType, link: String)

    fun setUserAvatar(link: String)
}
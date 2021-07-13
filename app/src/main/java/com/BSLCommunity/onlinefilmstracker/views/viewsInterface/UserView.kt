package com.BSLCommunity.onlinefilmstracker.views.viewsInterface

import com.BSLCommunity.onlinefilmstracker.presenters.UserPresenter

interface UserView {
    fun showAuthWindow(type: UserPresenter.WindowType, link: String)

    fun setUserAvatar(link: String)
}
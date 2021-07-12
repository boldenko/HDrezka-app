package com.BSLCommunity.onlinefilmstracker.views.viewsInterface

import com.BSLCommunity.onlinefilmstracker.interfaces.IMsg

interface BookmarksView {
    fun setBookmarksSpinner(bookmarksNames: ArrayList<String>)

    fun showMsg(type: IMsg.MsgType)

    fun setNoBookmarks()
}
package com.BSLCommunity.onlinefilmstracker.viewsInterface

import com.BSLCommunity.onlinefilmstracker.views.IMsg

interface BookmarksView {
    fun setBookmarksSpinner(bookmarksNames: ArrayList<String>)

    fun showMsg(type: IMsg.MsgType)

    fun setNoBookmarks()
}
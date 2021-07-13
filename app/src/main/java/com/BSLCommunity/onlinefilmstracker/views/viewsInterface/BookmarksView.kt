package com.BSLCommunity.onlinefilmstracker.views.viewsInterface

import com.BSLCommunity.onlinefilmstracker.interfaces.IConnection
import com.BSLCommunity.onlinefilmstracker.interfaces.IMsg

interface BookmarksView : IMsg, IConnection {
    fun setBookmarksSpinner(bookmarksNames: ArrayList<String>)

    fun setNoBookmarks()
}
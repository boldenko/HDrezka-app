package com.BSLCommunity.onlinefilmstracker.viewsInterface

import com.BSLCommunity.onlinefilmstracker.objects.Bookmark
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.presenters.BookmarksPresenter

interface BookmarksView {
    fun setBookmarksSpinner(bookmarksNames: ArrayList<String>)

    fun showMsg(type:BookmarksPresenter.MsgType)
}
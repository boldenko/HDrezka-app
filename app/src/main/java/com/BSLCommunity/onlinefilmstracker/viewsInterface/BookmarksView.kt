package com.BSLCommunity.onlinefilmstracker.viewsInterface

import com.BSLCommunity.onlinefilmstracker.objects.Bookmark
import com.BSLCommunity.onlinefilmstracker.objects.Film

interface BookmarksView {
    fun setBookmarksSpinner(bookmarksNames: ArrayList<String>)

    fun setFilms(films: ArrayList<Film>)

    fun redrawFilms()

    fun showMsg(msg:String)
}
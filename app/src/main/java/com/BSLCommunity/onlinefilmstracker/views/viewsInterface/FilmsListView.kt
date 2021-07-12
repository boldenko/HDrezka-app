package com.BSLCommunity.onlinefilmstracker.views.viewsInterface

import com.BSLCommunity.onlinefilmstracker.objects.Film

interface FilmsListView {
    fun setFilms(films: ArrayList<Film>)

    fun redrawFilms()

    fun setProgressBarState(state: Boolean)
}
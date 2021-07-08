package com.BSLCommunity.onlinefilmstracker.viewsInterface

import com.BSLCommunity.onlinefilmstracker.objects.Film

interface FilmsListView {
    fun setFilms(films: ArrayList<Film>)

    fun redrawFilms()

    fun setProgressBarState(state: Boolean)
}
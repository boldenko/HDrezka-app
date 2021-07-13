package com.BSLCommunity.onlinefilmstracker.views.viewsInterface

import com.BSLCommunity.onlinefilmstracker.interfaces.IProgressState
import com.BSLCommunity.onlinefilmstracker.objects.Film

interface FilmsListView: IProgressState {
    fun setFilms(films: ArrayList<Film>)

    fun redrawFilms()
}
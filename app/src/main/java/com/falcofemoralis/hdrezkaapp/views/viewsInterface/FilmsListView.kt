package com.falcofemoralis.hdrezkaapp.views.viewsInterface

import com.falcofemoralis.hdrezkaapp.interfaces.IProgressState
import com.falcofemoralis.hdrezkaapp.objects.Film

interface FilmsListView: IProgressState {
    fun setFilms(films: ArrayList<Film>)

    fun redrawFilms(from: Int, count: Int, isAdded: Boolean)
}
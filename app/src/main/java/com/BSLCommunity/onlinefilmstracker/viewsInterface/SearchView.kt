package com.BSLCommunity.onlinefilmstracker.viewsInterface

import com.BSLCommunity.onlinefilmstracker.objects.Film

interface SearchView {
    fun redrawSearchFilms(films: ArrayList<String>)

    fun openFilm(film: Film)
}
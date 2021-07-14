package com.BSLCommunity.onlinefilmstracker.views.viewsInterface

import com.BSLCommunity.onlinefilmstracker.interfaces.IConnection

interface SearchView : IConnection {
    fun redrawSearchFilms(films: ArrayList<String>)
}
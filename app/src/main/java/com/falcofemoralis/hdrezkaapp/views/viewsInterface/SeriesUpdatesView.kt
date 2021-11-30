package com.falcofemoralis.hdrezkaapp.views.viewsInterface

import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.objects.Film

interface SeriesUpdatesView: IConnection {
    fun setList(films: LinkedHashMap<String, ArrayList<Film>>)

}
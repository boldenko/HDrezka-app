package com.falcofemoralis.hdrezkaapp.models

import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SettingsData

object NewestFilmsModel {
    private const val NEWEST = "/new/page/"

    fun getNewestFilms(page: Int): ArrayList<Film> {
        return FilmsListModel.getFilmsFromPage(SettingsData.provider + NEWEST + page)
    }
}
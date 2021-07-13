package com.BSLCommunity.onlinefilmstracker.models

import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.objects.SettingsData

object NewestFilmsModel {
    private const val NEWEST = "/new/page/"

    fun getNewestFilms(page: Int): ArrayList<Film> {
        return FilmsListModel.getFilmsFromPage(SettingsData.provider + NEWEST + page)
    }
}
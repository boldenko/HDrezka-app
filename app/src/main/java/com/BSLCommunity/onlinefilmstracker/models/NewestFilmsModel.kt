package com.BSLCommunity.onlinefilmstracker.models

import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.objects.SettingsData
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import org.w3c.dom.Document

object NewestFilmsModel {
    private const val NEWEST = "/new/page/"

    fun getNewestFilms(page: Int): ArrayList<Film> {
        return FilmsListModel.getFilmsFromPage(SettingsData.provider + NEWEST + page)
    }
}
package com.falcofemoralis.hdrezkaapp.models

import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object NewestFilmsModel {
    private const val NEWEST = "/new/page/"

    fun getNewestFilms(page: Int, filter: String): ArrayList<Film> {
        val doc: Document = Jsoup.connect(SettingsData.provider + NEWEST + page + "/?filter=$filter").get()
        return FilmsListModel.getFilmsFromPage(doc)
    }
}
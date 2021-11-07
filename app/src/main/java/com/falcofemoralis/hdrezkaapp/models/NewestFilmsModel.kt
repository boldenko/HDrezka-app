package com.falcofemoralis.hdrezkaapp.models

import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import org.jsoup.nodes.Document

object NewestFilmsModel : BaseModel() {
    private const val NEWEST = "/new/page/"

    fun getNewestFilms(page: Int, filter: String): ArrayList<Film> {
        val s = SettingsData.provider + "$NEWEST$page/?filter=$filter"
        val doc: Document = getJsoup(SettingsData.provider + "$NEWEST$page/?filter=$filter").get()
        return FilmsListModel.getFilmsFromPage(doc)
    }
}
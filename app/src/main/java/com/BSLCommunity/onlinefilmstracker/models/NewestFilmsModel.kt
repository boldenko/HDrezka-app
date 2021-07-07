package com.BSLCommunity.onlinefilmstracker.models

import com.BSLCommunity.onlinefilmstracker.objects.Film
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object NewestFilmsModel {
    private const val HDREZKA_NEWEST = "http://hdrezka.tv/new/page/"
    const val FILMS = "div.b-content__inline_item"
    const val FILM_LINK = "div.b-content__inline_item-cover a"
    const val FILM_TYPE = "span.cat i"

    fun getFilmsFromPage(page: Int): ArrayList<Film> {
        val films: ArrayList<Film> = ArrayList()
        val doc: Document = Jsoup.connect(HDREZKA_NEWEST + page).get()

        for (element in doc.select(FILMS)) {
            films.add(Film(element.select(FILM_LINK).attr("href"), element.select(FILM_TYPE)[0].text()))
        }
        return films
    }
}
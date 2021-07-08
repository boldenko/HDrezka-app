package com.BSLCommunity.onlinefilmstracker.models

import com.BSLCommunity.onlinefilmstracker.objects.Film
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object FilmsListModel {
    const val FILMS = "div.b-content__inline_item"
    const val FILM_LINK = "div.b-content__inline_item-cover a"
    const val FILM_TYPE = "span.cat i"

    fun getFilmsFromPage(url: String): ArrayList<Film> {
        try {
            val films: ArrayList<Film> = ArrayList()
            val doc: Document = Jsoup.connect(url).get()

            for (element in doc.select(FILMS)) {
                films.add(Film(element.select(FILM_LINK).attr("href"), element.select(FILM_TYPE)[0].text()))
            }
            return films
        } catch (e: Exception) {
            throw e
        }
    }
}
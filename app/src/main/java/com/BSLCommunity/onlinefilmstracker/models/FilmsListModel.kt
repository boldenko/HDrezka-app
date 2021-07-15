package com.BSLCommunity.onlinefilmstracker.models

import com.BSLCommunity.onlinefilmstracker.objects.Film
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.lang.Exception

object FilmsListModel {
    const val FILMS = "div.b-content__inline_item"
    const val FILM_LINK = "div.b-content__inline_item-cover a"

    fun getFilmsFromPage(url: String): ArrayList<Film> {
        val films: ArrayList<Film> = ArrayList()
        val doc: Document = Jsoup.connect(url).get()

        for (element in doc.select(FILMS)) {
            films.add(Film(element.select(FILM_LINK).attr("href")))
        }

        if(films.size == 0){
            throw Exception("Empty list")
        }
        return films
    }
}
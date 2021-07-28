package com.falcofemoralis.hdrezkaapp.models

import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import org.jsoup.HttpStatusException
import org.jsoup.nodes.Document

object FilmsListModel {
    const val FILMS = "div.b-content__inline_item"
    const val FILM_IMG = "div.b-content__inline_item-cover a img"

    fun getFilmsFromPage(doc: Document): ArrayList<Film> {
        val films: ArrayList<Film> = ArrayList()

        for (el in doc.select(FILMS)) {
            val film = Film(el.attr("data-id"))
            film.link = el.attr("data-url")
            film.posterPath = el.select(FILM_IMG).attr("src")

            val text = el.select("div.b-content__inline_item-link div").text()
            val separated = text.split(",")
            film.year = separated[0]
            film.countries = ArrayList()
            film.countries!!.add(separated[1].drop(1))
            films.add(film)
        }

        if (films.size == 0) {
            throw HttpStatusException("Empty list", 404, SettingsData.provider)
        }
        return films
    }
}
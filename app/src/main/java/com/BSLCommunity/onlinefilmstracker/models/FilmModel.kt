package com.BSLCommunity.onlinefilmstracker.models

import android.util.Log
import com.BSLCommunity.onlinefilmstracker.objects.Film
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

object FilmModel {
    private const val FILM_TITLE = "div.b-post__title h1"
    private const val FILM_POSTER = "div.b-sidecover a"
    private const val FILM_TABLE_INFO = "table.b-post__info tbody tr"
    private const val FILM_IMDB_RATING = "span.imdb span"

    private fun getFilmPage(link: String): Document {
        return Jsoup.connect(link).get()
    }

    fun getMainData(film: Film): Film {
        val filmPage: Document = getFilmPage(film.link)
        val table: Elements = filmPage.select(FILM_TABLE_INFO)

        film.title = filmPage.select(FILM_TITLE).text()

        val posterElement: Element = filmPage.select(FILM_POSTER)[0]
        film.fullSizePosterPath = posterElement.attr("href")
        film.posterPath = posterElement.select("img").attr("src")

        // Parse info table
        for (tr in table) {
            val td: Elements = tr.select("td")
            if (td.size > 0) {
                val h2Els: Elements = td[0].select("h2")
                if (h2Els.size > 0) {
                    val h2: Element = h2Els[0]

                    when (h2.text()) {
                        "Рейтинги" -> {
                            film.ratingIMDB = td[1].select(FILM_IMDB_RATING).text()
                        }
                        "Дата выхода" -> {
                            film.date = td[1].ownText()
                            film.year = td[1].select("a").text()
                        }
                        "Страна" -> {
                            val countries: ArrayList<String> = ArrayList()
                            for (el in td[1].select("a")) {
                                countries.add(el.text())
                            }
                            film.countries = countries
                        }
                        "Жанр" -> {
                            val genres: ArrayList<String> = ArrayList()
                            for (el in td[1].select("a")) {
                                genres.add(el.select("span").text())
                            }
                            film.genres = genres
                        }
                    }
                }
            }
        }

        film.hasMainData = true

        return film
    }

    fun getAdditionalData(film: Film): Film {
        val document: Document = getFilmPage(film.link)
        film.origTitle = document.select("div.b-post__origtitle").text()
        film.description = document.select("div.b-post__description_text").text()
        film.votes = document.select("span.imdb i").text()
        film.runtime = document.select("td[itemprop=duration]").text()

        val actorsLinks: ArrayList<String> = ArrayList()
        val directors: ArrayList<String> = ArrayList()

        val personsElements: Elements = document.select("div.persons-list-holder")
        for (el in personsElements) {
            val els: Elements = el.select("span.item")

            if (el.select("span.inline h2").text() == "В ролях актеры") {
                for (actorElement in els) {
                    val actorLink: String = actorElement.select("span a").attr("href")
                    if (actorLink.isNotEmpty()) {
                        actorsLinks.add(actorLink)
                    }
                }
            } else {
                for (directorElement in els) {
                    directors.add(directorElement.select("span a span").text())
                }
            }
        }

        film.directors = directors
        film.actorsLinks = actorsLinks

        film.hasAdditionalData = true

        return film
    }


    fun getFilmsData(films: ArrayList<Film>, filmsPerPage: Int, callback: (ArrayList<Film>) -> Unit) {
        val filmsToLoad: ArrayList<Film> = ArrayList()
        for ((index, film) in (films.clone() as ArrayList<Film>).withIndex()) {
            filmsToLoad.add(film)
            films.removeAt(0)

            if (index == filmsPerPage - 1) {
                break
            }
        }

        val loadedFilms = arrayOfNulls<Film?>(filmsPerPage)
        var count = 0
        for ((index, film) in filmsToLoad.withIndex()) {
            GlobalScope.launch {
                loadedFilms[index] = getMainData(film)
                count++

                Log.d("FILM_DEBUG", "loaded ${film.title} on index $index")
                if (count >= filmsToLoad.size) {
                    val list: ArrayList<Film> = ArrayList()
                    for (item in loadedFilms) {
                        if (item != null) {
                            list.add(item)
                        }
                    }
                    Log.d("FILM_DEBUG", "all loaded")

                    withContext(Dispatchers.Main){
                        callback(list)
                    }
                }
            }
        }
    }
}
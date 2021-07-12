package com.BSLCommunity.onlinefilmstracker.models

import android.util.Log
import android.webkit.CookieManager
import com.BSLCommunity.onlinefilmstracker.objects.Bookmark
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.objects.Schedule
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
    private const val FILM_KP_RATING = "span.kp span"
    private const val FILM_WA_RATING = "span.wa span"

    fun getMainData(film: Film): Film {
        val filmPage: Document = Jsoup.connect(film.link).get()
        val table: Elements = filmPage.select(FILM_TABLE_INFO)

        val genre: String = film.link.split("/")[3]
        film.type = when (genre) {
            "series" -> "Сериал"
            "cartoons" -> "Мультфильм"
            "films" -> "Фильм"
            "animation" -> "Аниме"
            else -> genre
        }

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
                            film.ratingKP = td[1].select(FILM_KP_RATING).text()
                            film.ratingWA = td[1].select(FILM_WA_RATING).text()
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
        val document: Document = Jsoup.connect(film.link).header("Cookie", CookieManager.getInstance().getCookie(BookmarksModel.MAIN_PAGE)).get()
        film.origTitle = document.select("div.b-post__origtitle").text()
        film.description = document.select("div.b-post__description_text").text()
        film.votesIMDB = document.select("span.imdb i").text()
        film.votesKP = document.select("span.kp i").text()
        film.votesWA = document.select("span.wa i").text()
        film.runtime = document.select("td[itemprop=duration]").text()
        film.filmId = document.select("div.b-userset__fav_holder").attr("data-post_id")

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

        val seriesSchedule: ArrayList<Pair<String, ArrayList<Schedule>>> = ArrayList()
        val seasonsElements: Elements = document.select("div.b-post__schedule div.b-post__schedule_block")
        for (block in seasonsElements) {
            var header: String = block.select("div.b-post__schedule_block_title div.title").text()
            val toReplace = "${film.title} - даты выхода серий "

            if (header.contains(toReplace)) {
                header = header.replace("${film.title} - даты выхода серий ", "").dropLast(1)
            }

            val listSchedule: ArrayList<Schedule> = ArrayList()
            val list: Elements = block.select("div.b-post__schedule_list table tbody tr")
            for (el in list) {
                val episode: String = el.select("td.td-1").text()
                val name: String = el.select("td.td-2 b").text()
                val date: String = el.select("td.td-4").text()
                val nextEpisodeIn: String = el.select("td.td-5").text()

                val schedule = Schedule(episode, name, date)
                if (nextEpisodeIn.isNotEmpty()) {
                    schedule.nextEpisodeIn = nextEpisodeIn
                }

                listSchedule.add(schedule)
            }

            seriesSchedule.add(Pair(header, listSchedule))
        }
        film.seriesSchedule = seriesSchedule

        val collectionFilms: ArrayList<Film> = ArrayList()
        val collectionElements: Elements = document.select("div.b-post__partcontent_item")
        for (el in collectionElements) {
            val subFilm: Film

            val a = el.select("div.title a")
            if (a.size > 0) {
                subFilm = Film(a.attr("href"))
                subFilm.title = a.text()
            } else {
                subFilm = Film("")
                subFilm.title = el.select("div.title").text()
            }

            subFilm.year = el.select("div.year").text()
            subFilm.ratingKP = el.select("div.rating i").text()
            collectionFilms.add(subFilm)
        }
        film.collection = collectionFilms

        val relatedFilms: ArrayList<Film> = ArrayList()
        val relatedElements = document.select("div.b-content__inline_item")
        for (el in relatedElements) {
            val cover: String = el.select("div.b-content__inline_item-cover a img").attr("src")
            val a = el.select("div.b-content__inline_item-link a")
            val link: String = a.attr("href")
            val title: String = a.text()
            val misc: String = el.select("div.misc").text()

            val relatedFilm = Film(link)
            relatedFilm.posterPath = cover
            relatedFilm.title = title
            relatedFilm.relatedMisc = misc
            relatedFilms.add(relatedFilm)
        }
        film.related = relatedFilms

        val bookmarks: ArrayList<Bookmark> = ArrayList()
        val bookmarkElements = document.select("div.hd-label-row")
        for (el in bookmarkElements) {
            val name = "${el.select("label")[0].ownText()} (${el.select("b").text()})"
            val isChecked = el.select("input").attr("checked") == "checked"
            val catId = el.select("input").attr("value")

            val bookmark = Bookmark(catId, "", name, 0)
            bookmark.isChecked = isChecked
            bookmarks.add(bookmark)
        }
        film.bookmarks = bookmarks

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

                    withContext(Dispatchers.Main) {
                        callback(list)
                    }
                }
            }
        }
    }

    fun getFilmPosterByLink(filmLink: String): String {
        val filmPage: Document = Jsoup.connect(filmLink).get()
        val posterElement: Element = filmPage.select(FILM_POSTER)[0]
        return posterElement.select("img").attr("src")
    }
}
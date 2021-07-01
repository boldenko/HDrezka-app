package com.BSLCommunity.onlinefilmstracker.models

import com.BSLCommunity.onlinefilmstracker.objects.Film
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException

object NewestFilmsModel {
    private val HDREZKA_NEWEST = "http://hdrezka.tv/new/page/"
    private val FILMS = "div.b-content__inline_item"
    private val FILM_LINK = "div.b-content__inline_item-cover a"
    private val FILM_TITLE = "div.b-post__title h1"
    private val FILM_POSTER = "div.b-sidecover a img"
    private val FILM_TABLE_INFO = "table.b-post__info tbody tr"

    fun getPage(page: Int): Elements {
        val doc: Document = Jsoup.connect(HDREZKA_NEWEST + page).get()
        return doc.select(FILMS)
    }

    fun getFilm(element: Element): Film? {
        val link: String = element.select(FILM_LINK).attr("href")

        return try {
            val filmPage: Document = Jsoup.connect(link).get()
            val table: Elements = filmPage.select(FILM_TABLE_INFO)

            val title: String = filmPage.select(FILM_TITLE).text()
            val type: String = element.select("span.cat i")[0].text()
            val posterPath: String = filmPage.select(FILM_POSTER).attr("src")
            var ratingIMDB: String? = null
            var date = ""
            var year = ""
            val countries: ArrayList<String> = ArrayList()
            val genres: ArrayList<String> = ArrayList()

            // Parse info table
            for (tr in table) {
                val td: Elements = tr.select("td")
                if (td.size > 0) {
                    val h2Els: Elements = td[0].select("h2")
                    if (h2Els.size > 0) {
                        val h2: Element = h2Els[0]

                        when (h2.text()) {
                            "Рейтинги" -> {
                                ratingIMDB = td[1].select("span.b-post__info_rates imdb span").text()
                            }
                            "Дата выхода" -> {
                                date = td[1].ownText()
                                year = td[1].select("a").text()
                            }
                            "Страна" -> {
                                for (el in td[1].select("a")) {
                                    countries.add(el.text())
                                }
                            }
                            "Жанр" -> {
                                for (el in td[1].select("a")) {
                                    genres.add(el.select("span").text())
                                }
                            }
                        }
                    }
                }
            }

            Film(link, title, date, year, posterPath, countries, ratingIMDB, genres, type)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
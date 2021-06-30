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
    private val FILM_RATING_IMDB = "span.b-post__info_r"
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

            val title: String = filmPage.select(FILM_TITLE).text();
            val poster: String = filmPage.select(FILM_POSTER).attr("src")
            val ratingIMDB: String = filmPage.select(FILM_RATING_IMDB).select("span").text()
            var date = ""
            var country = ""

            // Parse info table
            for (tr in table) {
                val td: Elements = tr.select("td")
                if (td[0].select("h2").text().equals("Дата выхода")) {
                    date = td[1].text()
                }

                if (td[0].select("h2").text().equals("Страна")) {
                    country = td[1].select("a").text()
                }
            }

            Film(title, date, poster, country, ratingIMDB, ArrayList())
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
package com.BSLCommunity.onlinefilmstracker.models

import android.webkit.CookieManager
import com.BSLCommunity.onlinefilmstracker.objects.Bookmark
import com.BSLCommunity.onlinefilmstracker.objects.Film
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

object BookmarksModel {
    private const val MAIN_PAGE = "http://hdrezka.tv/favorites/"

    fun getBookmarksList(): ArrayList<Bookmark> {
        val document: Document = Jsoup.connect(MAIN_PAGE).header("Cookie", CookieManager.getInstance().getCookie(MAIN_PAGE)).get()

        val bookmarks: ArrayList<Bookmark> = ArrayList()

        val tableEls: Elements = document.select("div.b-favorites_content__cats_list_item")
        for (el in tableEls) {
            val link: String = el.select("a.b-favorites_content__cats_list_link").attr("href")
            val name: String = el.select("span.name").text()
            val amount: Number = el.select("span.num-holder b").text().toInt()

            bookmarks.add(Bookmark(link, name, amount))
        }

        return bookmarks

    }

    fun getFilmsFromBookmarkPage(link: String, page: Int, sort: String?, show: String?): ArrayList<Film> {
        var url = "${link}/page/${page}/"
        url += if (sort != null && sort.isNotEmpty()) {
            "?filter=${sort}"
        } else {
            "?filter=added"
        }
        if (show != null && show.isNotEmpty() && show != "0") {
            url += "&genre=${show}"
        }

        val document: Document = Jsoup.connect(url).header("Cookie", CookieManager.getInstance().getCookie(MAIN_PAGE)).get()

        val films: ArrayList<Film> = ArrayList()

        val elements: Elements = document.select(FilmsListModel.FILMS)
        for (el in elements) {
            films.add(Film(el.select(FilmsListModel.FILM_LINK).attr("href"), el.select(FilmsListModel.FILM_TYPE)[0].text()))
        }

        return films
    }
}
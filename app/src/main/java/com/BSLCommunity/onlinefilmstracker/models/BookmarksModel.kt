package com.BSLCommunity.onlinefilmstracker.models

import android.util.ArrayMap
import android.webkit.CookieManager
import com.BSLCommunity.onlinefilmstracker.objects.Bookmark
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.objects.SettingsData
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

object BookmarksModel {
    private const val MAIN_PAGE = "/favorites/"
    private const val POST_URL = "/ajax/favorites/"

    fun getBookmarksList(): ArrayList<Bookmark> {
        val document: Document = Jsoup.connect(SettingsData.provider + MAIN_PAGE).header("Cookie", CookieManager.getInstance().getCookie(SettingsData.provider)).get()

        val bookmarks: ArrayList<Bookmark> = ArrayList()

        val tableEls: Elements = document.select("div.b-favorites_content__cats_list_item")
        for (el in tableEls) {
            val catId: String = el.attr("data-cat_id")
            val link: String = el.select("a.b-favorites_content__cats_list_link").attr("href")
            val name: String = el.select("span.name").text()
            val amount: Number = el.select("span.num-holder b").text().toInt()

            bookmarks.add(Bookmark(catId, link, name, amount))
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

        val document: Document = Jsoup.connect(url).header("Cookie", CookieManager.getInstance().getCookie(SettingsData.provider)).get()

        val films: ArrayList<Film> = ArrayList()

        val elements: Elements = document.select(FilmsListModel.FILMS)
        for (el in elements) {
            films.add(Film(el.select(FilmsListModel.FILM_LINK).attr("href")))
        }

        return films
    }

    fun postBookmark(filmId: String, catId: String) {
        val data: ArrayMap<String, String> = ArrayMap()
        data["post_id"] = filmId
        data["cat_id"] = catId
        data["action"] = "add_post"

        Jsoup.connect(SettingsData.provider + POST_URL)
            .data(data)
            .userAgent("Mozilla")
            .header("Cookie", CookieManager.getInstance().getCookie(SettingsData.provider))
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .ignoreContentType(true)
            .post()
    }
}
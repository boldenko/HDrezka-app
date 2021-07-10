package com.BSLCommunity.onlinefilmstracker.models

import android.webkit.CookieManager
import com.BSLCommunity.onlinefilmstracker.objects.WatchLater
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

object WatchLaterModel {
    const val MAIN_PAGE = "http://hdrezka.tv/continue/"

    fun getWatchLaterList(): ArrayList<WatchLater> {
        val document: Document = Jsoup.connect(MAIN_PAGE).header("Cookie", CookieManager.getInstance().getCookie(BookmarksModel.MAIN_PAGE)).get()

        val watchLaterList: ArrayList<WatchLater> = ArrayList()

        val els: Elements = document.select("div.b-videosaves__list_item")
        if (els.size > 0) {
            els.removeFirst()

            for (el in els) {
                val date = el.select("div.date").text()
                val a = el.select("div.title a")
                val name = "${a.text()} ${el.select("div.title small").text()}"
                val filmLInk = a.attr("href")
                val info = el.select("div.info")[0].ownText()
                val additionalInfo = el.select("div.info span.info-holder a").text()

                watchLaterList.add(WatchLater(date, filmLInk, name, info, "$info $additionalInfo"))
            }
        }

        return watchLaterList
    }
}
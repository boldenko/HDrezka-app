package com.falcofemoralis.hdrezkaapp.models

import android.content.Context
import android.os.Environment
import android.webkit.CookieManager
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

object SearchModel {
    private const val SEARCH_URL = "/engine/ajax/search.php"

    fun getFilmsListByQuery(text: String): ArrayList<Film> {
        val doc: Document = Jsoup.connect(SettingsData.provider + SEARCH_URL).data("q", text).userAgent("Mozilla").post()

        val films: ArrayList<Film> = ArrayList()
        val els: Elements = doc.select("li")


        for (el in els) {
            val link: String = el.select("a").attr("href")
            val title: String = el.select("span.enty").text()
            val rating: String = el.select("span.rating i").text()
            val additionalInfo: String = el.select("a")[0].ownText()
            val data = el.select("a")[0].ownText().replace("(", "").replace(")", "").split(",")
            val type: String = if (data.size == 3) {
                data[1]
            } else {
                "Фильм"
            }

            val film = Film(link)
            film.title = title
            film.ratingIMDB = rating // actually kp rating
            film.additionalInfo = additionalInfo
            film.type = type

            films.add(film)
        }

        return films
    }

    fun getFilmsFromSearchPage(query: String, page: Int, context: Context): ArrayList<Film> {
        val doc: Document = Jsoup
            .connect(SettingsData.provider + "/search/?do=search&subaction=search&q=$query&page=$page")
            .header("Cookie", CookieManager.getInstance().getCookie(SettingsData.provider))
            .userAgent(System.getProperty("http.agent"))
            .get()

        /* test */
  /*      val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "testfile")
        val bw = BufferedWriter(FileWriter(file, false))
        bw.write(doc.html())
        bw.close()
*/
        return FilmsListModel.getFilmsFromPage(doc)
    }
}
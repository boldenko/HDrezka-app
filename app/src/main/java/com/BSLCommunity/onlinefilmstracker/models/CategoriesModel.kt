package com.BSLCommunity.onlinefilmstracker.models

import android.util.ArrayMap
import com.BSLCommunity.onlinefilmstracker.objects.Film
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

object CategoriesModel {
    private const val MAIN_PAGE = "http://hdrezka.tv/"

    fun getCategories(): ArrayMap<String, ArrayList<Pair<String, String>>> {
        val doc: Document = Jsoup.connect(MAIN_PAGE).get()

        val categories: ArrayMap<String, ArrayList<Pair<String, String>>> = ArrayMap()
        val els: Elements = doc.select("li.b-topnav__item")
        for (el in els) {
            if (el.classNames().contains("single")) {
                continue
            }

            val header: String = el.select("a.b-topnav__item-link").text()

            val genres: ArrayList<Pair<String, String>> = ArrayList()
            val list: Elements = el.select("ul.left li a")
            for (item in list) {
                val name: String = item.text()
                val link: String = item.attr("href")

                genres.add(Pair(name, link))
            }

            categories[header] = genres
        }

        return categories
    }
}
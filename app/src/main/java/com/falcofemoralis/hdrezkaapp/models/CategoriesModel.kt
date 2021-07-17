package com.falcofemoralis.hdrezkaapp.models

import android.util.ArrayMap
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

object CategoriesModel {
    fun getCategories(): ArrayMap<Pair<String, String>, ArrayList<Pair<String, String>>> {
        val doc: Document = Jsoup.connect(SettingsData.provider).get()

        val categories: ArrayMap<Pair<String, String>, ArrayList<Pair<String, String>>> = ArrayMap()
        val els: Elements = doc.select("li.b-topnav__item")
        for (el in els) {
            if (el.classNames().contains("single")) {
                continue
            }

            val headerName = el.select("a.b-topnav__item-link").text()
            val headerLink = el.select("a.b-topnav__item-link").attr("href")

            val genres: ArrayList<Pair<String, String>> = ArrayList()
            val list: Elements = el.select("select.select-category option")
            for (item in list) {
                val name: String = item.text()
                val link: String = item.attr("value")

                genres.add(Pair(name, link))
            }

            categories[Pair(headerName, headerLink)] = genres
        }

        return categories
    }

    fun getYears(): ArrayList<String> {
        val doc: Document = Jsoup.connect(SettingsData.provider).get()

        val years: ArrayList<String> = ArrayList()
        val els: Elements = doc.select("select.select-year")[0].select("option")
        for (el in els) {
            years.add(el.text())
        }

        return years
    }

    fun getFilmsFromCategory(catLink: String, page: Int): ArrayList<Film> {
        return FilmsListModel.getFilmsFromPage(SettingsData.provider + catLink + "page/" + page)
    }
}
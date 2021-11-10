package com.falcofemoralis.hdrezkaapp.models

import android.util.ArrayMap
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SeriesUpdateItem
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import org.jsoup.nodes.Document

import android.webkit.CookieManager

object NewestFilmsModel {
    private const val NEWEST = "/new/page/"

    fun getNewestFilms(page: Int, filter: String): ArrayList<Film> {
        val doc: Document = BaseModel.getJsoup(SettingsData.provider + "$NEWEST$page/?filter=$filter").get()
        return FilmsListModel.getFilmsFromPage(doc)
    }

    fun getSeriesUpdates(): LinkedHashMap<String, ArrayList<SeriesUpdateItem>> {
        val doc: Document = BaseModel.getJsoup(SettingsData.provider)
            .header("Cookie", CookieManager.getInstance().getCookie(SettingsData.provider))
            .get()

        val seriesUpdates: LinkedHashMap<String, ArrayList<SeriesUpdateItem>> = LinkedHashMap()

        val blocks = doc.select("div.b-seriesupdate__block")
        if (blocks.size > 0) {
            for (block in blocks) {
                var date = ""
                val dateItem = block.select("div.b-seriesupdate__block_date")
                if (dateItem != null) {
                    date = dateItem.text().replace(" развернуть", "")
                }

                val series: ArrayList<SeriesUpdateItem> = ArrayList()
                val seriesItems = block.select("li.b-seriesupdate__block_list_item")
                for (item in seriesItems) {
                    val title = item.select("div.cell a.b-seriesupdate__block_list_link").text()
                    val link = item.select("div.cell a.b-seriesupdate__block_list_link").attr("href")
                    val season = item.select("span.season").text()
                    val episode = item.select("span.cell").text()
                    val voice = item.select("span.cell i").text()
                    val isUserWatch = item.hasClass("tracked")

                    if(link.isNotEmpty() && title.isNotEmpty()) {
                        series.add(SeriesUpdateItem(link, title, season, episode, voice, isUserWatch))
                    }
                }

                seriesUpdates[date] = series
            }
        }

        return seriesUpdates
    }
}
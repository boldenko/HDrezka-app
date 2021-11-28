package com.falcofemoralis.hdrezkaapp.models

import android.webkit.CookieManager
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SeriesUpdateItem
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import org.jsoup.nodes.Document

object NewestFilmsModel {
    private const val NEWEST = "/new/page/"
    val SORTS: ArrayList<String> = arrayListOf("last", "popular", "watching")
    val TYPES: ArrayList<String> = arrayListOf("0", "1", "2", "3", "82") // all, films, serials, multfilms, anime

    fun getNewestFilms(page: Int, sort: String, type: String): ArrayList<Film> {
        val doc: Document = BaseModel.getJsoup(SettingsData.provider + "$NEWEST$page/?filter=$sort&genre=$type").get()
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
                    val episodeItems = item.select("span.cell")
                    var episode = ""
                    if (episodeItems.size > 0) {
                        episode = episodeItems[0].ownText()
                    }
                    val voice = item.select("span.cell i").text()
                    val isUserWatch = item.hasClass("tracked")

                    if (link.isNotEmpty() && title.isNotEmpty()) {
                        series.add(SeriesUpdateItem(link, title, season, episode, voice, isUserWatch))
                    }
                }

                seriesUpdates[date] = series
            }
        }

        return seriesUpdates
    }
}
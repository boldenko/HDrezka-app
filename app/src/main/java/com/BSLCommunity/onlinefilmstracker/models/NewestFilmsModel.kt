package com.BSLCommunity.onlinefilmstracker.models

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

object NewestFilmsModel {
    private val HDREZKA_NEWEST = "http://hdrezka.tv/new/page/"
    private val FILMS = "div.b-content__inline_item"

    fun getPage(page: Int, callback: (Elements) -> Unit) {
        GlobalScope.launch {
            val doc: Document = Jsoup.connect(HDREZKA_NEWEST + page).get()
            callback(doc.select(FILMS))
        }
    }
}
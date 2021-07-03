package com.BSLCommunity.onlinefilmstracker.models

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

object NewestFilmsModel {
    private val HDREZKA_NEWEST = "http://hdrezka.tv/new/page/"
    private val FILMS = "div.b-content__inline_item"

    fun getPage(page: Int): Elements {
        val doc: Document = Jsoup.connect(HDREZKA_NEWEST + page).get()
        return doc.select(FILMS)
    }
}
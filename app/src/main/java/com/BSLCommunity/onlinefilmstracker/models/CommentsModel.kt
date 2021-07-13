package com.BSLCommunity.onlinefilmstracker.models

import com.BSLCommunity.onlinefilmstracker.objects.Comment
import com.BSLCommunity.onlinefilmstracker.objects.SettingsData
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements

object CommentsModel {
    private const val COMMENT_LINK = "/ajax/get_comments/"

    // filmId = news_id
    // t = unix time
    // cstart = page
    fun getCommentsFromPage(page: Int, filmId: String): ArrayList<Comment> {
        val unixTime = System.currentTimeMillis()
        val result: String = Jsoup.connect(SettingsData.provider + COMMENT_LINK + "?t=$unixTime&news_id=$filmId&cstart=$page&type=0&comment_id=0&skin=hdrezka")
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .ignoreContentType(true)
            .execute()
            .body()

        val jsonObject = JSONObject(result)
        val doc = Jsoup.parse(jsonObject.getString("comments"))

        val comments: ArrayList<Comment> = ArrayList()
        val list: Elements = doc.select("li.comments-tree-item")
        for (el in list) {
            val avatarPath: String = el.select("div.ava img")[0].attr("src")
            val nickname: String = el.select("span.name")[0].text()
            val date: String = el.select("span.date")[0].text()
            val text: ArrayList<Pair<Comment.TextType, String>> = ArrayList()
            val textElements: Element = el.select("div.text div")[0]
            for (textItem in textElements.childNodes()) {
                when (textItem) {
                    is TextNode -> {
                        text.add(Pair(Comment.TextType.REGULAR, textItem.text()))
                    }
                    is Element -> {
                        if (textItem.hasClass("text_spoiler")) {
                            text.add(Pair(Comment.TextType.SPOILER, textItem.text()))
                        } else if (textItem.text() != "спойлер") {
                            text.add(Pair(Comment.TextType.REGULAR, textItem.text()))
                        }
                    }
                }
            }
            val indent: Int = el.attr("data-indent")[0].toString().toInt()

            comments.add(Comment(avatarPath, nickname, text, date, indent))
        }

        return comments
    }
}
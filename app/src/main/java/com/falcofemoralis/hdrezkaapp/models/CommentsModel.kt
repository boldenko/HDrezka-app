package com.falcofemoralis.hdrezkaapp.models

import android.util.Log
import com.falcofemoralis.hdrezkaapp.objects.Comment
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import org.json.JSONObject
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements

object CommentsModel {
    private const val COMMENT_LINK = "/ajax/get_comments/"
    private const val COMMENT_ADD = "/ajax/add_comment/"
    private const val COMMENT_LIKE = "/ajax/comments_likes/"

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
            comments.add(getCommentData(el))
        }

        if (comments.size == 0) {
            throw Exception("Empty list")
        }

        return comments
    }

    private fun getCommentData(el: Element): Comment {
        val id: Int = el.select("div")[0].attr("id").toString().replace("comment-id-", "").toInt()
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

        return Comment(id, avatarPath, nickname, text, date, indent)
    }

    fun postComment(filmId: String, text: String, parent: Int, indent: Int): Comment {
        // NEW
        val success: Boolean = true
        //comments: Лучшая часть!
        //post_id: 25923
        //type: 0
        //parent: 0
        //g_recaptcha_response:
        //has_adb: 1

        // TO SOMEONE
        //comments: Согласен, но все же с юмором переборщили)
        //post_id: 25923 <-- film id?
        //type: 0
        //parent: 2379004 <-- COMMENT ID?
        //g_recaptcha_response:
        //has_adb: 1
        Log.d("COMMENT_EDITOR_TEST", "addComment($filmId, $text)")

        //{success: true, comment_id: 2386263,…}
        //comment_id: 2386263
        //message: "<div id='comment-id-2386263'><div class=\"b-comment clearfix\">\r\n    <div class=\"ava\">\r\n        <img src=\"https://static.hdrezka.ac/uploads/fotos/2020/10/9/l26372e4a0f81be52i21b.jpg\" height=\"60\" width=\"60\" alt=\"Papygai4ik\" />\r\n    </div>\r\n    <div class=\"message\">\r\n        <div class=\"info\">\r\n            <!-- <span class=\"b-comment__answers_ctrl\" data-show=\"1\">скрыть ответы</span> -->\r\n            \r\n            <span class=\"name\">Papygai4ik</span>,\r\n            <span class=\"date\">оставлен сегодня, 19:21</span>\r\n            <a class=\"share-link\" href=\"#comment2386263\" data-id=\"2386263\">#</a>\r\n            \r\n        </div>\r\n        <div class=\"text\"><div id='comm-id-2386263'>В 15 серии <!--dle_spoiler--><div class=\"title_spoiler\"><img id=\"image-sp2d923aa262064e8dc4e657a3680b2f15\" style=\"vertical-align: middle;border: none;\" alt=\"\" src=\"http://hdrezka.tv/templates/hdrezka/dleimages/spoiler-plus.gif\" /><a href=\"javascript:ShowOrHide('sp2d923aa262064e8dc4e657a3680b2f15')\">спойлер<img class=\"attention\" height=\"15\" width=\"16\" src=\"http://hdrezka.tv/templates/hdrezka/images/spoiler-attention.png\" alt=\"\" /></a></div><div id=\"sp2d923aa262064e8dc4e657a3680b2f15\" class=\"text_spoiler\" style=\"display:none;\"><!--spoiler_text-->опять разговоры)<!--spoiler_text_end--></div><!--/dle_spoiler--></div></div>\r\n        <div class=\"actions\">\r\n            <ul class=\"edit\">\r\n                \r\n                <li><a onclick=\"ajax_comm_edit('2386263', 'ajax'); return false;\" href=\"http://hdrezka.tv/index.php?do=comments&amp;action=comm_edit&amp;id=2386263&amp;area=ajax\">Изменить</a></li>\r\n                <li><a href=\"javascript:void(0)\" onclick=\"sof.comments.deleteComment(2386263, 'cc049189a849dd8e5b2c1f0caca8f010', 0, 'ajax');\">Удалить</a></li>\r\n                \r\n            </ul>\r\n            <span class=\"b-comment__quoteuser\" onclick=\"sof.comments.quoteUser('Papygai4ik', '2386263', '0');\">Ответить</span>\r\n            <span data-likes_num=\"0\" class=\"show-likes-comment b-comment__like_it self-disabled disabled\" data-comment_id=\"2386263\"><i>Поддерживаю!</i></span>\r\n            <span class=\"b-comment__likes_count\">(<i>0</i>)</span>\r\n            \r\n        </div>\r\n    </div>    \r\n</div>\r\n</div>"
        //success: true
        if (success) {
            return Comment(0, SettingsData.staticProvider + "/i/nopersonphoto.png", "Victor", arrayListOf(Pair(Comment.TextType.REGULAR, text)), "2020-05-06", indent)
        } else {
            throw HttpStatusException("failed to post comment", 400, SettingsData.provider)
        }
    }

    fun addLike() {
        //comment_id: 2379004
    }
}
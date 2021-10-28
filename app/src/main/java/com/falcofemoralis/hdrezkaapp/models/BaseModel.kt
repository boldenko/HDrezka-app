package com.falcofemoralis.hdrezkaapp.models

import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import org.jsoup.Connection
import org.jsoup.Jsoup

open class BaseModel {
    fun getJsoup(link: String?): Connection {
        return Jsoup.connect(link)
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
            .ignoreContentType(true)

    }
}
package com.falcofemoralis.hdrezkaapp.models

import org.jsoup.Connection
import org.jsoup.Jsoup

open class BaseModel {
    companion object{
        const val userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36"
    }

    fun getJsoup(link: String?): Connection {
        return Jsoup.connect(link)
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .userAgent(userAgent)
            .ignoreContentType(true)
    }
}
package com.BSLCommunity.onlinefilmstracker.objects

import java.io.Serializable

open class Film(
    val link: String,
    val type: String
) : Serializable {
    var title: String? = null
    var date: String? = null
    var year: String? = null
    var posterPath: String? = null
    var fullSizePosterPath: String? = null
    var countries: ArrayList<String>? = null
    var ratingIMDB: String? = null
    var genres: ArrayList<String>? = null
    var origTitle: String? = null
    var description: String? = null
    var votes: String? = null
    var runtime: String? = null
    var actorsLinks: ArrayList<String>? = null
    var directors: ArrayList<String>? = null
}

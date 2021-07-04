package com.BSLCommunity.onlinefilmstracker.objects

import java.io.Serializable

open class Film(
    val link: String,
    val title: String,
    val date: String,
    val year: String,
    val posterPath: String,
    val fullSizePosterPath: String,
    val countries: ArrayList<String>,
    val ratingIMDB: String?,
    val genres: ArrayList<String>,
    val type: String
) : Serializable {
    var origTitle: String? = null
    var description: String? = null
    var votes: String? = null
    var runtime: String? = null
    var actorsLinks: ArrayList<String>? = null
    var directors: ArrayList<String>? = null
}

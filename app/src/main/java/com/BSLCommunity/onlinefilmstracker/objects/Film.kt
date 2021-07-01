package com.BSLCommunity.onlinefilmstracker.objects

data class Film(
    val filmLink: String,
    val title: String,
    val date: String,
    val year: String,
    val posterPath: String,
    val countries: ArrayList<String>,
    val ratingIMDB: String?,
    val genres: List<String>,
    val type: String
)
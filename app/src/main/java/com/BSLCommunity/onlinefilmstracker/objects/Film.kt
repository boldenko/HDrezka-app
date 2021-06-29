package com.BSLCommunity.onlinefilmstracker.objects

class Film(
    val title: String,
    val date: String,
    val posterPath: String,
    val country: String,
    val rating: String,
    val genres: List<String>
) {
}
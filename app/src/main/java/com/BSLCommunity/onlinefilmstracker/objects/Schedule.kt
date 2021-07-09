package com.BSLCommunity.onlinefilmstracker.objects

data class Schedule(
    val episode: String,
    val name: String,
    val date: String,
) {
    var nextEpisodeIn: String? = null
}


package com.BSLCommunity.onlinefilmstracker.objects

import java.io.Serializable

data class Schedule(
    val episode: String,
    val name: String,
    val date: String,
) : Serializable {
    var nextEpisodeIn: String? = null
}


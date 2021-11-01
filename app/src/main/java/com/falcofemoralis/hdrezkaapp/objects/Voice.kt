package com.falcofemoralis.hdrezkaapp.objects

import java.io.Serializable

open class Voice : Serializable {
    constructor(name: String, id: String) {
        this.name = name
        this.id = id
    }

    constructor(streams: String) {
        this.streams = streams
    }

    constructor(id: String, seasons: HashMap<String, ArrayList<String>>) {
        this.id = id
        this.seasons = seasons
    }

    var name: String? = null
    var id: String? = null
    var streams: String? = null
    var seasons: HashMap<String, ArrayList<String>>? = null
    var isCamrip: String = "0"
    var isDirector: String = "0"
    var isAds: String = "0"
    var selectedEpisode: Pair<String, String>? = null
    var subtitles: ArrayList<Subtitle>? = null
    var thumbnails: String? = null
}


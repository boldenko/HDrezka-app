package com.falcofemoralis.hdrezkaapp.objects

import java.io.Serializable

open class Film : Serializable {
    constructor()

    constructor(id: Int) {
        filmId = id
    }

    constructor(link: String) {
        filmLink = link
    }

    var filmId: Int? = null
    var filmLink: String? = null
    var type: String? = null
    var title: String? = null
    var date: String? = null
    var year: String? = null
    var posterPath: String? = null
    var fullSizePosterPath: String? = null
    var countries: ArrayList<String>? = null
    var ratingIMDB: String? = null
    var ratingKP: String? = null
    var ratingWA: String? = null
    var ratingHR: String? = null
    var isHRratingActive: Boolean = false
    var genres: ArrayList<String>? = null
    var origTitle: String? = null
    var description: String? = null
    var votesIMDB: String? = null
    var votesKP: String? = null
    var votesWA: String? = null
    var votesHR: String? = null
    var runtime: String? = null
    var actors: ArrayList<Actor>? = null
    var directors: ArrayList<Actor>? = null
    var additionalInfo: String? = null
    var hasMainData: Boolean = false
    var hasAdditionalData: Boolean = false
    var seriesSchedule: ArrayList<Pair<String, ArrayList<Schedule>>>? = null
    var collection: ArrayList<Film>? = null
    var related: ArrayList<Film>? = null
    var relatedMisc: String? = null
    var bookmarks: ArrayList<Bookmark>? = null
    var isMovieTranslation: Boolean? = null
    var translations: ArrayList<Voice>? = null
    var isAwaiting: Boolean = false
    var subInfo: String? = null
}

package com.BSLCommunity.onlinefilmstracker.viewsInterface

import android.util.ArrayMap
import com.BSLCommunity.onlinefilmstracker.objects.Actor
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.objects.Schedule

interface FilmView {
    fun setFilmBaseData(film: Film)

    fun setActors(actors: ArrayList<Actor?>)

    fun setDirectors(directors: ArrayList<String>)

    fun setCountries(countries: ArrayList<String>)

    fun setGenres(genres: ArrayList<String>)

    fun setFilmLink(link: String)

    fun setFullSizeImage(posterPath: String)

    fun setPlayer(link: String)

    fun setSchedule(schedule: ArrayList<Pair<String, ArrayList<Schedule>>>)

    fun setCollection(collection: ArrayList<Film>)

    fun setRelated(collection: ArrayList<Film>)
}
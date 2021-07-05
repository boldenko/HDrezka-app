package com.BSLCommunity.onlinefilmstracker.viewsInterface

import com.BSLCommunity.onlinefilmstracker.objects.Actor
import com.BSLCommunity.onlinefilmstracker.objects.Film

interface FilmView {
    fun setFilmBaseData(film: Film)

    fun setActors(actors: ArrayList<Actor?>)

    fun setDirectors(directors: ArrayList<String>)

    fun setCountries(countries: ArrayList<String>)

    fun setGenres(genres: ArrayList<String>)

    fun setFilmLink(link: String)

    fun setFullSizeImage(posterPath: String)

    fun setPlayer(link: String)
}
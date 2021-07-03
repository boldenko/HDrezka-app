package com.BSLCommunity.onlinefilmstracker.viewsInterface

import com.BSLCommunity.onlinefilmstracker.objects.Film

interface FilmView {
    fun setFilmBaseData(film: Film)

    fun setActors(actors: ArrayList<String>)

    fun setDirectors(directors: ArrayList<String>)

    fun setCountries(countries: ArrayList<String>)

    fun setGenres(genres: ArrayList<String>)

    fun setFilmLink(link: String)
}
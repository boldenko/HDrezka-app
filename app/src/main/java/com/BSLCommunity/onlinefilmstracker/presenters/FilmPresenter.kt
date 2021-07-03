package com.BSLCommunity.onlinefilmstracker.presenters

import com.BSLCommunity.onlinefilmstracker.models.FilmModel
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.viewsInterface.FilmView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilmPresenter(private val filmView: FilmView) {
    fun initFilmData(film: Film) {
        GlobalScope.launch {
            FilmModel.setAdditionalData(film)

            withContext(Dispatchers.Main) {
                filmView.setFilmBaseData(film)
                filmView.setGenres(film.genres)
                filmView.setCountries(film.countries)
                film.directors?.let { filmView.setDirectors(it) }
                film.actors?.let { filmView.setActors(it) }
                filmView.setFilmLink(film.link)
            }
        }
    }
}
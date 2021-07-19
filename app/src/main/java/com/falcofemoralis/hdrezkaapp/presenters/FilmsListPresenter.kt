package com.falcofemoralis.hdrezkaapp.presenters

import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.interfaces.IProgressState
import com.falcofemoralis.hdrezkaapp.models.FilmModel
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.Filters
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmsListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilmsListPresenter(
    private val filmsListView: FilmsListView,
    private val view: IConnection,
    private val filters: Filters,
    private val iFilmsList: IFilmsList
) {
    interface IFilmsList {
        fun getMoreFilms(): ArrayList<Film>
    }

    private val FILMS_PER_PAGE: Int = 9

    private var isLoading: Boolean = false // loading condition
    val filmList: ArrayList<Film> = ArrayList()
    val allFilms: ArrayList<Film> = ArrayList() // all loaded films
    val activeFilms: ArrayList<Film> = ArrayList() // current active films
    private var sortedFilmsCount: Int = 0 // current sorted films

    fun addMoreFilms(films: ArrayList<Film>) {
        filmList.addAll(films)
    }

    fun getNextFilms() {
        if (isLoading) {
            return
        }

        isLoading = true
        filmsListView.setProgressBarState(IProgressState.StateType.LOADING)
        GlobalScope.launch {
            try {
                if (filmList.size == 0) {
                    filmList.addAll(iFilmsList.getMoreFilms())
                }
                FilmModel.getFilmsData(filmList, FILMS_PER_PAGE, ::processFilms)
            } catch (e: Exception) {
                if (e.message != "Empty list") {
                    ExceptionHelper.catchException(e, view)
                }

                isLoading = false
                withContext(Dispatchers.Main) {
                    filmsListView.setProgressBarState(IProgressState.StateType.LOADED)
                }
                return@launch
            }
        }
    }

    private fun processFilms(films: ArrayList<Film>) {
        allFilms.addAll(films)
        addFilms(films)
    }

    fun addFilms(films: ArrayList<Film>) {
        isLoading = false

        // sort films
        val sortedFilms: ArrayList<Film> = ArrayList()
        for (film in films) {
            if (filters.checkFilmForFilters(film)) {
                sortedFilms.add(film)
            }
        }
        activeFilms.addAll(sortedFilms)
        filmsListView.redrawFilms()

        sortedFilmsCount += sortedFilms.size
        if (sortedFilmsCount >= FILMS_PER_PAGE) {
            sortedFilmsCount = 0
            filmsListView.setProgressBarState(IProgressState.StateType.LOADED)
        } else {
            getNextFilms()
        }
    }

    fun reset() {
        isLoading = false
        allFilms.clear()
        activeFilms.clear()
        sortedFilmsCount = 0
        filmsListView.redrawFilms()
    }

    fun applyFilter() {
        activeFilms.clear()
        filmsListView.redrawFilms()
        filmsListView.setProgressBarState(IProgressState.StateType.LOADING)
        addFilms(allFilms)
    }
}
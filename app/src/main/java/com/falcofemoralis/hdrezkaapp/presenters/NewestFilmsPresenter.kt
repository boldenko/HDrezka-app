package com.falcofemoralis.hdrezkaapp.presenters

import com.falcofemoralis.hdrezkaapp.constants.AppliedFilter
import com.falcofemoralis.hdrezkaapp.interfaces.IProgressState
import com.falcofemoralis.hdrezkaapp.models.FilmModel
import com.falcofemoralis.hdrezkaapp.models.NewestFilmsModel
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.Filters
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper.catchException
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmsListView
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.NewestFilmsView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewestFilmsPresenter(private val newestFilmsView: NewestFilmsView, private val filmsListView: FilmsListView) : Filters.IFilter {
    private val FILMS_PER_PAGE: Int = 9

    var filters: Filters = Filters(this)
    private val sortFilters: ArrayList<String> = arrayListOf("last", "popular", "watching")
    private var currentPage: Int = 1 // newest film page
    private var isLoading: Boolean = false // loading condition
    private var newestFilms: ArrayList<Film> = ArrayList() // current films links and their types from newest page
    private val allFilms: ArrayList<Film> = ArrayList() // all loaded films
    private val activeFilms: ArrayList<Film> = ArrayList() // current active films
    private var sortedFilmsCount: Int = 0 // current sorted films
    private var sortFilter: String = sortFilters[0]

    fun initFilms() {
        filmsListView.setFilms(activeFilms)
        getNextFilms()
    }

    fun getNextFilms() {
        if (isLoading) {
            return
        }

        isLoading = true
        filmsListView.setProgressBarState(IProgressState.StateType.LOADING)
        GlobalScope.launch {
            try {
                if (newestFilms.size == 0) {
                    newestFilms = NewestFilmsModel.getNewestFilms(currentPage, sortFilter)
                    currentPage++
                }
                FilmModel.getFilmsData(newestFilms, FILMS_PER_PAGE, ::processFilms)
            } catch (e: Exception) {
                if (e.message != "Empty list") {
                    catchException(e, newestFilmsView)
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

    private fun addFilms(films: ArrayList<Film>) {
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

    override fun applyFilters() {
        val sortItem = filters.appliedFilters[AppliedFilter.SORT]?.get(0)
        if (sortItem != null) {
            sortFilter = sortFilters[sortItem.toInt()]
            reset()
            getNextFilms()
        } else {
            activeFilms.clear()
            filmsListView.redrawFilms()
            filmsListView.setProgressBarState(IProgressState.StateType.LOADING)
            addFilms(allFilms)
        }
    }

    fun reset() {
        currentPage = 1
        isLoading = false
        newestFilms.clear()
        allFilms.clear()
        activeFilms.clear()
        sortedFilmsCount = 0
        filmsListView.redrawFilms()
    }
}
package com.falcofemoralis.hdrezkaapp.presenters

import android.util.ArrayMap
import com.falcofemoralis.hdrezkaapp.constants.AppliedFilter
import com.falcofemoralis.hdrezkaapp.interfaces.IProgressState
import com.falcofemoralis.hdrezkaapp.models.FilmModel
import com.falcofemoralis.hdrezkaapp.models.NewestFilmsModel
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper.catchException
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmsListView
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.NewestFilmsView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewestFilmsPresenter(private val newestFilmsView: NewestFilmsView, private val filmsListView: FilmsListView) {
    private val FILMS_PER_PAGE: Int = 9

    private var currentPage: Int = 1 // newest film page
    private var isLoading: Boolean = false // loading condition
    private var newestFilms: ArrayList<Film> = ArrayList() // current films links and their types from newest page
    private val allFilms: ArrayList<Film> = ArrayList() // all loaded films
    private val activeFilms: ArrayList<Film> = ArrayList() // current active films
    private var sortedFilmsCount: Int = 0 // current sorted films
    private var appliedFilters: ArrayMap<AppliedFilter, Array<String?>> = ArrayMap() // applied filters
    private var appliedFiltersTmp: ArrayMap<AppliedFilter, Array<String?>>? = null

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
                    newestFilms = NewestFilmsModel.getNewestFilms(currentPage)
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
            if (checkFilmForFilters(film)) {
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

    fun initFilters() {
        appliedFiltersTmp = ArrayMap(appliedFilters)
    }

    fun setFilter(key: AppliedFilter, value: Array<String?>) {
        appliedFilters[key] = value
        appliedFiltersTmp
    }

    fun applyFilters() {
        activeFilms.clear()
        filmsListView.redrawFilms()
        filmsListView.setProgressBarState(IProgressState.StateType.LOADING)
        addFilms(allFilms)
    }

    fun dismissFilters() {
        appliedFiltersTmp?.let {
            appliedFilters = it
        }
    }

    fun clearFilters() {
        appliedFilters.clear()
        applyFilters()
    }

    fun getFilter(key: AppliedFilter): Array<String?>? {
        return appliedFilters[key]
    }

    // true - film соотвествует критериям
    // false - фильм не соотвествует критериям
    private fun checkFilmForFilters(film: Film): Boolean {
        val applyList: ArrayMap<AppliedFilter, Boolean> = ArrayMap()

        val filters: ArrayMap<AppliedFilter, ArrayList<String>> = ArrayMap()
        for (filterEntry in appliedFilters) {
            val list: ArrayList<String> = ArrayList()
            for (item in filterEntry.value) {
                if (item != null) {
                    list.add(item)
                }
            }
            if (list.size > 0) {
                filters[filterEntry.key] = list
            }
        }

        for (filterEntry in filters) {
            when (filterEntry.key) {
                AppliedFilter.COUNTRIES -> {
                    film.countries?.let {
                        for (country in film.countries!!) {
                            applyList[AppliedFilter.COUNTRIES] = country in filterEntry.value

                            if (applyList[AppliedFilter.COUNTRIES] == true) {
                                break
                            }
                        }
                    }
                }

                AppliedFilter.GENRES -> {
                    film.genres?.let {
                        for (genre in film.genres!!) {
                            applyList[AppliedFilter.GENRES] = genre in filterEntry.value

                            if (applyList[AppliedFilter.GENRES] == true) {
                                break
                            }
                        }
                    }
                }

                AppliedFilter.COUNTRIES_INVERTED -> {
                    film.countries?.let {
                        for (country in film.countries!!) {
                            applyList[AppliedFilter.COUNTRIES_INVERTED] = country !in filterEntry.value

                            if (applyList[AppliedFilter.COUNTRIES_INVERTED] == false) {
                                break
                            }
                        }
                    }
                }

                AppliedFilter.GENRES_INVERTED -> {
                    film.genres?.let {
                        for (genre in film.genres!!) {
                            applyList[AppliedFilter.GENRES_INVERTED] = genre !in filterEntry.value

                            if (applyList[AppliedFilter.GENRES_INVERTED] == false) {
                                break
                            }
                        }
                    }
                }

                AppliedFilter.RATING -> {
                    if (film.ratingIMDB != null && film.ratingIMDB?.isNotEmpty() == true) {
                        val min: Float = filterEntry.value[0].toFloat()
                        val max: Float = filterEntry.value[1].toFloat()
                        val filmRating: Float = film.ratingIMDB!!.toFloat()
                        applyList[AppliedFilter.RATING] = filmRating in min..max
                    } else {
                        applyList[AppliedFilter.RATING] = false
                    }
                }

                AppliedFilter.YEAR -> {
                    if (film.year != null && film.year?.isNotEmpty() == true) {
                        val min: Float = filterEntry.value[0].toFloat()
                        val max: Float = filterEntry.value[1].toFloat()
                        val filmYear: Float = film.year!!.take(4).toFloat()

                        applyList[AppliedFilter.YEAR] = filmYear in min..max
                    } else {
                        applyList[AppliedFilter.YEAR] = false
                    }
                }

                AppliedFilter.TYPE -> {
                    if (filterEntry.value[0] == "Все") {
                        continue
                    }

                    film.type.let {
                        applyList[AppliedFilter.TYPE] = filterEntry.value[0].take(4) == film.type!!.take(4)
                    }
                }
            }
        }

        var isApply = true
        for (applyItem in applyList) {
            isApply = isApply and applyItem.value
        }

        return isApply
    }
}
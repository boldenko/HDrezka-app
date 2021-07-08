package com.BSLCommunity.onlinefilmstracker.presenters

import android.util.ArrayMap
import android.util.Log
import com.BSLCommunity.onlinefilmstracker.constants.AppliedFilter
import com.BSLCommunity.onlinefilmstracker.models.FilmModel
import com.BSLCommunity.onlinefilmstracker.models.FilmsListModel
import com.BSLCommunity.onlinefilmstracker.models.NewestFilmsModel
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.views.INoConnection
import com.BSLCommunity.onlinefilmstracker.viewsInterface.FilmsListView
import com.BSLCommunity.onlinefilmstracker.viewsInterface.NewestFilmsView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NewestFilmsPresenter(private val newestFilmsView: NewestFilmsView, private val filmsListView: FilmsListView, private val noConnectionInterface: INoConnection) {
    private val FILMS_PER_PAGE: Int = 9

    private var currentPage: Int = 1 // newest film page
    private var isLoading: Boolean = false // loading condition
    private var newestFilms: ArrayList<Film> = ArrayList() // current films links and their types from newest page
    private val allFilms: ArrayList<Film> = ArrayList() // all loaded films
    private val activeFilms: ArrayList<Film> = ArrayList() // current active films
    private var sortedFilmsCount: Int = 0 // current sorted films
    private var appliedFilters: ArrayMap<AppliedFilter, ArrayList<String>> = ArrayMap() // applied filters

    fun initFilms() {
        filmsListView.setFilms(activeFilms)
        getNextFilms()
    }

    fun getNextFilms() {
        if (isLoading) {
            return
        }

        isLoading = true
        filmsListView.setProgressBarState(true)
        GlobalScope.launch {
            if (newestFilms.size == 0) {
                try {
                    newestFilms = FilmsListModel.getFilmsFromPage(NewestFilmsModel.HDREZKA_NEWEST + currentPage)
                    currentPage++
                } catch (e: Exception) {
                    noConnectionInterface.showErrorDialog()
                    return@launch
                }
            }
            FilmModel.getFilmsData(newestFilms, FILMS_PER_PAGE, ::processFilms)
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
                Log.d("FILM_DEBUG", "sorted ${film.title}")
                sortedFilms.add(film)
            }
        }
        activeFilms.addAll(sortedFilms)
        filmsListView.redrawFilms()

        sortedFilmsCount += sortedFilms.size
        if (sortedFilmsCount >= FILMS_PER_PAGE) {
            sortedFilmsCount = 0
            filmsListView.setProgressBarState(false)
        } else {
            getNextFilms()
        }
    }

    fun setFilter(key: AppliedFilter, value: ArrayList<String>) {
        appliedFilters[key] = value
    }

    fun applyFilters() {
        activeFilms.clear()
        filmsListView.redrawFilms()
        filmsListView.setProgressBarState(true)
        addFilms(allFilms)
    }

    // true - film соотвествует критериям
    // false - фильм не соотвествует критериям
    private fun checkFilmForFilters(film: Film): Boolean {
        val applyList: ArrayMap<AppliedFilter, Boolean> = ArrayMap()

        for (filterEntry in appliedFilters) {
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

                    applyList[AppliedFilter.TYPE] = filterEntry.value[0].take(4) == film.type.take(4)
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
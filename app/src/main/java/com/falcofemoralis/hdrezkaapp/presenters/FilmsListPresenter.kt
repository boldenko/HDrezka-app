package com.falcofemoralis.hdrezkaapp.presenters

import android.util.ArrayMap
import com.falcofemoralis.hdrezkaapp.constants.AdapterAction
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.interfaces.IProgressState
import com.falcofemoralis.hdrezkaapp.models.FilmModel
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper
import com.falcofemoralis.hdrezkaapp.views.elements.FiltersMenu
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmsListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException

class FilmsListPresenter(
    private val filmsListView: FilmsListView,
    private val view: IConnection,
    private val iFilmsList: IFilmsList,
) {
    interface IFilmsList {
        fun getMoreFilms(): ArrayList<Film>
    }

    private var filmsPerPage: Int = 0

    init {
        SettingsData.filmsInRow?.let {
            filmsPerPage = it * SettingsData.rowMultiplier!!
        }
    }

    private var isLoading: Boolean = false // loading condition
    val filmList: ArrayList<Film> = ArrayList()
    val allFilms: ArrayList<Film> = ArrayList() // all loaded films
    val activeFilms: ArrayList<Film> = ArrayList() // current active films
    private var sortedFilmsCount: Int = 0 // current sorted films
    var appliedFilters: ArrayMap<FiltersMenu.AppliedFilter, Array<String?>> = ArrayMap()
    private var token = ""

    fun addMoreFilms(films: ArrayList<Film>) {
        filmList.addAll(films)
    }

    fun setToken(token: String) {
        this.token = token
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

                val tokenTmp = token
                fun processFilms(films: ArrayList<Film>) {
                    if (tokenTmp == token) {
                        allFilms.addAll(films)
                        addFilms(films)
                    }
                }

                FilmModel.getFilmsData(filmList, filmsPerPage, ::processFilms)
            } catch (e: HttpStatusException) {
                if (e.statusCode != 404 || e.statusCode == 503) {
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

    private fun addFilms(films: ArrayList<Film>) {
        isLoading = false

        // sort films
        val sortedFilms: ArrayList<Film> = ArrayList()
        for (film in films) {
            if (checkFilmForFilters(film)) {
                sortedFilms.add(film)
            }
        }
        val itemsCount = activeFilms.size
        activeFilms.addAll(sortedFilms)
        filmsListView.redrawFilms(itemsCount, films.size, AdapterAction.ADD)

        sortedFilmsCount += sortedFilms.size
        if (sortedFilmsCount >= filmsPerPage) {
            sortedFilmsCount = 0
            filmsListView.setProgressBarState(IProgressState.StateType.LOADED)
        } else {
            getNextFilms()
        }
    }

    fun reset() {
        isLoading = false
        allFilms.clear()
        val itemsCount = activeFilms.size
        activeFilms.clear()
        sortedFilmsCount = 0
        filmsListView.redrawFilms(0, itemsCount, AdapterAction.DELETE)
    }

    fun applyFilter() {
        val itemsCount = activeFilms.size
        activeFilms.clear()
        filmsListView.redrawFilms(0, itemsCount, AdapterAction.DELETE)
        filmsListView.setProgressBarState(IProgressState.StateType.LOADING)
        addFilms(allFilms)
    }

    // true - film соотвествует критериям
    // false - фильм не соотвествует критериям
    private fun checkFilmForFilters(film: Film): Boolean {
        val applyList: ArrayMap<FiltersMenu.AppliedFilter, Boolean> = ArrayMap()

        val filters: ArrayMap<FiltersMenu.AppliedFilter, ArrayList<String>> = ArrayMap()
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
                FiltersMenu.AppliedFilter.COUNTRIES -> {
                    film.countries?.let {
                        for (country in film.countries!!) {
                            applyList[FiltersMenu.AppliedFilter.COUNTRIES] = country in filterEntry.value

                            if (applyList[FiltersMenu.AppliedFilter.COUNTRIES] == true) {
                                break
                            }
                        }
                    }
                }

                FiltersMenu.AppliedFilter.GENRES -> {
                    film.genres?.let {
                        for (genre in film.genres!!) {
                            applyList[FiltersMenu.AppliedFilter.GENRES] = genre in filterEntry.value

                            if (applyList[FiltersMenu.AppliedFilter.GENRES] == true) {
                                break
                            }
                        }
                    }
                }

                FiltersMenu.AppliedFilter.COUNTRIES_INVERTED -> {
                    film.countries?.let {
                        for (country in film.countries!!) {
                            applyList[FiltersMenu.AppliedFilter.COUNTRIES_INVERTED] = country !in filterEntry.value

                            if (applyList[FiltersMenu.AppliedFilter.COUNTRIES_INVERTED] == false) {
                                break
                            }
                        }
                    }
                }

                FiltersMenu.AppliedFilter.GENRES_INVERTED -> {
                    film.genres?.let {
                        for (genre in film.genres!!) {
                            applyList[FiltersMenu.AppliedFilter.GENRES_INVERTED] = genre !in filterEntry.value

                            if (applyList[FiltersMenu.AppliedFilter.GENRES_INVERTED] == false) {
                                break
                            }
                        }
                    }
                }

                FiltersMenu.AppliedFilter.RATING -> {
                    if (film.ratingIMDB != null && film.ratingIMDB?.isNotEmpty() == true) {
                        val min: Float = filterEntry.value[0].toFloat()
                        val max: Float = filterEntry.value[1].toFloat()
                        val filmRating: Float = film.ratingIMDB!!.toFloat()
                        applyList[FiltersMenu.AppliedFilter.RATING] = filmRating in min..max
                    } else {
                        applyList[FiltersMenu.AppliedFilter.RATING] = false
                    }
                }

                /*              AppliedFilter.YEAR -> {
                                  if (film.year != null && film.year?.isNotEmpty() == true) {
                                      val min: Float = filterEntry.value[0].toFloat()
                                      val max: Float = filterEntry.value[1].toFloat()
                                      val filmYear: Float = film.year!!.take(4).toFloat()

                                      applyList[AppliedFilter.YEAR] = filmYear in min..max
                                  } else {
                                      applyList[AppliedFilter.YEAR] = false
                                  }
                              }
              */
                FiltersMenu.AppliedFilter.TYPE -> {
                    if (filterEntry.value[0] == "Все") {
                        continue
                    }

                    film.type.let {
                        applyList[FiltersMenu.AppliedFilter.TYPE] = filterEntry.value[0].take(4) == film.type!!.take(4)
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
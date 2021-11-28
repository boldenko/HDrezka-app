package com.falcofemoralis.hdrezkaapp.presenters

import android.util.ArrayMap
import com.falcofemoralis.hdrezkaapp.constants.AppliedFilter
import com.falcofemoralis.hdrezkaapp.models.NewestFilmsModel
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SeriesUpdateItem
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmsListView
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.NewestFilmsView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewestFilmsPresenter(
    private val newestFilmsView: NewestFilmsView,
    private val filmsListView: FilmsListView
) : FilmsListPresenter.IFilmsList {
    var filmsListPresenter: FilmsListPresenter = FilmsListPresenter(filmsListView, newestFilmsView, this)
    private var appliedFilters: ArrayMap<AppliedFilter, String> = ArrayMap()
    private var currentPage: Int = 1 // newest film page

    fun initFilms() {
        appliedFilters[AppliedFilter.SORT] = NewestFilmsModel.SORTS[0]
        appliedFilters[AppliedFilter.TYPE] = NewestFilmsModel.TYPES[0]
        filmsListView.setFilms(filmsListPresenter.activeFilms)
        filmsListPresenter.getNextFilms()
    }

    override fun getMoreFilms(): ArrayList<Film> {
        return try {
            var films: ArrayList<Film> = ArrayList()

            if (appliedFilters[AppliedFilter.SORT]!! == NewestFilmsModel.SORTS[3]) {
                if (currentPage == 1) {
                    films = NewestFilmsModel.getNewFilms(appliedFilters[AppliedFilter.TYPE]!!)
                }
            } else if (appliedFilters[AppliedFilter.SORT]!! == NewestFilmsModel.SORTS[4]) {
                films = NewestFilmsModel.getAnnounce(currentPage, appliedFilters[AppliedFilter.TYPE]!!)
            } else {
                films = NewestFilmsModel.getNewestFilms(currentPage, appliedFilters[AppliedFilter.SORT]!!, appliedFilters[AppliedFilter.TYPE]!!)
            }

            currentPage++
            films
        } catch (e: Exception) {
            ExceptionHelper.catchException(e, newestFilmsView)
            ArrayList()
        }
    }

    fun setFilter(type: AppliedFilter, pos: Int) {
        appliedFilters[type] = when (type) {
            AppliedFilter.TYPE -> NewestFilmsModel.TYPES[pos]
            AppliedFilter.SORT -> NewestFilmsModel.SORTS[pos]
            else -> ""
        }
    }

    fun applyFilters() {
        filmsListPresenter.reset()
        filmsListPresenter.filmList.clear()
        currentPage = 1
        filmsListPresenter.getNextFilms()
    }

    fun initSeriesUpdates(callback: (seriesUpdates: LinkedHashMap<String, ArrayList<SeriesUpdateItem>>) -> Unit) {
        GlobalScope.launch {
            val seriesUpdates = NewestFilmsModel.getSeriesUpdates()

            if (seriesUpdates.size > 0) {
                withContext(Dispatchers.Main) {
                    callback(seriesUpdates)
                }
            }
        }
    }
}
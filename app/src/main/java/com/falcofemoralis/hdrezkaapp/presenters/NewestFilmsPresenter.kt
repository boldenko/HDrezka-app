package com.falcofemoralis.hdrezkaapp.presenters

import android.util.ArrayMap
import com.falcofemoralis.hdrezkaapp.models.NewestFilmsModel
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SeriesUpdateItem
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper
import com.falcofemoralis.hdrezkaapp.views.elements.FiltersMenu
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmsListView
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.NewestFilmsView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewestFilmsPresenter(
    private val newestFilmsView: NewestFilmsView,
    private val filmsListView: FilmsListView
) : FiltersMenu.IFilters, FilmsListPresenter.IFilmsList {
    var filmsListPresenter: FilmsListPresenter = FilmsListPresenter(filmsListView, newestFilmsView, this)
    private val sortFilters: ArrayList<String> = arrayListOf("last", "popular", "watching")
    private var currentPage: Int = 1 // newest film page
    private var sortFilter: String = sortFilters[0]

    fun initFilms() {
        filmsListView.setFilms(filmsListPresenter.activeFilms)
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

    override fun getMoreFilms(): ArrayList<Film> {
        return try {
            val films: ArrayList<Film> = NewestFilmsModel.getNewestFilms(currentPage, sortFilter)
            currentPage++
            films
        } catch (e: Exception) {
            ExceptionHelper.catchException(e, newestFilmsView)
            ArrayList()
        }
    }

    override fun onFilterCreated(appliedFilters: ArrayMap<FiltersMenu.AppliedFilter, Array<String?>>) {
        filmsListPresenter.appliedFilters = appliedFilters
    }

    override fun onApplyFilters(appliedFilters: ArrayMap<FiltersMenu.AppliedFilter, Array<String?>>) {
        val sortItem = appliedFilters[FiltersMenu.AppliedFilter.SORT]?.get(0)
        filmsListPresenter.appliedFilters = appliedFilters
        if (sortItem != null) {
            sortFilter = sortFilters[sortItem.toInt()]
            filmsListPresenter.reset()
            filmsListPresenter.filmList.clear()
            currentPage = 1
            filmsListPresenter.getNextFilms()
        } else {
            filmsListPresenter.applyFilter()
        }

        newestFilmsView.showFilterMsg()
    }
}
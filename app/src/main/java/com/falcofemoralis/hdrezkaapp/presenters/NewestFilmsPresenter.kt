package com.falcofemoralis.hdrezkaapp.presenters

import android.util.ArrayMap
import com.falcofemoralis.hdrezkaapp.models.NewestFilmsModel
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.views.elements.FiltersMenu
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmsListView
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.NewestFilmsView

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

    override fun getMoreFilms(): ArrayList<Film> {
        val films: ArrayList<Film> = NewestFilmsModel.getNewestFilms(currentPage, sortFilter)
        currentPage++
        return films
    }

    override fun onFilterCreated(appliedFilters: ArrayMap<FiltersMenu.AppliedFilter, Array<String?>>) {
        filmsListPresenter.appliedFilters = appliedFilters
    }

    override fun onApplyFilters(appliedFilters: ArrayMap<FiltersMenu.AppliedFilter, Array<String?>>) {
        val sortItem = appliedFilters[FiltersMenu.AppliedFilter.SORT]?.get(0)
        if (sortItem != null) {
            sortFilter = sortFilters[sortItem.toInt()]
            currentPage = 1
            filmsListPresenter.reset()
            filmsListPresenter.getNextFilms()
        } else {
            filmsListPresenter.applyFilter()
        }
    }
}
package com.falcofemoralis.hdrezkaapp.presenters

import com.falcofemoralis.hdrezkaapp.models.NewestFilmsModel
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.views.elements.FiltersMenu
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmsListView
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.NewestFilmsView

class NewestFilmsPresenter(private val newestFilmsView: NewestFilmsView, private val filmsListView: FilmsListView) : FiltersMenu.IFilter, FilmsListPresenter.IFilmsList {
    var filters: FiltersMenu = FiltersMenu(this)
    var filmsListPresenter: FilmsListPresenter = FilmsListPresenter(filmsListView, newestFilmsView, filters, this)
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

    override fun applyFilters() {
        val sortItem = filters.appliedFilters[FiltersMenu.AppliedFilter.SORT]?.get(0)
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
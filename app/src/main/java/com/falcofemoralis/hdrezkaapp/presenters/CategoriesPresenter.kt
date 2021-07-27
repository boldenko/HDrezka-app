package com.falcofemoralis.hdrezkaapp.presenters

import android.util.ArrayMap
import com.falcofemoralis.hdrezkaapp.models.CategoriesModel
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper.catchException
import com.falcofemoralis.hdrezkaapp.views.elements.FiltersMenu
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.CategoriesView
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmsListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoriesPresenter(private val categoriesView: CategoriesView, private val filmsListView: FilmsListView) : FiltersMenu.IFilters, FilmsListPresenter.IFilmsList {
    private var currentPage = 1
    private var selectedCategoryLink: String? = null

    var filmsListPresenter: FilmsListPresenter = FilmsListPresenter(filmsListView, categoriesView, this)
    var categories: ArrayMap<Pair<String, String>, ArrayList<Pair<String, String>>> = ArrayMap()
    var typesNames: ArrayList<String> = ArrayList()
    var genresNames: ArrayMap<String, ArrayList<String>> = ArrayMap()
    var yearsNames: ArrayList<String> = ArrayList()

    fun initCategories() {
        GlobalScope.launch {
            try {
                categories = CategoriesModel.getCategories()
                yearsNames = CategoriesModel.getYears()

                //  if (categories.size > 0 && yearsNames.size > 0) {
                for ((key, value) in categories) {
                    typesNames.add(key.first)

                    val list: ArrayList<String> = ArrayList()
                    for (genre in value) {
                        list.add(genre.first)
                    }

                    genresNames[key.first] = list
                }

                withContext(Dispatchers.Main) {
                    categoriesView.setCategories()
                    filmsListView.setFilms(filmsListPresenter.activeFilms)
                }
            } catch (e: Exception) {
                catchException(e, categoriesView)
                return@launch
            }
        }
    }

    fun setCategory(typePos: Int?, genrePos: Int?, yearPos: Int?) {
        var link = ""
        typePos?.let {
            link += categories.keyAt(typePos).second + "best/"

            genrePos?.let {
                link = categories.valueAt(typePos)[genrePos].second

                yearPos?.let {
                    val year: String = yearsNames[yearPos]
                    if (year != "за все время") {
                        link += "$year/"
                    }
                }
            }
        }

        link.let {
            selectedCategoryLink = link
            currentPage = 1
            filmsListPresenter.reset()
            filmsListPresenter.filmList.clear()
            categoriesView.showList()
            filmsListPresenter.setToken(link)
            filmsListPresenter.getNextFilms()
        }
    }

    override fun getMoreFilms(): ArrayList<Film> {
        var films: ArrayList<Film> = ArrayList()
        selectedCategoryLink?.let {
            films = CategoriesModel.getFilmsFromCategory(it, currentPage)
            currentPage++
        }
        return films
    }

    override fun onFilterCreated(appliedFilters: ArrayMap<FiltersMenu.AppliedFilter, Array<String?>>) {

    }

    override fun onApplyFilters(appliedFilters: ArrayMap<FiltersMenu.AppliedFilter, Array<String?>>) {
        filmsListPresenter.appliedFilters = appliedFilters
        filmsListPresenter.applyFilter()
    }
}
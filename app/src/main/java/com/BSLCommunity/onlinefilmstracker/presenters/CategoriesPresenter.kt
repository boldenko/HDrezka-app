package com.BSLCommunity.onlinefilmstracker.presenters

import android.util.ArrayMap
import com.BSLCommunity.onlinefilmstracker.interfaces.IProgressState
import com.BSLCommunity.onlinefilmstracker.models.CategoriesModel
import com.BSLCommunity.onlinefilmstracker.models.FilmModel
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.views.viewsInterface.CategoriesView
import com.BSLCommunity.onlinefilmstracker.views.viewsInterface.FilmsListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoriesPresenter(private val categoriesView: CategoriesView, private val filmsListView: FilmsListView) {
    private val FILMS_PER_PAGE: Int = 9

    private var curPage = 1
    private var loadedFilms: ArrayList<Film> = ArrayList()
    private var activeFilms: ArrayList<Film> = ArrayList()
    private var isLoading: Boolean = false
    private var selectedCategoryLink: String? = null

    fun initCategories() {
        GlobalScope.launch {
            val categories: ArrayMap<String, ArrayList<Pair<String, String>>> = CategoriesModel.getCategories()

            withContext(Dispatchers.Main) {
                categoriesView.setCategories(categories)
                filmsListView.setFilms(activeFilms)
            }
        }
    }

    fun setCategory(link: String?) {
        link?.let {
            selectedCategoryLink = link
            activeFilms.clear()
            loadedFilms.clear()
            categoriesView.showList()
            getNextFilms()
        }
    }

    fun getNextFilms() {
        if (isLoading) {
            return
        }

        isLoading = true
        filmsListView.setProgressBarState(IProgressState.StateType.LOADING)
        if (loadedFilms.size > 0) {
            FilmModel.getFilmsData(loadedFilms, FILMS_PER_PAGE, ::addFilms)
        } else {
            GlobalScope.launch {
                // if page is not empty
                selectedCategoryLink?.let {
                    loadedFilms.addAll(CategoriesModel.getFilmsFromCategory(it, curPage))
                    curPage++
                }

                if (loadedFilms.size > 0) {
                    FilmModel.getFilmsData(loadedFilms, FILMS_PER_PAGE, ::addFilms)
                }
            }
        }
    }

    private fun addFilms(films: ArrayList<Film>) {
        isLoading = false
        activeFilms.addAll(films)
        filmsListView.redrawFilms()
        filmsListView.setProgressBarState(IProgressState.StateType.LOADED)
    }
}
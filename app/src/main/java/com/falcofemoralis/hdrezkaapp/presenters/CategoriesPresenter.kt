package com.falcofemoralis.hdrezkaapp.presenters

import android.util.ArrayMap
import com.falcofemoralis.hdrezkaapp.interfaces.IProgressState
import com.falcofemoralis.hdrezkaapp.models.CategoriesModel
import com.falcofemoralis.hdrezkaapp.models.FilmModel
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper.catchException
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.CategoriesView
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmsListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException

class CategoriesPresenter(private val categoriesView: CategoriesView, private val filmsListView: FilmsListView) {
    private val FILMS_PER_PAGE: Int = 9

    private var curPage = 1
    private var loadedFilms: ArrayList<Film> = ArrayList()
    private var activeFilms: ArrayList<Film> = ArrayList()
    private var isLoading: Boolean = false
    private var selectedCategoryLink: String? = null

    var categories: ArrayMap<Pair<String, String>, ArrayList<Pair<String, String>>> = ArrayMap()
    var typesNames: ArrayList<String> = ArrayList()
    var genresNames: ArrayMap<String, ArrayList<String>> = ArrayMap()
    var yearsNames: ArrayList<String> = ArrayList()

    fun initCategories() {
        GlobalScope.launch {
            try {
                categories = CategoriesModel.getCategories()
                yearsNames = CategoriesModel.getYears()

                if (categories.size > 0 && yearsNames.size > 0) {
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
                        filmsListView.setFilms(activeFilms)
                    }
                } else {
                    catchException(HttpStatusException("no access", 500, ""), categoriesView)
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
            curPage = 1
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
            try {
                FilmModel.getFilmsData(loadedFilms, FILMS_PER_PAGE, ::addFilms)
            } catch (e: Exception) {
                catchException(e, categoriesView)
                isLoading = false
                return
            }
        } else {
            GlobalScope.launch {
                try {
                    // if page is not empty
                    selectedCategoryLink?.let {
                        loadedFilms.addAll(CategoriesModel.getFilmsFromCategory(it, curPage))
                        curPage++
                    }

                    if (loadedFilms.size > 0) {
                        FilmModel.getFilmsData(loadedFilms, FILMS_PER_PAGE, ::addFilms)
                    }
                } catch (e: Exception) {
                    if (e.message != "Empty list") {
                        catchException(e, categoriesView)

                    }
                    isLoading = false
                    withContext(Dispatchers.Main) {
                        filmsListView.setProgressBarState(IProgressState.StateType.LOADED)
                    }
                    return@launch
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
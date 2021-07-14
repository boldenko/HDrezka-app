package com.BSLCommunity.onlinefilmstracker.presenters

import com.BSLCommunity.onlinefilmstracker.interfaces.IProgressState
import com.BSLCommunity.onlinefilmstracker.models.FilmModel
import com.BSLCommunity.onlinefilmstracker.models.SearchModel
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.utils.ExceptionHelper.catchException
import com.BSLCommunity.onlinefilmstracker.views.viewsInterface.FilmsListView
import com.BSLCommunity.onlinefilmstracker.views.viewsInterface.SearchView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchPresenter(private val searchView: SearchView, private val filmsListView: FilmsListView) {
    private val FILMS_PER_PAGE = 9

    var activeSearchFilms: ArrayList<Film> = ArrayList()
    var activeListFilms: ArrayList<Film> = ArrayList()

    private var loadedListFilms: ArrayList<Film> = ArrayList()
    private var currentPage: Int = 1
    private var isLoading: Boolean = false
    private var query: String = ""

    fun initFilms() {
        filmsListView.setFilms(activeListFilms)
    }

    fun getFilms(text: String) {
        GlobalScope.launch {
            try {
                activeSearchFilms = SearchModel.getFilmsListByQuery(text)

                val searchFilms: ArrayList<String> = ArrayList()
                for (film in activeSearchFilms) {
                    searchFilms.add("${film.title} ${film.additionalInfo} ${film.ratingIMDB}")
                }

                withContext(Dispatchers.Main) {
                    searchView.redrawSearchFilms(searchFilms)
                }
            } catch (e: Exception) {
                catchException(e, searchView)
                return@launch
            }
        }
    }

    fun setQuery(text: String) {
        query = text
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
                if (loadedListFilms.size == 0) {
                    loadedListFilms = SearchModel.getFilmsFromSearchPage(query, currentPage)
                    currentPage++
                }
                FilmModel.getFilmsData(loadedListFilms, FILMS_PER_PAGE, ::addFilms)
            } catch (e: Exception) {
                catchException(e, searchView)
                isLoading = false
                return@launch
            }
        }
    }


    private fun addFilms(films: ArrayList<Film>) {
        isLoading = false
        activeListFilms.addAll(films)
        filmsListView.redrawFilms()
        filmsListView.setProgressBarState(IProgressState.StateType.LOADED)
    }
}
package com.BSLCommunity.onlinefilmstracker.presenters

import android.util.Log
import com.BSLCommunity.onlinefilmstracker.models.SearchModel
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.viewsInterface.SearchView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchPresenter(private val searchView: SearchView) {
    private var activeSearchFilms: ArrayList<Film> = ArrayList()
    private val activeListFilms: ArrayList<Film> = ArrayList()

    fun fetchFilms(text: String) {
        GlobalScope.launch {
            activeSearchFilms = SearchModel.getFilmsListByQuery(text)

            val searchFilms: ArrayList<String> = ArrayList()
            for (film in activeSearchFilms) {
                searchFilms.add("${film.title} ${film.additionalInfo} ${film.ratingIMDB}")
            }

            withContext(Dispatchers.Main) {
                searchView.redrawSearchFilms(searchFilms)
            }
        }
    }
}
package com.BSLCommunity.onlinefilmstracker.presenters

import com.BSLCommunity.onlinefilmstracker.models.NewestFilmsModel
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.viewsInterface.NewestFilmsView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.select.Elements

class NewestFilmsPresenter(private val newestFilmsView: NewestFilmsView) {
    private val FILMS_PER_PAGE: Int = 9

    private var currentPage: Int = 1
    private var isLoading: Boolean = false
    private val films: ArrayList<Film> = ArrayList()

    private lateinit var filmElements: Elements

    fun initFilms() {
        GlobalScope.launch {
            filmElements = NewestFilmsModel.getPage(currentPage++)
            addFilms()
        }
    }

    fun addFilms() {
        GlobalScope.launch {
            if (isLoading) {
                return@launch
            }

            isLoading = true
            var count = 0 // films added
            val elementsCopy: Elements = filmElements.clone()

            for (element in elementsCopy) {
                // if films loaded more than 9, break!
                if (count > (FILMS_PER_PAGE - 1)) {
                    break
                }

                // load film and add it to arrayList
                val film: Film? = NewestFilmsModel.getFilm(element)
                if (film != null) {
                    films.add(film)
                    count++
                }

                filmElements.removeAt(0)
            }
            isLoading = false

            // if films loaded more than 0, update adapter
            if (count > 0) {
                withContext(Dispatchers.Main) {
                    newestFilmsView.setFilms(films)
                }
            } else {
                // load films from next page
                filmElements.clear()
                filmElements = NewestFilmsModel.getPage(currentPage++)
                addFilms()
            }
        }
    }
}
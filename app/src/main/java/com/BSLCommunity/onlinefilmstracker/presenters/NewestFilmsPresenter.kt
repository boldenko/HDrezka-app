package com.BSLCommunity.onlinefilmstracker.presenters

import android.os.CountDownTimer
import android.util.ArrayMap
import android.util.Log
import com.BSLCommunity.onlinefilmstracker.constants.AppliedFilter
import com.BSLCommunity.onlinefilmstracker.models.NewestFilmsModel
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.viewsInterface.NewestFilmsView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.select.Elements
import java.util.*
import kotlin.collections.ArrayList

class NewestFilmsPresenter(private val newestFilmsView: NewestFilmsView) {
    private val FILMS_PER_PAGE: Int = 9

    private var currentPage: Int = 1 // newest film page
    private var isLoading: Boolean = false // loading condition
    private val allFilms: ArrayList<Film> = ArrayList() // all loaded films
    private val activeFilms: ArrayList<Film> = ArrayList() // current active films
    private var sortedFilmsCount: Int = 0 // current sorted films
    private var appliedFilters: ArrayMap<AppliedFilter, ArrayList<String>> = ArrayMap() // applied filters
    private var filmElements: Elements = Elements() // current films elements from newst page

    private val debugKey: String = "FILMS_DEBUG"
    private var timer: CountDownTimer? = null

    fun initFilms() {
        newestFilmsView.setFilms(activeFilms)
        getFilms()
    }

    fun getFilms() {
        GlobalScope.launch {
            if (isLoading) {
                Log.d(debugKey, "loading... return")
                return@launch
            }

            isLoading = true
            if (filmElements.size == 0) {
                filmElements = NewestFilmsModel.getPage(currentPage++)
            }

            // load FILMS_PER_PAGE (9) films
            val loadedFilms: ArrayList<Film> = ArrayList()
            for (filmElement in filmElements.clone()) {
                if (loadedFilms.size >= FILMS_PER_PAGE) {
                    Log.d(debugKey, "loaded ${loadedFilms.size} films. BREAK")
                    break
                }

                val film: Film? = NewestFilmsModel.getFilm(filmElement)
                if (film != null) {
                    Log.d(debugKey, "film: $film")
                    loadedFilms.add(film)
                }

                filmElements.removeAt(0)
            }
            isLoading = false

            allFilms.addAll(loadedFilms)
            addFilms(loadedFilms)
        }
    }

    private suspend fun addFilms(films: ArrayList<Film>) {
        val sortedFilms: ArrayList<Film> = ArrayList()

        for (film in films) {
            if (checkFilmForFilters(film)) {
                sortedFilms.add(film)
            }
        }

        withContext(Dispatchers.Main) {
            activeFilms.addAll(sortedFilms)
            Log.d(debugKey, "redraw ${sortedFilms.size} films")
            newestFilmsView.redrawFilms()
        }

        sortedFilmsCount += sortedFilms.size
        if (sortedFilmsCount >= FILMS_PER_PAGE) {
            sortedFilmsCount = 0
            withContext(Dispatchers.Main) {
                newestFilmsView.setProgressBarState(false)
                timer?.cancel()
            }
        } else {
            Log.d(debugKey, "sorted ${sortedFilmsCount}. Not enough")
            getFilms()
        }
    }

    fun setFilter(key: AppliedFilter, value: ArrayList<String>) {
        appliedFilters[key] = value
    }

    fun applyFilters() {
        Log.d(debugKey, "filers applied!")
        GlobalScope.launch {
            activeFilms.clear()
            withContext(Dispatchers.Main) {
                newestFilmsView.redrawFilms()
                newestFilmsView.setProgressBarState(true)

                timer = object : CountDownTimer(2000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {}

                    override fun onFinish() {
                        newestFilmsView.showStopToast()
                    }
                }
                timer?.start()
            }

            addFilms(allFilms)
        }
    }

    // true - film соотвествует критериям
    // false - фильм не соотвествует критериям
    private fun checkFilmForFilters(film: Film): Boolean {
        val applyList: ArrayMap<AppliedFilter, Boolean> = ArrayMap()

        for (filterEntry in appliedFilters) {
            when (filterEntry.key) {
                AppliedFilter.COUNTRIES -> {
                    for (country in film.countries) {
                        applyList[AppliedFilter.COUNTRIES] = country in filterEntry.value

                        if (applyList[AppliedFilter.COUNTRIES] == true) {
                            break
                        }
                    }
                }

                AppliedFilter.GENRES -> {
                    for (genre in film.genres) {
                        applyList[AppliedFilter.GENRES] = genre in filterEntry.value

                        if (applyList[AppliedFilter.GENRES] == true) {
                            break
                        }
                    }
                }

                AppliedFilter.RATING -> {
                    if (film.ratingIMDB?.isNotEmpty() == true) {
                        val min: Float = filterEntry.value[0].toFloat()
                        val max: Float = filterEntry.value[1].toFloat()
                        val filmRating: Float = film.ratingIMDB.toFloat()
                        applyList[AppliedFilter.RATING] = filmRating in min..max
                    }
                }

                AppliedFilter.YEAR -> {
                    val min: Float = filterEntry.value[0].toFloat()
                    val max: Float = filterEntry.value[1].toFloat()
                    val filmYear: Float = film.year.take(4).toFloat()

                    applyList[AppliedFilter.YEAR] = filmYear in min..max
                }

                AppliedFilter.TYPE -> {
                    if (filterEntry.value[0] == "Все") {
                        continue
                    }

                    applyList[AppliedFilter.YEAR] = filterEntry.value[0].take(4) == film.type.take(4)
                }
            }
        }

        var isApply = true
        for (applyItem in applyList) {
            isApply = isApply and applyItem.value
        }

        return isApply
    }
}
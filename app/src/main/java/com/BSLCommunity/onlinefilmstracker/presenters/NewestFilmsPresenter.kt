package com.BSLCommunity.onlinefilmstracker.presenters

import android.os.CountDownTimer
import android.util.ArrayMap
import android.util.Log
import com.BSLCommunity.onlinefilmstracker.constants.AppliedFilter
import com.BSLCommunity.onlinefilmstracker.models.FilmModel
import com.BSLCommunity.onlinefilmstracker.models.NewestFilmsModel
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.viewsInterface.NewestFilmsView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewestFilmsPresenter(private val newestFilmsView: NewestFilmsView) {
    private val FILMS_PER_PAGE: Int = 9
    private val STOP_TIME: Long = 8 // Seconds

    private var currentPage: Int = 1 // newest film page
    private var isLoading: Boolean = false // loading condition
    private var newestFilms: ArrayList<Film> = ArrayList() // current films links and their types from newest page
    private val allFilms: ArrayList<Film?> = ArrayList() // all loaded films
    private val activeFilms: ArrayList<Film> = ArrayList() // current active films
    private var sortedFilmsCount: Int = 0 // current sorted films
    private var appliedFilters: ArrayMap<AppliedFilter, ArrayList<String>> = ArrayMap() // applied filters
    private var timer: CountDownTimer? = null

    fun initFilms() {
        newestFilmsView.setFilms(activeFilms)
        getNextFilms()
    }

    fun getNextFilms() {
        if (isLoading) {
            return
        }

        isLoading = true
        if (newestFilms.size == 0) {
            GlobalScope.launch {
                newestFilms = NewestFilmsModel.getFilmsFromPage(currentPage++)
                getFilmsData()
            }
        } else {
            getFilmsData()
        }
    }

    private fun getFilmsData() {
        // load FILMS_PER_PAGE (18) films
        val filmsToLoad: ArrayList<Film> = ArrayList()
        for ((index, film) in (newestFilms.clone() as ArrayList<Film>).withIndex()) {
            filmsToLoad.add(film)
            newestFilms.removeAt(0)

            if (index == FILMS_PER_PAGE - 1) {
                break
            }
        }

        val loadedFilms = arrayOfNulls<Film?>(FILMS_PER_PAGE)
        var count = 0
        for ((index, film) in filmsToLoad.withIndex()) {
            GlobalScope.launch {
                loadedFilms[index] = FilmModel.getMainData(film)
                count++

                Log.d("FILM_DEBUG", "loaded ${film.title} on index $index")
                if (count >= FILMS_PER_PAGE) {
                    isLoading = false
                    val list: ArrayList<Film?> = ArrayList()
                    for (item in loadedFilms) {
                        list.add(item)
                    }
                    allFilms.addAll(list)
                    addFilms(list)
                }
            }
        }
    }

    private fun addFilms(films: ArrayList<Film?>) {
        GlobalScope.launch {
            val sortedFilms: ArrayList<Film> = ArrayList()

            for (film in films) {
                if (film != null) {
                    if (checkFilmForFilters(film)) {
                        sortedFilms.add(film)
                    }
                }
            }

            activeFilms.addAll(sortedFilms)

            // !!!
            withContext(Dispatchers.Main) {
                newestFilmsView.redrawFilms()
            }

            sortedFilmsCount += sortedFilms.size
            if (sortedFilmsCount >= FILMS_PER_PAGE) {
                sortedFilmsCount = 0

                // !!!
                withContext(Dispatchers.Main) {
                    newestFilmsView.setProgressBarState(false)
                }
                //   timer?.cancel()
            } else {
                getNextFilms()
            }
        }
    }

    fun setFilter(key: AppliedFilter, value: ArrayList<String>) {
        appliedFilters[key] = value
    }

    fun applyFilters() {
        activeFilms.clear()
        newestFilmsView.redrawFilms()
        newestFilmsView.setProgressBarState(true)
        /*   timer?.cancel()
           startTimer()*/
        addFilms(allFilms)
    }

/*    private fun startTimer() {
        timer = object : CountDownTimer(STOP_TIME * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                newestFilmsView.showStopToast()
                startTimer()
            }
        }
        timer?.start()
    }*/


    // true - film соотвествует критериям
    // false - фильм не соотвествует критериям
    private fun checkFilmForFilters(film: Film): Boolean {
        val applyList: ArrayMap<AppliedFilter, Boolean> = ArrayMap()

        for (filterEntry in appliedFilters) {
            when (filterEntry.key) {
                AppliedFilter.COUNTRIES -> {
                    film.countries?.let {
                        for (country in film.countries!!) {
                            applyList[AppliedFilter.COUNTRIES] = country in filterEntry.value

                            if (applyList[AppliedFilter.COUNTRIES] == true) {
                                break
                            }
                        }
                    }
                }

                AppliedFilter.GENRES -> {
                    film.genres?.let {
                        for (genre in film.genres!!) {
                            applyList[AppliedFilter.GENRES] = genre in filterEntry.value

                            if (applyList[AppliedFilter.GENRES] == true) {
                                break
                            }
                        }
                    }
                }

                AppliedFilter.COUNTRIES_INVERTED -> {
                    film.countries?.let {
                        for (country in film.countries!!) {
                            applyList[AppliedFilter.COUNTRIES_INVERTED] = country !in filterEntry.value

                            if (applyList[AppliedFilter.COUNTRIES_INVERTED] == false) {
                                break
                            }
                        }
                    }
                }

                AppliedFilter.GENRES_INVERTED -> {
                    film.genres?.let {
                        for (genre in film.genres!!) {
                            applyList[AppliedFilter.GENRES_INVERTED] = genre !in filterEntry.value

                            if (applyList[AppliedFilter.GENRES_INVERTED] == false) {
                                break
                            }
                        }
                    }
                }

                AppliedFilter.RATING -> {
                    if (film.ratingIMDB != null && film.ratingIMDB?.isNotEmpty() == true) {
                        val min: Float = filterEntry.value[0].toFloat()
                        val max: Float = filterEntry.value[1].toFloat()
                        val filmRating: Float = film.ratingIMDB!!.toFloat()
                        applyList[AppliedFilter.RATING] = filmRating in min..max
                    } else {
                        applyList[AppliedFilter.RATING] = false
                    }
                }

                AppliedFilter.YEAR -> {
                    if (film.year != null && film.year?.isNotEmpty() == true) {
                        val min: Float = filterEntry.value[0].toFloat()
                        val max: Float = filterEntry.value[1].toFloat()
                        val filmYear: Float = film.year!!.take(4).toFloat()

                        applyList[AppliedFilter.YEAR] = filmYear in min..max
                    } else {
                        applyList[AppliedFilter.YEAR] = false
                    }
                }

                AppliedFilter.TYPE -> {
                    if (filterEntry.value[0] == "Все") {
                        continue
                    }

                    applyList[AppliedFilter.TYPE] = filterEntry.value[0].take(4) == film.type.take(4)
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
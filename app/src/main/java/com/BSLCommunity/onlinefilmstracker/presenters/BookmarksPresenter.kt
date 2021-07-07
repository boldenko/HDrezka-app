package com.BSLCommunity.onlinefilmstracker.presenters

import com.BSLCommunity.onlinefilmstracker.constants.BookmarkFilterType
import com.BSLCommunity.onlinefilmstracker.models.BookmarksModel
import com.BSLCommunity.onlinefilmstracker.models.FilmModel
import com.BSLCommunity.onlinefilmstracker.objects.Bookmark
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.viewsInterface.BookmarksView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookmarksPresenter(private val bookmarksView: BookmarksView) {
    private val FILMS_PER_PAGE = 9

    var bookmarks: ArrayList<Bookmark>? = null
    val sortFilters: ArrayList<String> = ArrayList(arrayListOf("added", "year", "popular"))
    val showFilters: ArrayList<String> = ArrayList(arrayListOf("0", "1", "2", "3", "82"))
    private var selectedBookmark: Bookmark? = null
    private var selectedSortFilter: String? = null
    private var selectedShowFilter: String? = null
    private var curPage = 1
    private var loadedFilms: ArrayList<Film> = ArrayList()
    private var activeFilms: ArrayList<Film> = ArrayList()

    fun initBookmarks() {
        GlobalScope.launch {
            bookmarks = BookmarksModel.getBookmarksList()

            withContext(Dispatchers.Main) {
                if (bookmarks != null) {
                    val names: ArrayList<String> = ArrayList()
                    for (bookmark in bookmarks!!) {
                        names.add("${bookmark.name} (${bookmark.amount})")
                    }
                    bookmarksView.setBookmarksSpinner(names)
                    bookmarksView.setFilms(activeFilms)
                }
            }
        }
    }

    fun setBookmark(bookmark: Bookmark) {
        reset()
        selectedBookmark = bookmark
        getNextFilms()
    }

    private fun reset() {
        curPage = 1
        activeFilms.clear()
        loadedFilms.clear()
    }

    fun getNextFilms() {
        if (loadedFilms.size > 0) {
            setFilms()
        } else {
            GlobalScope.launch {
                // if page is not empty
                selectedBookmark?.let { loadedFilms.addAll(BookmarksModel.getFilmsFromBookmarkPage(it.link, curPage++, selectedSortFilter, selectedShowFilter)) }

                if (loadedFilms.size > 0) {
                    setFilms()
                } else {
                    withContext(Dispatchers.Main) {
                        bookmarksView.showMsg("Ничего не можем найти в закладках по данному запросу")
                    }
                }
            }
        }
    }

    private fun setFilms() {
        // get 9 films
        val filmsToLoad: ArrayList<Film> = ArrayList()
        for ((index, film) in (loadedFilms.clone() as ArrayList<Film>).withIndex()) {
            filmsToLoad.add(film)
            loadedFilms.removeAt(0)

            if (index == FILMS_PER_PAGE - 1) {
                break
            }
        }

        // load data for every films
        val films = arrayOfNulls<Film?>(FILMS_PER_PAGE)
        var count = 0
        for ((index, film) in filmsToLoad.withIndex()) {
            GlobalScope.launch {
                films[index] = FilmModel.getMainData(film)
                count++

                if (count >= filmsToLoad.size) {
                    val list: ArrayList<Film> = ArrayList()
                    for (item in films) {
                        if (item != null) {
                            list.add(item)
                        }
                    }
                    activeFilms.addAll(list)

                    // all films loaded
                    withContext(Dispatchers.Main) {
                        bookmarksView.redrawFilms()
                    }
                }
            }
        }
    }

    fun setFilter(filter: String, type: BookmarkFilterType) {
        when (type) {
            BookmarkFilterType.SORT -> selectedSortFilter = filter
            BookmarkFilterType.SHOW -> selectedShowFilter = filter
        }

        reset()
        getNextFilms()
    }
}
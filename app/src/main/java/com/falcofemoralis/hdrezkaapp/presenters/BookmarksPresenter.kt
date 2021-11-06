package com.falcofemoralis.hdrezkaapp.presenters

import android.util.Log
import com.falcofemoralis.hdrezkaapp.constants.AdapterAction
import com.falcofemoralis.hdrezkaapp.constants.BookmarkFilterType
import com.falcofemoralis.hdrezkaapp.interfaces.IMsg
import com.falcofemoralis.hdrezkaapp.interfaces.IProgressState
import com.falcofemoralis.hdrezkaapp.models.BookmarksModel
import com.falcofemoralis.hdrezkaapp.models.FilmModel
import com.falcofemoralis.hdrezkaapp.objects.Bookmark
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper.catchException
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.BookmarksView
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmsListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookmarksPresenter(private val bookmarksView: BookmarksView, private val filmsListView: FilmsListView) {
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
    private var isLoading: Boolean = false

    fun initBookmarks() {
        GlobalScope.launch {
            try {
                bookmarks = BookmarksModel.getBookmarksList()

                withContext(Dispatchers.Main) {
                    if (bookmarks != null && bookmarks!!.size > 0) {
                        val names: ArrayList<String> = ArrayList()
                        for (bookmark in bookmarks!!) {
                            names.add("${bookmark.name} (${bookmark.amount})")
                        }
                        bookmarksView.setBookmarksSpinner(names)
                        filmsListView.setFilms(activeFilms)
                    } else {
                        bookmarksView.setNoBookmarks()
                    }
                }
            } catch (e: Exception) {
                catchException(e, bookmarksView)
                return@launch
            }
        }
    }

    fun setBookmark(bookmark: Bookmark) {
        selectedBookmark = bookmark
        filmsListView.setProgressBarState(IProgressState.StateType.LOADING)
        reset()
        getNextFilms()
    }

    private fun reset() {
        curPage = 1
        val itemsCount = activeFilms.size
        activeFilms.clear()
        loadedFilms.clear()
        filmsListView.redrawFilms(0, itemsCount, AdapterAction.DELETE, ArrayList())
    }

    fun getNextFilms() {
        if (isLoading) {
            return
        }

        isLoading = true
        if (loadedFilms.size > 0) {
            try {
                FilmModel.getFilmsData(loadedFilms, FILMS_PER_PAGE, ::addFilms)
            } catch (e: Exception) {
                catchException(e, bookmarksView)
                isLoading = false
                return
            }
        } else {
            GlobalScope.launch {
                // if page is not empty
                selectedBookmark?.let {
                    try {
                        Log.d("BOOK_TEST", "start load " + loadedFilms.toString())
                        loadedFilms.addAll(BookmarksModel.getFilmsFromBookmarkPage(it.link, curPage++, selectedSortFilter, selectedShowFilter))
                        Log.d("BOOK_TEST", "end load " + loadedFilms.toString())
                    } catch (e: Exception) {
                        Log.d("BOOK_TEST", "fail load " + e)

                        catchException(e, bookmarksView)
                        isLoading = false
                        return@launch
                    }
                }

                if (loadedFilms.size > 0) {
                    try {
                        Log.d("BOOK_TEST", "start getting films data " + loadedFilms.toString())

                        FilmModel.getFilmsData(loadedFilms, FILMS_PER_PAGE, ::addFilms)

                        withContext(Dispatchers.Main) {
                            bookmarksView.hideMsg()
                        }
                    } catch (e: Exception) {
                        Log.d("BOOK_TEST", "fail getting films data " + e)

                        catchException(e, bookmarksView)
                        isLoading = false
                        return@launch
                    }
                } else if (curPage == 2) {
                    isLoading = false
                    withContext(Dispatchers.Main) {
                        bookmarksView.showMsg(IMsg.MsgType.NOTHING_FOUND)
                    }
                } else {
                    isLoading = false
                    withContext(Dispatchers.Main) {
                        filmsListView.setProgressBarState(IProgressState.StateType.LOADED)
                    }
                }
            }
        }
    }

    private fun addFilms(films: ArrayList<Film>) {
        Log.d("BOOK_TEST", "addFilms" + films.toString())

        isLoading = false
        val itemsCount = activeFilms.size
        activeFilms.addAll(films)
        filmsListView.redrawFilms(itemsCount, films.size, AdapterAction.ADD, films)
        filmsListView.setProgressBarState(IProgressState.StateType.LOADED)
    }

    fun setFilter(filter: String, type: BookmarkFilterType) {
        when (type) {
            BookmarkFilterType.SORT -> selectedSortFilter = filter
            BookmarkFilterType.SHOW -> selectedShowFilter = filter
        }

        reset()
        getNextFilms()
    }

    fun setMsg(type: IMsg.MsgType) {
        filmsListView.setProgressBarState(IProgressState.StateType.LOADED)
        bookmarksView.showMsg(type)
    }

    fun redrawBookmarks() {
        reset()
        bookmarks?.clear()
        initBookmarks()
    }

    fun redrawBookmarksFilms() {
        reset()
        getNextFilms()
    }
}
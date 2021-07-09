package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.constants.BookmarkFilterType
import com.BSLCommunity.onlinefilmstracker.models.UserModel
import com.BSLCommunity.onlinefilmstracker.presenters.BookmarksPresenter
import com.BSLCommunity.onlinefilmstracker.viewsInterface.BookmarksView
import com.BSLCommunity.onlinefilmstracker.viewsInterface.FilmListCallView
import com.chivorn.smartmaterialspinner.SmartMaterialSpinner

class BookmarksFragment : Fragment(), BookmarksView, FilmListCallView, AdapterView.OnItemSelectedListener {
    private lateinit var currentView: View
    private lateinit var bookmarksPresenter: BookmarksPresenter
    private lateinit var spinnersLayout: LinearLayout
    private lateinit var msgView: TextView
    private lateinit var filmsListFragment: FilmsListFragment
    private lateinit var progressBarSpinnerLayout: ProgressBar
    private var checked = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_bookmarks, container, false)
        Log.d("FRAGMENT_TEST", "book init")

        filmsListFragment = FilmsListFragment()
        filmsListFragment.setCallView(this)
        childFragmentManager.beginTransaction().replace(R.id.fragment_bookmarks_fcv_container, filmsListFragment).commit()

        msgView = currentView.findViewById(R.id.fragment_bookmarks_tv_not_auth)
        spinnersLayout = currentView.findViewById(R.id.fragment_bookmarks_ll_spinners_layout)
        spinnersLayout.visibility = View.GONE
        progressBarSpinnerLayout = currentView.findViewById(R.id.fragment_bookmarks_pb_spinner_loading)

        if (UserModel.isLoggedIn == true) {
            setSpinnerData(R.id.fragment_bookmarks_sp_sort)
            setSpinnerData(R.id.fragment_bookmarks_sp_show)
        }
        return currentView
    }

    override fun onFilmsListCreated() {
        bookmarksPresenter = BookmarksPresenter(this, filmsListFragment)

        if (UserModel.isLoggedIn == true) {
            bookmarksPresenter.initBookmarks()
        } else {
            progressBarSpinnerLayout.visibility = View.GONE
            bookmarksPresenter.setMsg(BookmarksPresenter.MsgType.NOT_AUTHORIZED)
        }

        super.onStart()
    }

    private fun setSpinnerData(spinnerId: Int) {
        val spinner: SmartMaterialSpinner<String> = currentView.findViewById(spinnerId)

        activity?.let {
            when (spinnerId) {
                R.id.fragment_bookmarks_sp_sort -> spinner.item = resources.getStringArray(R.array.sort).toMutableList()
                R.id.fragment_bookmarks_sp_show -> spinner.item = resources.getStringArray(R.array.show).toMutableList()
            }
            spinner.setSelection(0)
            spinner.onItemSelectedListener = this
        }
    }

    override fun setBookmarksSpinner(bookmarksNames: ArrayList<String>) {
        currentView.findViewById<ProgressBar>(R.id.fragment_bookmarks_pb_spinner_loading).visibility = View.GONE
        spinnersLayout.visibility = View.VISIBLE

        activity?.let {
            val bookmarksSpinner: SmartMaterialSpinner<String> = currentView.findViewById(R.id.fragment_bookmarks_sp_list)
            bookmarksSpinner.item = bookmarksNames
            bookmarksSpinner.onItemSelectedListener = this
            bookmarksSpinner.setSelection(0)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        when (parent.id) {
            R.id.fragment_bookmarks_sp_sort -> {
                if (checked > 2) {
                    Log.d("DEEBUG", "sort selected")
                    bookmarksPresenter.setFilter(bookmarksPresenter.sortFilters[position], BookmarkFilterType.SORT)
                } else {
                    checked++
                }
            }
            R.id.fragment_bookmarks_sp_show -> {
                if (checked > 1) {
                    Log.d("DEEBUG", "show selected")
                    bookmarksPresenter.setFilter(bookmarksPresenter.showFilters[position], BookmarkFilterType.SHOW)
                } else {
                    checked++
                }
            }
            R.id.fragment_bookmarks_sp_list -> {
                Log.d("DEEBUG", "list selected")
                bookmarksPresenter.bookmarks?.get(position)?.let { bookmarksPresenter.setBookmark(it) }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun showMsg(type: BookmarksPresenter.MsgType) {
        msgView.visibility = View.VISIBLE

        when (type) {
            BookmarksPresenter.MsgType.NOT_AUTHORIZED -> msgView.text = "Данный раздел доступен только зарегистрированным пользователям"
            BookmarksPresenter.MsgType.NOTHING_FOUND -> msgView.text = "Ничего не можем найти в закладках по данному запросу"
        }
    }

    override fun triggerEnd() {
        bookmarksPresenter.getNextFilms()
    }
}
package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.constants.BookmarkFilterType
import com.BSLCommunity.onlinefilmstracker.models.UserModel
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.presenters.BookmarksPresenter
import com.BSLCommunity.onlinefilmstracker.views.OnFragmentInteractionListener
import com.BSLCommunity.onlinefilmstracker.viewsInterface.BookmarksView
import com.chivorn.smartmaterialspinner.SmartMaterialSpinner

class BookmarksFragment : Fragment(), BookmarksView, AdapterView.OnItemSelectedListener {
    private val FILMS_PER_ROW: Int = 3

    private lateinit var currentView: View
    private lateinit var bookmarksPresenter: BookmarksPresenter
    private lateinit var viewList: RecyclerView
    private lateinit var fragmentListener: OnFragmentInteractionListener
    private lateinit var scrollView: NestedScrollView
    private lateinit var progressBar: ProgressBar
    private lateinit var spinnersLayout: LinearLayout
    private lateinit var msgView: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentListener = context as OnFragmentInteractionListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_bookmarks, container, false)
        bookmarksPresenter = BookmarksPresenter(this)

        msgView = currentView.findViewById(R.id.fragment_bookmarks_tv_not_auth)

        spinnersLayout = currentView.findViewById(R.id.fragment_bookmarks_ll_spinners_layout)
        spinnersLayout.visibility = View.GONE

        viewList = currentView.findViewById(R.id.fragment_bookmarks_rv_films)
        viewList.layoutManager = GridLayoutManager(context, FILMS_PER_ROW)

        progressBar = currentView.findViewById(R.id.fragment_bookmarks_pb_data_loading)
        scrollView = currentView.findViewById(R.id.fragment_bookmarks_nsv_films)
        scrollView.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener {
            override fun onScrollChange(v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
                val view = scrollView.getChildAt(scrollView.childCount - 1)
                val diff = view.bottom - (scrollView.height + scrollView.scrollY)

                if (diff == 0) {
                    progressBar.visibility = View.VISIBLE
                    bookmarksPresenter.getNextFilms()
                }
            }
        })

        if (UserModel.isLoggedIn == true) {
            setSpinnerData(R.id.fragment_bookmarks_sp_sort)
            setSpinnerData(R.id.fragment_bookmarks_sp_show)

            bookmarksPresenter.initBookmarks()
        } else {
            showMsg("Данный раздел доступен только зарегистрированным пользователям")
            viewList.visibility = View.GONE
            progressBar.visibility = View.GONE
        }
        return currentView
    }

    private fun setSpinnerData(spinnerId: Int) {
        val spinner: SmartMaterialSpinner<String> = currentView.findViewById(spinnerId)

        activity?.let {
            when (spinnerId) {
                R.id.fragment_bookmarks_sp_sort -> spinner.item = resources.getStringArray(R.array.sort).toMutableList()
                R.id.fragment_bookmarks_sp_show -> spinner.item = resources.getStringArray(R.array.show).toMutableList()
            }
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
                bookmarksPresenter.setFilter(bookmarksPresenter.sortFilters[position], BookmarkFilterType.SORT)
            }
            R.id.fragment_bookmarks_sp_show -> {
                bookmarksPresenter.setFilter(bookmarksPresenter.showFilters[position], BookmarkFilterType.SHOW)
            }
            R.id.fragment_bookmarks_sp_list -> {
                bookmarksPresenter.bookmarks?.get(position)?.let { bookmarksPresenter.setBookmark(it) }
            }
        }
        progressBar.visibility = View.VISIBLE
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun setFilms(films: ArrayList<Film>) {
        viewList.adapter = context?.let { FilmsListRecyclerViewAdapter(it, films, ::openFilm) }
    }

    override fun redrawFilms() {
        msgView.visibility = View.GONE
        progressBar.visibility = View.GONE
        viewList.adapter?.notifyDataSetChanged()
    }

    override fun showMsg(msg: String) {
        msgView.visibility = View.VISIBLE
        msgView.text = msg
    }

    private fun openFilm(film: Film) {
        val data = Bundle()
        data.putSerializable("film", film)

        fragmentListener.onFragmentInteraction(
            FilmFragment(), OnFragmentInteractionListener.Action.NEXT_FRAGMENT_HIDE, data, null
        )
    }
}
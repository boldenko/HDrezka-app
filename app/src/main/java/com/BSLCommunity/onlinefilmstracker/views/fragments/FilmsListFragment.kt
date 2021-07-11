package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.utils.FragmentOpener
import com.BSLCommunity.onlinefilmstracker.views.interfaces.OnFragmentInteractionListener
import com.BSLCommunity.onlinefilmstracker.views.adapters.FilmsListRecyclerViewAdapter
import com.BSLCommunity.onlinefilmstracker.viewsInterface.FilmListCallView
import com.BSLCommunity.onlinefilmstracker.viewsInterface.FilmsListView


open class FilmsListFragment : Fragment(), FilmsListView {
    private val FILMS_PER_ROW: Int = 3

    private lateinit var currentView: View
    private lateinit var viewList: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollView: NestedScrollView
    private lateinit var fragmentListener: OnFragmentInteractionListener
    private lateinit var callView: FilmListCallView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentListener = context as OnFragmentInteractionListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        currentView = inflater.inflate(R.layout.fragment_films_list, container, false)
        Log.d("FRAGMENT_TEST", "film list init")

        progressBar = currentView.findViewById(R.id.fragment_films_list_pb_data_loading)

        viewList = currentView.findViewById(R.id.fragment_films_list_rv_films)
        viewList.layoutManager = GridLayoutManager(context, FILMS_PER_ROW)

        scrollView = currentView.findViewById(R.id.fragment_films_list_nsv_films)
        scrollView.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener {
            override fun onScrollChange(v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
                val view = scrollView.getChildAt(scrollView.childCount - 1)
                val diff = view.bottom - (scrollView.height + scrollView.scrollY)

                if (diff == 0) {
                    setProgressBarState(true)
                    callView.triggerEnd()
                }
            }
        })

        callView.onFilmsListCreated()

        return currentView
    }

    fun setCallView(cv: FilmListCallView) {
        callView = cv
    }

    override fun setFilms(films: ArrayList<Film>) {
        viewList.adapter = context?.let { FilmsListRecyclerViewAdapter(it, films, ::listCallback) }
    }

    override fun redrawFilms() {
        viewList.adapter?.notifyDataSetChanged()
    }

    override fun setProgressBarState(state: Boolean) {
        if (state) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
    }

    fun listCallback(film: Film) {
        FragmentOpener.openFilm(film, this, fragmentListener)
    }
}
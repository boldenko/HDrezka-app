package com.falcofemoralis.hdrezkaapp.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.interfaces.OnFragmentInteractionListener
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import com.falcofemoralis.hdrezkaapp.presenters.SeriesUpdatesPresenter
import com.falcofemoralis.hdrezkaapp.utils.FragmentOpener
import com.falcofemoralis.hdrezkaapp.views.adapters.FilmsListRecyclerViewAdapter
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.SeriesUpdatesView

class SeriesUpdatesFragment : Fragment(), SeriesUpdatesView {
    private var seriesUpdatesPresenter: SeriesUpdatesPresenter? = null
    private lateinit var currentView: View
    private lateinit var fragmentListener: OnFragmentInteractionListener
    private lateinit var scrollView: NestedScrollView
    private lateinit var progressBar: ProgressBar

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentListener = context as OnFragmentInteractionListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        currentView = inflater.inflate(R.layout.fragment_series_updates, container, false)

        scrollView = currentView.findViewById(R.id.fragment_series_updates_scroll)
        scrollView.visibility = View.GONE
        progressBar = currentView.findViewById(R.id.fragment_series_updates_pb_data_loading)

        if (seriesUpdatesPresenter == null) {
            seriesUpdatesPresenter = SeriesUpdatesPresenter(this)
        }
        // download updates?
        seriesUpdatesPresenter?.initList()

        return currentView
    }

    override fun setList(films: LinkedHashMap<String, ArrayList<Film>>) {
        val container: LinearLayout = currentView.findViewById(R.id.fragment_series_updates_ll_films)

        fun listCallback(film: Film) {
            FragmentOpener.openWithData(this, fragmentListener, film, "film")
        }

        for ((date, filmsList) in films) {
            val layout = layoutInflater.inflate(R.layout.inflate_actor_career_layout, null)

            layout.findViewById<TextView>(R.id.career_header).text = date
            val recyclerView: RecyclerView = layout.findViewById(R.id.career_films)
            recyclerView.layoutManager = SettingsData.filmsInRow?.let { GridLayoutManager(requireContext(), it) }
            recyclerView.adapter = FilmsListRecyclerViewAdapter(requireContext(), filmsList, ::listCallback)

            container.addView(layout)

        }

        scrollView.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    override fun showConnectionError(type: IConnection.ErrorType, errorText: String) {
        //
    }

    fun initUserUpdatesData(_context: Context, updateNotifyBadge: (n: Int) -> Unit, createNotifyBtn: () -> Unit) {
        if (seriesUpdatesPresenter == null) {
            seriesUpdatesPresenter = SeriesUpdatesPresenter(this)
        }

        seriesUpdatesPresenter?.initUserUpdatesData(_context, updateNotifyBadge, createNotifyBtn)
    }
}
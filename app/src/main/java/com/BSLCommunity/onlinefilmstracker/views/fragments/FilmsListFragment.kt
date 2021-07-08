package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.viewsInterface.FilmsListView


open class FilmsListFragment : Fragment(), FilmsListView {
    private lateinit var currentView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        currentView = inflater.inflate(R.layout.fragment_films_list, container, false)
        return currentView
    }

    override fun setFilms(films: ArrayList<Film>) {
        TODO("Not yet implemented")
    }

    override fun redrawFilms() {
        TODO("Not yet implemented")
    }

    override fun setProgressBarState(state: Boolean) {
        TODO("Not yet implemented")
    }
}
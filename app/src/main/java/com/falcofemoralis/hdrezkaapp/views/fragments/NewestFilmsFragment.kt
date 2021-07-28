package com.falcofemoralis.hdrezkaapp.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.presenters.NewestFilmsPresenter
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper
import com.falcofemoralis.hdrezkaapp.views.elements.FiltersMenu
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmListCallView
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.NewestFilmsView

class NewestFilmsFragment : Fragment(), NewestFilmsView, FilmListCallView {
    private lateinit var currentView: View
    private lateinit var newestFilmsPresenter: NewestFilmsPresenter
    private lateinit var filmsListFragment: FilmsListFragment
    private lateinit var filtersMenu: FiltersMenu

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_newest_films, container, false) as LinearLayout

        filmsListFragment = FilmsListFragment()
        filmsListFragment.setCallView(this)
        childFragmentManager.beginTransaction().replace(R.id.fragment_newest_films_fcv_container, filmsListFragment).commit()

        return currentView
    }

    override fun onFilmsListCreated() {
        newestFilmsPresenter = NewestFilmsPresenter(this, filmsListFragment)
        filtersMenu = FiltersMenu(newestFilmsPresenter, requireActivity(), currentView.findViewById(R.id.fragment_newest_films_bt_filters))
        filtersMenu
            .createDialogFilter(FiltersMenu.AppliedFilter.COUNTRIES, resources.getStringArray(R.array.countries))
            .createDialogFilter(FiltersMenu.AppliedFilter.GENRES, resources.getStringArray(R.array.genres))
            .createDialogFilter(FiltersMenu.AppliedFilter.COUNTRIES_INVERTED, resources.getStringArray(R.array.countries))
            .createDialogFilter(FiltersMenu.AppliedFilter.GENRES_INVERTED, resources.getStringArray(R.array.genres))
            .createRatingFilter()
            .createTypesFilter(true)
            .createSortFilter()
            .apply()
        newestFilmsPresenter.initFilms()
        requireActivity()
    }

    override fun triggerEnd() {
        newestFilmsPresenter.filmsListPresenter.getNextFilms()
    }

    override fun showConnectionError(type: IConnection.ErrorType) {
        ExceptionHelper.showToastError(requireContext(), type)
    }
}
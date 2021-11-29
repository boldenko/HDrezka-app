package com.falcofemoralis.hdrezkaapp.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.constants.AppliedFilter
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.interfaces.OnFragmentInteractionListener
import com.falcofemoralis.hdrezkaapp.presenters.NewestFilmsPresenter
import com.falcofemoralis.hdrezkaapp.utils.DialogManager
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmListCallView
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.NewestFilmsView

class NewestFilmsFragment : Fragment(), NewestFilmsView, FilmListCallView {
    private lateinit var currentView: View
    private lateinit var newestFilmsPresenter: NewestFilmsPresenter
    private lateinit var filmsListFragment: FilmsListFragment
    private lateinit var fragmentListener: OnFragmentInteractionListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentListener = context as OnFragmentInteractionListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_newest_films, container, false) as LinearLayout

        initFilmsList()

        // initSeriesUpdatesBtn()

        return currentView
    }

    private fun initFilmsList() {
        filmsListFragment = FilmsListFragment()
        filmsListFragment.setCallView(this)
        childFragmentManager.beginTransaction().replace(R.id.fragment_newest_films_fcv_container, filmsListFragment).commit()
    }

    override fun onFilmsListCreated() {
        newestFilmsPresenter = NewestFilmsPresenter(this, filmsListFragment)
        newestFilmsPresenter.initFilms()

        initFiltersBtn()
    }

    override fun onFilmsListDataInit() {
    }

    override fun triggerEnd() {
        newestFilmsPresenter.filmsListPresenter.getNextFilms()
    }

    private fun initFiltersBtn() {
        val builder = DialogManager.getDialog(requireContext(), null)
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_newest_filters, null)

        val typeGroupView: RadioGroup = dialogView.findViewById(R.id.film_types)
        typeGroupView.setOnCheckedChangeListener { group, checkedId ->
            run {
                if (checkedId != -1) {
                    val pos: Int = when (checkedId) {
                        R.id.type_all -> 0
                        R.id.type_films -> 1
                        R.id.type_serials -> 2
                        R.id.type_multfilms -> 3
                        R.id.type_anime -> 4
                        else -> 0
                    }
                    newestFilmsPresenter.setFilter(AppliedFilter.TYPE, pos)
                } else {
                    group.findViewById<RadioButton>(R.id.type_all).isChecked = true
                }
            }
        }

        val sortGroupView: RadioGroup = dialogView.findViewById(R.id.film_sort)
        sortGroupView.setOnCheckedChangeListener { group, checkedId ->
            run {
                if (checkedId != -1) {
                    val pos: Int = when (checkedId) {
                        R.id.sort_last -> 0
                        R.id.sort_popular -> 1
                        R.id.sort_now -> 2
                        R.id.sort_new -> 3
                        R.id.sort_announce -> 4
                        else -> 0
                    }
                    newestFilmsPresenter.setFilter(AppliedFilter.SORT, pos)
                } else {
                    group.findViewById<RadioButton>(R.id.sort_last).isChecked = true
                }
            }
        }

        builder.setView(dialogView)
        builder.setPositiveButton(R.string.ok_text) { d, i ->
            newestFilmsPresenter.applyFilters()
            d.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { d, i ->
            d.dismiss()
        }

        val d = builder.create()
        currentView.findViewById<TextView>(R.id.fragment_newest_films_tv_filters).setOnClickListener {
            d.show()
        }
    }

    override fun showConnectionError(type: IConnection.ErrorType, errorText: String) {
        try {
            if (context != null) {
                ExceptionHelper.showToastError(requireContext(), type, errorText)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
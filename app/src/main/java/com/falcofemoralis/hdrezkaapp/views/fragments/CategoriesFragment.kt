package com.falcofemoralis.hdrezkaapp.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.chivorn.smartmaterialspinner.SmartMaterialSpinner
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.presenters.CategoriesPresenter
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper
import com.falcofemoralis.hdrezkaapp.views.elements.FiltersMenu
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.CategoriesView
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmListCallView


class CategoriesFragment : Fragment(), CategoriesView, AdapterView.OnItemSelectedListener, FilmListCallView {
    private lateinit var currentView: View
    private lateinit var categoriesPresenter: CategoriesPresenter
    private lateinit var typesSpinner: SmartMaterialSpinner<String>
    private lateinit var genresSpinner: SmartMaterialSpinner<String>
    private lateinit var yearsSpinner: SmartMaterialSpinner<String>
    private lateinit var filmsListFragment: FilmsListFragment
    private lateinit var filtersMenu: FiltersMenu
    private lateinit var filterMenuBtn: TextView

    private var typePos: Int? = null
    private var genrePos: Int? = null
    private var yearPos: Int? = null

    enum class SpinnerState {
        UNSET,
        FREE,
        AWAIT,
    }

    private var genresSpinnerState: SpinnerState = SpinnerState.FREE
    private var yearsSpinnerState: SpinnerState = SpinnerState.FREE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_categories, container, false)

        filmsListFragment = FilmsListFragment()
        filmsListFragment.setCallView(this)
        childFragmentManager.beginTransaction().replace(R.id.fragment_categories_fcv_container, filmsListFragment).commit()

        typesSpinner = currentView.findViewById(R.id.fragment_categories_sp_types)
        genresSpinner = currentView.findViewById(R.id.fragment_categories_sp_genres)
        yearsSpinner = currentView.findViewById(R.id.fragment_categories_sp_years)

        genresSpinner.onItemSelectedListener = this
        yearsSpinner.onItemSelectedListener = this

        return currentView
    }

    override fun onFilmsListCreated() {
        categoriesPresenter = CategoriesPresenter(this, filmsListFragment)
        filterMenuBtn = currentView.findViewById(R.id.fragment_categories_films_bt_filters)
        filtersMenu = FiltersMenu(categoriesPresenter, requireActivity(), filterMenuBtn)
        filtersMenu
            .createDialogFilter(FiltersMenu.AppliedFilter.COUNTRIES, resources.getStringArray(R.array.countries))
            .createDialogFilter(FiltersMenu.AppliedFilter.COUNTRIES_INVERTED, resources.getStringArray(R.array.countries))
            .apply()

        categoriesPresenter.initCategories()
    }

    override fun setCategories() {
        typesSpinner.item = categoriesPresenter.typesNames
        typesSpinner.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        when (parent.id) {
            R.id.fragment_categories_sp_types -> {
                typePos = position
                genresSpinnerState = SpinnerState.AWAIT
                yearsSpinnerState = SpinnerState.AWAIT
                genresSpinner.setSelection(0)
                yearsSpinner.setSelection(0)
                genresSpinner.item = categoriesPresenter.genresNames[categoriesPresenter.typesNames[position]]
                yearsSpinner.item = categoriesPresenter.yearsNames
                categoriesPresenter.setCategory(typePos, genrePos, yearPos)
            }
            R.id.fragment_categories_sp_genres -> {
                if (genresSpinnerState == SpinnerState.FREE) {
                    genrePos = position
                    categoriesPresenter.setCategory(typePos, genrePos, yearPos)
                } else if (genresSpinnerState == SpinnerState.AWAIT) {
                    genresSpinnerState = SpinnerState.FREE
                }
            }
            R.id.fragment_categories_sp_years -> {
                if (yearsSpinnerState == SpinnerState.FREE) {
                    yearPos = position
                    categoriesPresenter.setCategory(typePos, genrePos, yearPos)
                } else if (yearsSpinnerState == SpinnerState.AWAIT) {
                    yearsSpinnerState = SpinnerState.FREE
                }
            }
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun triggerEnd() {
        categoriesPresenter.filmsListPresenter.getNextFilms()
    }

    override fun showList() {
        currentView.findViewById<TextView>(R.id.fragment_categories_tv_msg).visibility = View.GONE
        currentView.findViewById<FragmentContainerView>(R.id.fragment_categories_fcv_container).visibility = View.VISIBLE
    }

    override fun showConnectionError(type: IConnection.ErrorType) {
        ExceptionHelper.showToastError(requireContext(), type)
    }
}
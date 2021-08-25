package com.falcofemoralis.hdrezkaapp.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
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
    private lateinit var filmsListFragment: FilmsListFragment
    private lateinit var filtersMenu: FiltersMenu
    private lateinit var filterMenuBtn: TextView

    private var typePos: Int? = null
    private var genrePos: Int? = null
    private var yearPos: Int? = null

    enum class SpinnerState {
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
        filterMenuBtn = currentView.findViewById(R.id.fragment_categories_films_bt_filters)
        filterMenuBtn.visibility = View.GONE

        return currentView
    }

    override fun onFilmsListCreated() {
        categoriesPresenter = CategoriesPresenter(this, filmsListFragment)
        filtersMenu = FiltersMenu(categoriesPresenter, requireActivity(), filterMenuBtn)
        filtersMenu
            .createDialogFilter(FiltersMenu.AppliedFilter.COUNTRIES, resources.getStringArray(R.array.countries))
            .createDialogFilter(FiltersMenu.AppliedFilter.COUNTRIES_INVERTED, resources.getStringArray(R.array.countries))
            .createSpinnerFilter(FiltersMenu.AppliedFilter.SPINNER_GENRES)
            .createSpinnerFilter(FiltersMenu.AppliedFilter.SPINNER_YEARS)
            .removeActionButtons()
            .apply()

        filtersMenu.genresSpinnerView?.onItemSelectedListener = this
        filtersMenu.yearsSpinnerView?.onItemSelectedListener = this

        categoriesPresenter.initCategories()
    }

    override fun dataInited() {}

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
                filtersMenu.genresSpinnerView?.setSelection(0)
                filtersMenu.yearsSpinnerView?.setSelection(0)
                filtersMenu.genresSpinnerView?.item = categoriesPresenter.genresNames[categoriesPresenter.typesNames[position]]
                filtersMenu.yearsSpinnerView?.item = categoriesPresenter.yearsNames
                categoriesPresenter.setCategory(typePos, genrePos, yearPos)

                if (filterMenuBtn.visibility == View.GONE) {
                    typesSpinner.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f)
                    filterMenuBtn.visibility = View.VISIBLE
                }
            }
            R.id.sp_genres -> {
                if (genresSpinnerState == SpinnerState.FREE) {
                    genrePos = position
                    categoriesPresenter.setCategory(typePos, genrePos, yearPos)
                } else if (genresSpinnerState == SpinnerState.AWAIT) {
                    genresSpinnerState = SpinnerState.FREE
                }
            }
            R.id.sp_years -> {
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

    override fun showConnectionError(type: IConnection.ErrorType, errorText: String) {
        ExceptionHelper.showToastError(requireContext(), type, errorText)
    }
}
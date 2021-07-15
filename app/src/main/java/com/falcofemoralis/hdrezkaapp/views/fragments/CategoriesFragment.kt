package com.falcofemoralis.hdrezkaapp.views.fragments

import android.os.Bundle
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.presenters.CategoriesPresenter
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.CategoriesView
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmListCallView
import com.chivorn.smartmaterialspinner.SmartMaterialSpinner

class CategoriesFragment : Fragment(), CategoriesView, AdapterView.OnItemSelectedListener, FilmListCallView {
    private lateinit var currentView: View
    private lateinit var categoriesPresenter: CategoriesPresenter
    private lateinit var typesSpinner: SmartMaterialSpinner<String>
    private lateinit var genresSpinner: SmartMaterialSpinner<String>
    private lateinit var filmsListFragment: FilmsListFragment
    private var categories: ArrayMap<String, ArrayList<Pair<String, String>>>? = null
    private var typesNames: ArrayList<String>? = null
    private var genresNames: ArrayMap<String, ArrayList<String>>? = null
    private var typePos: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        currentView = inflater.inflate(R.layout.fragment_categories, container, false)

        filmsListFragment = FilmsListFragment()
        filmsListFragment.setCallView(this)
        childFragmentManager.beginTransaction().replace(R.id.fragment_categories_fcv_container, filmsListFragment).commit()

        typesSpinner = currentView.findViewById(R.id.fragment_categories_sp_types)
        genresSpinner = currentView.findViewById(R.id.fragment_categories_sp_genres)

        return currentView
    }

    override fun onFilmsListCreated() {
        categoriesPresenter = CategoriesPresenter(this, filmsListFragment)
        categoriesPresenter.initCategories()
    }

    override fun setCategories(categories: ArrayMap<String, ArrayList<Pair<String, String>>>) {
        this.categories = categories
        typesNames = ArrayList()
        genresNames = ArrayMap()
        for ((key, value) in categories) {
            typesNames!!.add(key)

            val list: ArrayList<String> = ArrayList()
            for (genre in value) {
                list.add(genre.first)
            }

            genresNames!![key] = list
        }

        typesSpinner.item = typesNames
        typesSpinner.onItemSelectedListener = this
        typesSpinner.setSelection(0)
        genresSpinner.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        when (parent.id) {
            R.id.fragment_categories_sp_types -> {
                genresSpinner.item = genresNames!![typesNames!![position]]
                typePos = position
            }
            R.id.fragment_categories_sp_genres -> {
                val catLink: String? = categories?.get(typesNames?.get(typePos))?.get(position)?.second
                categoriesPresenter.setCategory(catLink)
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun triggerEnd() {
        categoriesPresenter.getNextFilms()
    }

    override fun showList() {
        currentView.findViewById<TextView>(R.id.fragment_categories_tv_msg).visibility = View.GONE
        currentView.findViewById<FragmentContainerView>(R.id.fragment_categories_fcv_container).visibility = View.VISIBLE
    }

    override fun showConnectionError(type: IConnection.ErrorType) {
        ExceptionHelper.showToastError(requireContext(), type)
    }
}
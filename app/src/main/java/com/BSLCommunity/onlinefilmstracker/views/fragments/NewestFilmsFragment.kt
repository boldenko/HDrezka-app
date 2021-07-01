package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.os.Bundle
import android.util.ArrayMap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.constants.AppliedFilter
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.presenters.NewestFilmsPresenter
import com.BSLCommunity.onlinefilmstracker.viewsInterface.NewestFilmsView


class NewestFilmsFragment : Fragment(), NewestFilmsView {
    private val FILMS_PER_ROW: Int = 3

    private lateinit var newestFilmsPresenter: NewestFilmsPresenter
    private lateinit var currentFragment: LinearLayout
    private lateinit var viewList: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollView: NestedScrollView

    private val selectedFilters: ArrayMap<AppliedFilter, ArrayList<String>> = ArrayMap()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentFragment = inflater.inflate(R.layout.fragment_newest_films, container, false) as LinearLayout

        progressBar = activity?.findViewById(R.id.pb_data_loading)!!

        viewList = currentFragment.findViewById(R.id.fragment_films_list_films_rv_films)
        viewList.layoutManager = GridLayoutManager(context, FILMS_PER_ROW)

        scrollView = activity?.findViewById(R.id.nestedScrollView)!!
        scrollView.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener {
            override fun onScrollChange(v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
                val view = scrollView.getChildAt(scrollView.childCount - 1)
                val diff = view.bottom - (scrollView.height + scrollView.scrollY)

                if (diff == 0) {
                    setProgressBarState(true)
                    newestFilmsPresenter.getFilms()
                }
            }
        })

        newestFilmsPresenter = NewestFilmsPresenter(this)
        newestFilmsPresenter.initFilms()

        createFilters()

        return currentFragment
    }

    override fun setFilms(films: ArrayList<Film>) {
        viewList.adapter = FilmsListRecyclerViewAdapter(films)
    }

    override fun redrawFilms() {
        //viewList.adapter.notifyItemRangeChanged()
        viewList.adapter?.notifyDataSetChanged()
    }

    override fun setProgressBarState(state: Boolean){
        if(state){
            progressBar.visibility = View.VISIBLE
        } else{
            progressBar.visibility = View.GONE
        }
    }


    private fun createFilters() {
        activity?.let {
            val filtersDialog = AlertDialog.Builder(it)
            // Get the layout inflater
            val filtersDialogView: LinearLayout = requireActivity().layoutInflater.inflate(R.layout.dialog_filters, null) as LinearLayout

            createCountriesFilters(filtersDialogView.findViewById(R.id.bt_countries))
            // createGenresFilters()

            filtersDialog.setView(filtersDialogView)
            filtersDialog.setPositiveButton("set") { dialog, id ->
                newestFilmsPresenter.applyFilters(selectedFilters)
                dialog.dismiss()
            }
            filtersDialog.setNegativeButton("cancel") { dialog, id ->
                dialog.dismiss()
            }
            val d = filtersDialog.create()
            currentFragment.findViewById<Button>(R.id.fragment_films_list_open_filters).setOnClickListener {
                d.show()
            }
        }
    }

    private fun createCountriesFilters(btn: Button) {
        val countries = resources.getStringArray(R.array.countries)
        val checkedCountries: ArrayList<String> = ArrayList()

        activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Выберите страны")
            builder.setMultiChoiceItems(countries, BooleanArray(countries.size)) { dialog, which, isChecked ->
                if (isChecked) {
                    checkedCountries.add(countries[which])
                } else {
                    checkedCountries.remove(countries[which])
                }
            }
            builder.setPositiveButton("Ok") { dialog, id ->
                selectedFilters[AppliedFilter.COUNTRY] = checkedCountries
                dialog.dismiss()
            }
            val d = builder.create()
            btn.setOnClickListener {
                d.show()
            }
        }
    }

    private fun createGenresFilters(btn: Button) {

    }
}
package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.constants.AppliedFilter
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.presenters.NewestFilmsPresenter
import com.BSLCommunity.onlinefilmstracker.views.OnFragmentInteractionListener
import com.BSLCommunity.onlinefilmstracker.viewsInterface.NewestFilmsView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.RangeSlider

class NewestFilmsFragment : Fragment(), NewestFilmsView {
    private val FILMS_PER_ROW: Int = 3

    private lateinit var newestFilmsPresenter: NewestFilmsPresenter
    private lateinit var currentFragment: LinearLayout
    private lateinit var viewList: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollView: NestedScrollView
    private lateinit var stopToast: Toast
    private lateinit var fragmentListener: OnFragmentInteractionListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentListener = context as OnFragmentInteractionListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentFragment = inflater.inflate(R.layout.fragment_newest_films, container, false) as LinearLayout

        progressBar = activity?.findViewById(R.id.pb_data_loading)!!

        viewList = currentFragment.findViewById(R.id.fragment_films_list_films_rv_films)
        viewList.layoutManager = GridLayoutManager(context, FILMS_PER_ROW)

        scrollView = currentFragment.findViewById(R.id.nestedScrollView)
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
        createStopToast()

        return currentFragment
    }

    private fun openFilm(film: Film) {
        val data = Bundle()
        data.putSerializable("film", film)

        fragmentListener.onFragmentInteraction(
            this, FilmFragment(),
            OnFragmentInteractionListener.Action.NEXT_FRAGMENT_HIDE, data, null
        )
    }

    override fun setFilms(films: ArrayList<Film>) {
        viewList.adapter = FilmsListRecyclerViewAdapter(films, ::openFilm)
    }

    override fun redrawFilms() {
        //viewList.adapter.notifyItemRangeChanged()
        viewList.adapter?.notifyDataSetChanged()
    }

    override fun setProgressBarState(state: Boolean) {
        if (state) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
    }

    override fun showStopToast() {
        stopToast.show()
    }


    private fun createFilters() {
        activity?.let {
            val filtersDialog = MaterialAlertDialogBuilder(it)

            // Get the layout inflater
            val filtersDialogView: LinearLayout = requireActivity().layoutInflater.inflate(R.layout.dialog_filters, null) as LinearLayout

            // Year range slider
            filtersDialogView.findViewById<RangeSlider>(R.id.year_range_slider).addOnChangeListener { slider, value, fromUser ->
                newestFilmsPresenter.setFilter(AppliedFilter.YEAR, arrayListOf(slider.values[0].toString(), slider.values[1].toString()))
            }

            // Rating range slider
            filtersDialogView.findViewById<RangeSlider>(R.id.rating_range_slider).addOnChangeListener { slider, value, fromUser ->
                newestFilmsPresenter.setFilter(AppliedFilter.RATING, arrayListOf(slider.values[0].toString(), slider.values[1].toString()))
            }

            // Film type buttons
            filtersDialogView.findViewById<RadioGroup>(R.id.film_types).setOnCheckedChangeListener { group, checkedId ->
                run {
                    val value: String = group.findViewById<RadioButton>(checkedId).text as String
                    newestFilmsPresenter.setFilter(AppliedFilter.TYPE, arrayListOf(value))
                }
            }

            // Countries filter
            createFilter(
                "Выберите страны",
                filtersDialogView.findViewById(R.id.bt_countries),
                AppliedFilter.COUNTRIES,
                resources.getStringArray(R.array.countries)
            )

            // Genres filter
            createFilter(
                "Выберите жанры",
                filtersDialogView.findViewById(R.id.bt_genres),
                AppliedFilter.GENRES,
                resources.getStringArray(R.array.genres)
            )

            // Inverted Countries filter
            createFilter(
                "Исключить страны",
                filtersDialogView.findViewById(R.id.bt_countries_inverted),
                AppliedFilter.COUNTRIES_INVERTED,
                resources.getStringArray(R.array.countries)
            )

            // Inverted Genres filter
            createFilter(
                "Исключить жанры",
                filtersDialogView.findViewById(R.id.bt_genres_inverted),
                AppliedFilter.GENRES_INVERTED,
                resources.getStringArray(R.array.genres)
            )

            filtersDialog.setView(filtersDialogView)
            filtersDialog.setPositiveButton("Установить") { dialog, id ->
                newestFilmsPresenter.applyFilters()
                dialog.dismiss()
            }
            filtersDialog.setNegativeButton("Отменить") { dialog, id ->
                dialog.dismiss()
            }
            val d = filtersDialog.create()
            currentFragment.findViewById<Button>(R.id.fragment_films_list_open_filters).setOnClickListener {
                d.show()
            }
        }
    }

    private fun createFilter(title: String, btn: Button, filterType: AppliedFilter, data: Array<String>) {
        val checkedItems: ArrayList<String> = ArrayList()

        activity?.let {
            val builder = MaterialAlertDialogBuilder(it)
            builder.setTitle(title)
            builder.setMultiChoiceItems(data, BooleanArray(data.size)) { dialog, which, isChecked ->
                if (isChecked) {
                    checkedItems.add(data[which])
                } else {
                    checkedItems.remove(data[which])
                }
            }
            builder.setPositiveButton("ОК") { dialog, id ->
                newestFilmsPresenter.setFilter(filterType, checkedItems)
                dialog.dismiss()
            }
            val d = builder.create()
            btn.setOnClickListener {
                d.show()
            }
        }
    }

    private fun createStopToast() {
        stopToast = Toast(activity?.applicationContext).also {
            // View and duration has to be set
            val view = LayoutInflater.from(context).inflate(R.layout.popup_toast, null)
            view.findViewById<TextView>(R.id.stop_btn).setOnClickListener {
                newestFilmsPresenter.stopGetFilms()
            }
            it.view = view
            it.duration = Toast.LENGTH_LONG
            it.setGravity(Gravity.START, 0, 0)
            it.setMargin(0.1f, 0.2f)
        }
    }
}
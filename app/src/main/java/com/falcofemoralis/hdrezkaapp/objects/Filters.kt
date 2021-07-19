package com.falcofemoralis.hdrezkaapp.objects

import android.util.ArrayMap
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.falcofemoralis.hdrezkaapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.RangeSlider


class Filters(private val iFilter: IFilter) {
    interface IFilter {
        fun applyFilters()
    }

    enum class AppliedFilter {
        COUNTRIES,
        COUNTRIES_INVERTED,
        GENRES,
        GENRES_INVERTED,
        RATING,
        TYPE,
        SORT
    }

    var appliedFilters: ArrayMap<AppliedFilter, Array<String?>> = ArrayMap() // applied filters
    private var appliedFiltersTmp: ArrayMap<AppliedFilter, Array<String?>>? = null
    private var activity: FragmentActivity? = null
    private var dialog: AlertDialog? = null
    private var ratingSliderView: RangeSlider? = null
    private var typeView: RadioGroup? = null
    private var sortView: RadioGroup? = null
    private var countriesView: Button? = null
    private var invertedCountriesView: Button? = null
    private var genresView: Button? = null
    private var invertedGenresView: Button? = null
    private var typeHeaderView: TextView? = null
    private var ratingHeaderView: TextView? = null
    private var sortHeaderView: TextView? = null

    fun createFilters(activity: FragmentActivity, btn: View) {
        this.activity = activity

        val filtersDialog = MaterialAlertDialogBuilder(activity)

        // Get the layout inflater
        val filtersDialogView: FrameLayout = activity.layoutInflater.inflate(R.layout.dialog_filters, null) as FrameLayout

        // Rating range slider
        ratingSliderView = filtersDialogView.findViewById(R.id.rating_range_slider)
        ratingHeaderView = filtersDialogView.findViewById(R.id.rating_slider_header)
        ratingSliderView?.addOnChangeListener { slider, value, fromUser ->
            setFilter(AppliedFilter.RATING, arrayOf(slider.values[0].toString(), slider.values[1].toString()))
        }

        // Film type buttons
        typeView = filtersDialogView.findViewById(R.id.film_types)
        typeHeaderView = filtersDialogView.findViewById(R.id.film_types_header)
        typeView?.setOnCheckedChangeListener { group, checkedId ->
            run {
                setFilter(AppliedFilter.TYPE, arrayOf(group.findViewById<RadioButton>(checkedId).text as String?))
            }
        }

        // Film sort buttons
        sortView = filtersDialogView.findViewById(R.id.film_sort)
        sortHeaderView = filtersDialogView.findViewById(R.id.film_sort_header)
        sortView?.setOnCheckedChangeListener { group, checkedId ->
            run {
                val pos: Int = when (checkedId) {
                    R.id.sort_last -> 0
                    R.id.sort_popular -> 1
                    R.id.sort_now -> 2
                    else -> 0
                }
                setFilter(AppliedFilter.SORT, arrayOf(pos.toString()))
            }
        }


        // Countries filter
        countriesView = filtersDialogView.findViewById(R.id.bt_countries)
        createFilter(
            activity.getString(R.string.choose_countries),
            countriesView,
            AppliedFilter.COUNTRIES,
            activity.resources.getStringArray(R.array.countries)
        )

        // Genres filter
        genresView = filtersDialogView.findViewById(R.id.bt_genres)
        createFilter(
            activity.getString(R.string.choose_genres),
            genresView,
            AppliedFilter.GENRES,
            activity.resources.getStringArray(R.array.genres)
        )

        // Inverted Countries filter
        invertedCountriesView = filtersDialogView.findViewById(R.id.bt_countries_inverted)
        createFilter(
            activity.getString(R.string.exclude_countries),
            invertedCountriesView,
            AppliedFilter.COUNTRIES_INVERTED,
            activity.resources.getStringArray(R.array.countries)
        )

        // Inverted Genres filter
        invertedGenresView = filtersDialogView.findViewById(R.id.bt_genres_inverted)
        createFilter(
            activity.getString(R.string.exclude_genres),
            invertedGenresView,
            AppliedFilter.GENRES_INVERTED,
            activity.resources.getStringArray(R.array.genres)
        )

        filtersDialog.setView(filtersDialogView)
        dialog = filtersDialog.create()

        filtersDialogView.findViewById<Button>(R.id.filter_set).setOnClickListener {
            //applyFilters()
            iFilter.applyFilters()
            dialog?.dismiss()
        }
        filtersDialogView.findViewById<Button>(R.id.filter_cancel).setOnClickListener {
            dismissFilters()
            dialog?.dismiss()
        }
        filtersDialogView.findViewById<Button>(R.id.filter_clear).setOnClickListener {
            clearFilters()
            Toast.makeText(activity.applicationContext, activity.getString(R.string.filters_cleared), Toast.LENGTH_LONG).show()
        }
        btn.setOnClickListener {
            initFilters()
            dialog?.show()
        }
    }


    private fun createFilter(title: String, btn: Button?, filterType: AppliedFilter, data: Array<String>) {
        activity?.let {
            val builder = MaterialAlertDialogBuilder(it)
            builder.setTitle(title)
            btn?.setOnClickListener {
                var checkedItems: Array<String?> = arrayOfNulls(data.size)

                getFilter(filterType)?.let {
                    checkedItems = it.clone()
                }

                val booleanArray = BooleanArray(data.size)
                for ((index, item) in data.withIndex()) {
                    booleanArray[index] = item == checkedItems[index]
                }

                builder.setPositiveButton(this.activity!!.getString(R.string.ok)) { dialog, id ->
                    setFilter(filterType, checkedItems)
                    dialog.dismiss()
                }

                builder.setMultiChoiceItems(data, booleanArray) { dialog, which, isChecked ->
                    if (isChecked) {
                        checkedItems[which] = data[which]
                    } else {
                        checkedItems[which] = null
                    }
                }
                val d = builder.create()
                d.show()
            }
        }
    }


    private fun initFilters() {
        appliedFiltersTmp = ArrayMap(appliedFilters)
    }

    fun setFilter(key: AppliedFilter, value: Array<String?>) {
        appliedFilters[key] = value
        appliedFiltersTmp
    }

    private fun dismissFilters() {
        appliedFiltersTmp?.let {
            appliedFilters = it
        }
    }

    private fun clearFilters() {
        appliedFilters.clear()
        //applyFilters()
        iFilter.applyFilters()
    }

    private fun getFilter(key: AppliedFilter): Array<String?>? {
        return appliedFilters[key]
    }

    // true - film соотвествует критериям
    // false - фильм не соотвествует критериям
    fun checkFilmForFilters(film: Film): Boolean {
        val applyList: ArrayMap<AppliedFilter, Boolean> = ArrayMap()

        val filters: ArrayMap<AppliedFilter, ArrayList<String>> = ArrayMap()
        for (filterEntry in appliedFilters) {
            val list: ArrayList<String> = ArrayList()
            for (item in filterEntry.value) {
                if (item != null) {
                    list.add(item)
                }
            }
            if (list.size > 0) {
                filters[filterEntry.key] = list
            }
        }

        for (filterEntry in filters) {
            when (filterEntry.key) {
                AppliedFilter.COUNTRIES -> {
                    film.countries?.let {
                        for (country in film.countries!!) {
                            applyList[AppliedFilter.COUNTRIES] = country in filterEntry.value

                            if (applyList[AppliedFilter.COUNTRIES] == true) {
                                break
                            }
                        }
                    }
                }

                AppliedFilter.GENRES -> {
                    film.genres?.let {
                        for (genre in film.genres!!) {
                            applyList[AppliedFilter.GENRES] = genre in filterEntry.value

                            if (applyList[AppliedFilter.GENRES] == true) {
                                break
                            }
                        }
                    }
                }

                AppliedFilter.COUNTRIES_INVERTED -> {
                    film.countries?.let {
                        for (country in film.countries!!) {
                            applyList[AppliedFilter.COUNTRIES_INVERTED] = country !in filterEntry.value

                            if (applyList[AppliedFilter.COUNTRIES_INVERTED] == false) {
                                break
                            }
                        }
                    }
                }

                AppliedFilter.GENRES_INVERTED -> {
                    film.genres?.let {
                        for (genre in film.genres!!) {
                            applyList[AppliedFilter.GENRES_INVERTED] = genre !in filterEntry.value

                            if (applyList[AppliedFilter.GENRES_INVERTED] == false) {
                                break
                            }
                        }
                    }
                }

                AppliedFilter.RATING -> {
                    if (film.ratingIMDB != null && film.ratingIMDB?.isNotEmpty() == true) {
                        val min: Float = filterEntry.value[0].toFloat()
                        val max: Float = filterEntry.value[1].toFloat()
                        val filmRating: Float = film.ratingIMDB!!.toFloat()
                        applyList[AppliedFilter.RATING] = filmRating in min..max
                    } else {
                        applyList[AppliedFilter.RATING] = false
                    }
                }

                /*              AppliedFilter.YEAR -> {
                                  if (film.year != null && film.year?.isNotEmpty() == true) {
                                      val min: Float = filterEntry.value[0].toFloat()
                                      val max: Float = filterEntry.value[1].toFloat()
                                      val filmYear: Float = film.year!!.take(4).toFloat()

                                      applyList[AppliedFilter.YEAR] = filmYear in min..max
                                  } else {
                                      applyList[AppliedFilter.YEAR] = false
                                  }
                              }
              */
                AppliedFilter.TYPE -> {
                    if (filterEntry.value[0] == "Все") {
                        continue
                    }

                    film.type.let {
                        applyList[AppliedFilter.TYPE] = filterEntry.value[0].take(4) == film.type!!.take(4)
                    }
                }
            }
        }

        var isApply = true
        for (applyItem in applyList) {
            isApply = isApply and applyItem.value
        }

        return isApply
    }

    fun removeBlock(blocks: ArrayList<AppliedFilter>) {
        for (block in blocks) {

            val view = when (block) {
                AppliedFilter.COUNTRIES -> countriesView
                AppliedFilter.COUNTRIES_INVERTED -> invertedCountriesView
                AppliedFilter.GENRES -> genresView
                AppliedFilter.GENRES_INVERTED -> invertedGenresView
                AppliedFilter.RATING -> {
                    ratingHeaderView?.visibility = View.GONE
                    ratingSliderView
                }
                AppliedFilter.SORT -> {
                    sortHeaderView?.visibility = View.GONE
                    sortView
                }
                AppliedFilter.TYPE -> {
                    typeHeaderView?.visibility = View.GONE
                    typeView
                }
            }

            view?.visibility = View.GONE
        }
    }
}
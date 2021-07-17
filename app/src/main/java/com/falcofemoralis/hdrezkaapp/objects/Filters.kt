package com.falcofemoralis.hdrezkaapp.objects

import android.util.ArrayMap
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.constants.AppliedFilter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.RangeSlider

class Filters(private val iFilter: IFilter) {
    interface IFilter {
        fun applyFilters()
    }

    var appliedFilters: ArrayMap<AppliedFilter, Array<String?>> = ArrayMap() // applied filters
    private var appliedFiltersTmp: ArrayMap<AppliedFilter, Array<String?>>? = null
    private var activity: FragmentActivity? = null

    fun createFilters(activity: FragmentActivity, btn: Button) {
        this.activity = activity

        val filtersDialog = MaterialAlertDialogBuilder(activity)

        // Get the layout inflater
        val filtersDialogView: RelativeLayout = activity.layoutInflater.inflate(R.layout.dialog_filters, null) as RelativeLayout

        // Rating range slider
        filtersDialogView.findViewById<RangeSlider>(R.id.rating_range_slider).addOnChangeListener { slider, value, fromUser ->
            setFilter(AppliedFilter.RATING, arrayOf(slider.values[0].toString(), slider.values[1].toString()))
        }

        // Film type buttons
        filtersDialogView.findViewById<RadioGroup>(R.id.film_types).setOnCheckedChangeListener { group, checkedId ->
            run {
                setFilter(AppliedFilter.TYPE, arrayOf(group.findViewById<RadioButton>(checkedId).text as String?))
            }
        }

        // Film sort buttons
        filtersDialogView.findViewById<RadioGroup>(R.id.film_sort).setOnCheckedChangeListener { group, checkedId ->
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
        createFilter(
            activity.getString(R.string.choose_countries),
            filtersDialogView.findViewById(R.id.bt_countries),
            AppliedFilter.COUNTRIES,
            activity.resources.getStringArray(R.array.countries)
        )

        // Genres filter
        createFilter(
            activity.getString(R.string.choose_genres),
            filtersDialogView.findViewById(R.id.bt_genres),
            AppliedFilter.GENRES,
            activity.resources.getStringArray(R.array.genres)
        )

        // Inverted Countries filter
        createFilter(
            activity.getString(R.string.exclude_countries),
            filtersDialogView.findViewById(R.id.bt_countries_inverted),
            AppliedFilter.COUNTRIES_INVERTED,
            activity.resources.getStringArray(R.array.countries)
        )

        // Inverted Genres filter
        createFilter(
            activity.getString(R.string.exclude_genres),
            filtersDialogView.findViewById(R.id.bt_genres_inverted),
            AppliedFilter.GENRES_INVERTED,
            activity.resources.getStringArray(R.array.genres)
        )

        filtersDialog.setView(filtersDialogView)
        val d = filtersDialog.create()

        filtersDialogView.findViewById<Button>(R.id.filter_set).setOnClickListener {
            //applyFilters()
            iFilter.applyFilters()
            d.dismiss()
        }
        filtersDialogView.findViewById<Button>(R.id.filter_cancel).setOnClickListener {
            dismissFilters()
            d.dismiss()
        }
        filtersDialogView.findViewById<Button>(R.id.filter_clear).setOnClickListener {
            clearFilters()
            Toast.makeText(activity.applicationContext, activity.getString(R.string.filters_cleared), Toast.LENGTH_LONG).show()
        }
        btn.setOnClickListener {
            initFilters()
            d.show()
        }

    }

    fun createFilter(title: String, btn: Button, filterType: AppliedFilter, data: Array<String>) {
        activity?.let {
            val builder = MaterialAlertDialogBuilder(it)
            builder.setTitle(title)
            btn.setOnClickListener {
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


    fun initFilters() {
        appliedFiltersTmp = ArrayMap(appliedFilters)
    }

    fun setFilter(key: AppliedFilter, value: Array<String?>) {
        appliedFilters[key] = value
        appliedFiltersTmp
    }

    fun dismissFilters() {
        appliedFiltersTmp?.let {
            appliedFilters = it
        }
    }

    fun clearFilters() {
        appliedFilters.clear()
        //applyFilters()
        iFilter.applyFilters()
    }

    fun getFilter(key: AppliedFilter): Array<String?>? {
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

                AppliedFilter.YEAR -> {
                    if (film.year != null && film.year?.isNotEmpty() == true) {
                        val min: Float = filterEntry.value[0].toFloat()
                        val max: Float = filterEntry.value[1].toFloat()
                        val filmYear: Float = film.year!!.take(4).toFloat()

                        applyList[AppliedFilter.YEAR] = filmYear in min..max
                    } else {
                        applyList[AppliedFilter.YEAR] = false
                    }
                }

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
}
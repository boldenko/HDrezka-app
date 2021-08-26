package com.falcofemoralis.hdrezkaapp.views.elements

import android.util.ArrayMap
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.chivorn.smartmaterialspinner.SmartMaterialSpinner
import com.falcofemoralis.hdrezkaapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.RangeSlider


class FiltersMenu(
    private val iFilters: IFilters,
    private val activity: FragmentActivity,
    private val openBtn: View
) {
    interface IFilters {
        fun onFilterCreated(appliedFilters: ArrayMap<AppliedFilter, Array<String?>>)

        fun onApplyFilters(appliedFilters: ArrayMap<AppliedFilter, Array<String?>>)
    }

    enum class AppliedFilter {
        COUNTRIES,
        COUNTRIES_INVERTED,
        GENRES,
        GENRES_INVERTED,
        RATING,
        TYPE,
        SORT,
        SPINNER_GENRES,
        SPINNER_YEARS
    }

    private var filtersDialogBuilder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(activity)
    private val filtersDialogView: FrameLayout = activity.layoutInflater.inflate(R.layout.dialog_filters, null) as FrameLayout
    private var appliedFilters: ArrayMap<AppliedFilter, Array<String?>> = ArrayMap() // applied filters
    private var onSelectionAppliedFilters: ArrayMap<AppliedFilter, Array<String?>> = ArrayMap() // active filters during selecting..

    // private var appliedFiltersTmp: ArrayMap<AppliedFilter, Array<String?>>? = null
    private var dialog: AlertDialog? = null
    private var ratingSliderView: RangeSlider? = null
    private var typeGroupView: RadioGroup? = null
    private var sortGroupView: RadioGroup? = null
    var genresSpinnerView: SmartMaterialSpinner<String>? = null
    var yearsSpinnerView: SmartMaterialSpinner<String>? = null

    init {
        filtersDialogView.findViewById<TextView>(R.id.bt_countries).visibility = View.GONE
        filtersDialogView.findViewById<TextView>(R.id.bt_countries_inverted).visibility = View.GONE
        filtersDialogView.findViewById<TextView>(R.id.bt_genres).visibility = View.GONE
        filtersDialogView.findViewById<TextView>(R.id.bt_genres_inverted).visibility = View.GONE
        filtersDialogView.findViewById<LinearLayout>(R.id.rating_slider_layout).visibility = View.GONE
        filtersDialogView.findViewById<LinearLayout>(R.id.film_types_layout).visibility = View.GONE
        filtersDialogView.findViewById<LinearLayout>(R.id.film_sort_layout).visibility = View.GONE
        filtersDialogView.findViewById<SmartMaterialSpinner<String>>(R.id.sp_genres).visibility = View.GONE
        filtersDialogView.findViewById<SmartMaterialSpinner<String>>(R.id.sp_years).visibility = View.GONE
    }

    fun apply() {
        filtersDialogBuilder.setView(filtersDialogView)

        filtersDialogView.findViewById<Button>(R.id.filter_set).setOnClickListener {
            appliedFilters = ArrayMap(onSelectionAppliedFilters)
            iFilters.onApplyFilters(appliedFilters)
            dialog?.dismiss()
        }
        filtersDialogView.findViewById<Button>(R.id.filter_cancel).setOnClickListener {
            val rating: Array<String?>? = appliedFilters[AppliedFilter.RATING]
            if (rating != null) {
                ratingSliderView?.setValues(rating[0]!!.toFloat(), rating[1]!!.toFloat())
            } else {
                ratingSliderView?.setValues(0f, 10f)
            }

            val types: Array<String?>? = appliedFilters[AppliedFilter.TYPE]
            val id1 = if (types != null) {
                when (types[0]) {
                    "Все" -> R.id.type_all
                    "Фильмы" -> R.id.type_films
                    "Сериалы" -> R.id.type_serials
                    "Мультфильмы" -> R.id.type_multfilms
                    "Аниме" -> R.id.type_anime
                    else -> R.id.type_all
                }
            } else {
                R.id.type_all
            }

            typeGroupView?.findViewById<RadioButton>(id1)?.isChecked = true

            val sort: Array<String?>? = appliedFilters[AppliedFilter.SORT]
            val id2 = if (sort != null) {
                when (sort[0]!!.toInt()) {
                    0 -> R.id.sort_last
                    1 -> R.id.sort_popular
                    2 -> R.id.sort_now
                    else -> R.id.sort_last
                }
            } else {
                R.id.sort_last
            }
            sortGroupView?.findViewById<RadioButton>(id2)?.isChecked = true

            onSelectionAppliedFilters.clear()
            dialog?.dismiss()
        }
        filtersDialogView.findViewById<Button>(R.id.filter_clear).setOnClickListener {
            clearFilters()
            Toast.makeText(activity.applicationContext, activity.getString(R.string.filters_cleared), Toast.LENGTH_LONG).show()
        }

        dialog = filtersDialogBuilder.create()
        openBtn.setOnClickListener {
            onSelectionAppliedFilters = ArrayMap(appliedFilters)
            dialog?.show()
        }
    }

    fun createDialogFilter(filterType: AppliedFilter, data: Array<String>): FiltersMenu {
        var titleId: Int? = null
        var btnViewId: Int? = null

        when (filterType) {
            AppliedFilter.COUNTRIES -> {
                titleId = R.string.choose_countries
                btnViewId = R.id.bt_countries
            }
            AppliedFilter.GENRES -> {
                titleId = R.string.choose_genres
                btnViewId = R.id.bt_genres
            }
            AppliedFilter.COUNTRIES_INVERTED -> {
                titleId = R.string.exclude_countries
                btnViewId = R.id.bt_countries_inverted
            }
            AppliedFilter.GENRES_INVERTED -> {
                titleId = R.string.exclude_genres
                btnViewId = R.id.bt_genres_inverted
            }
        }
        val title = titleId?.let { activity.getString(it) }
        val btnView: TextView? = btnViewId?.let { filtersDialogView.findViewById(it) }

        btnView?.visibility = View.VISIBLE

        activity.let {
            val builder = MaterialAlertDialogBuilder(it)
            builder.setTitle(title)
            btnView?.setOnClickListener {
                var checkedItems: Array<String?> = arrayOfNulls(data.size)

                appliedFilters[filterType]?.let {
                    checkedItems = it.clone()
                }

                val booleanArray = BooleanArray(data.size)
                for ((index, item) in data.withIndex()) {
                    booleanArray[index] = item == checkedItems[index]
                }

                builder.setPositiveButton(this.activity.getString(R.string.ok)) { dialog, id ->
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

        return this
    }

    fun createRatingFilter(): FiltersMenu {
        ratingSliderView = filtersDialogView.findViewById(R.id.rating_range_slider)
        filtersDialogView.findViewById<LinearLayout>(R.id.rating_slider_layout).visibility = View.VISIBLE

        ratingSliderView?.addOnChangeListener { slider, value, fromUser ->
            setFilter(AppliedFilter.RATING, arrayOf(slider.values[0].toString(), slider.values[1].toString()))
        }

        return this
    }

    fun createTypesFilter(isAllType: Boolean): FiltersMenu {
        typeGroupView = filtersDialogView.findViewById(R.id.film_types)
        filtersDialogView.findViewById<LinearLayout>(R.id.film_types_layout).visibility = View.VISIBLE

        if (!isAllType) {
            filtersDialogView.findViewById<RadioButton>(R.id.type_all).visibility = View.GONE
        }

        typeGroupView?.setOnCheckedChangeListener { group, checkedId ->
            run {
                if (checkedId != -1) {
                    setFilter(AppliedFilter.TYPE, arrayOf(group.findViewById<RadioButton>(checkedId).text as String?))
                } else {
                    group.findViewById<RadioButton>(R.id.type_all).isChecked = true
                }
            }
        }
        return this
    }

    fun createSortFilter(): FiltersMenu {
        sortGroupView = filtersDialogView.findViewById(R.id.film_sort)
        filtersDialogView.findViewById<LinearLayout>(R.id.film_sort_layout).visibility = View.VISIBLE

        sortGroupView?.setOnCheckedChangeListener { group, checkedId ->
            run {
                if (checkedId != -1) {
                    val pos: Int = when (checkedId) {
                        R.id.sort_last -> 0
                        R.id.sort_popular -> 1
                        R.id.sort_now -> 2
                        else -> 0
                    }
                    setFilter(AppliedFilter.SORT, arrayOf(pos.toString()))
                } else {
                    group.findViewById<RadioButton>(R.id.sort_last).isChecked = true
                }
            }
        }

        return this
    }

    fun createSpinnerFilter(filterType: AppliedFilter): FiltersMenu {
        when (filterType) {
            AppliedFilter.SPINNER_GENRES -> {
                genresSpinnerView = filtersDialogView.findViewById(R.id.sp_genres)
                genresSpinnerView?.visibility = View.VISIBLE
            }
            AppliedFilter.SPINNER_YEARS -> {
                yearsSpinnerView = filtersDialogView.findViewById(R.id.sp_years)
                yearsSpinnerView?.visibility = View.VISIBLE
            }
        }
        return this
    }

    fun removeClearButton(): FiltersMenu {
        filtersDialogView.findViewById<Button>(R.id.filter_clear)?.visibility = View.GONE
        filtersDialogView.findViewById<Button>(R.id.filter_cancel)?.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f)
        filtersDialogView.findViewById<Button>(R.id.filter_set)?.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f)

        return this
    }

    private fun setFilter(key: AppliedFilter, value: Array<String?>) {
        onSelectionAppliedFilters[key] = value
    }

    private fun clearFilters() {
        appliedFilters.clear()
        onSelectionAppliedFilters.clear()
        ratingSliderView?.let {
            ratingSliderView!!.setValues(0f, 10f)
        }
        typeGroupView?.clearCheck()
        sortGroupView?.clearCheck()
        appliedFilters[AppliedFilter.SORT] = arrayOf("0")
        iFilters.onApplyFilters(appliedFilters)
        dialog?.dismiss()
    }
}
package com.falcofemoralis.hdrezkaapp.views.elements

import android.util.ArrayMap
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
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
        SORT
    }

    private var filtersDialogBuilder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(activity)
    private val filtersDialogView: FrameLayout = activity.layoutInflater.inflate(R.layout.dialog_filters, null) as FrameLayout
    private var appliedFilters: ArrayMap<AppliedFilter, Array<String?>> = ArrayMap() // applied filters
    private var appliedFiltersTmp: ArrayMap<AppliedFilter, Array<String?>>? = null
    private var dialog: AlertDialog? = null

    init {
        filtersDialogView.findViewById<TextView>(R.id.bt_countries).visibility = View.GONE
        filtersDialogView.findViewById<TextView>(R.id.bt_countries_inverted).visibility = View.GONE
        filtersDialogView.findViewById<TextView>(R.id.bt_genres).visibility = View.GONE
        filtersDialogView.findViewById<TextView>(R.id.bt_genres_inverted).visibility = View.GONE
        filtersDialogView.findViewById<TextView>(R.id.rating_slider_header).visibility = View.GONE
        filtersDialogView.findViewById<RangeSlider>(R.id.rating_range_slider).visibility = View.GONE
        filtersDialogView.findViewById<TextView>(R.id.film_types_header).visibility = View.GONE
        filtersDialogView.findViewById<RadioGroup>(R.id.film_types).visibility = View.GONE
        filtersDialogView.findViewById<TextView>(R.id.film_sort_header).visibility = View.GONE
        filtersDialogView.findViewById<RadioGroup>(R.id.film_sort).visibility = View.GONE
    }

    fun getAppliedFilter(type: AppliedFilter): Array<String?>? {
        return appliedFilters[type]
    }

    fun apply() {
        filtersDialogBuilder.setView(filtersDialogView)

        filtersDialogView.findViewById<Button>(R.id.filter_set).setOnClickListener {
            //applyFilters()
            iFilters.onApplyFilters(appliedFilters)
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

        dialog = filtersDialogBuilder.create()
        openBtn.setOnClickListener {
            appliedFiltersTmp = ArrayMap(appliedFilters)
            dialog?.show()
        }

        iFilters.onFilterCreated(appliedFilters)
    }

    fun createRatingFilter(): FiltersMenu {
        val ratingSliderView: RangeSlider = filtersDialogView.findViewById(R.id.rating_range_slider)
        val ratingHeaderView: TextView = filtersDialogView.findViewById(R.id.rating_slider_header)

        ratingSliderView.visibility = View.VISIBLE
        ratingHeaderView.visibility = View.VISIBLE

        ratingSliderView.addOnChangeListener { slider, value, fromUser ->
            setFilter(AppliedFilter.RATING, arrayOf(slider.values[0].toString(), slider.values[1].toString()))
        }

        return this
    }

    fun createTypesFilter(isAllType: Boolean): FiltersMenu {
        // Film type buttons
        val typeView: RadioGroup = filtersDialogView.findViewById(R.id.film_types)
        val typeHeaderView: TextView = filtersDialogView.findViewById(R.id.film_types_header)

        if (!isAllType) {
            filtersDialogView.findViewById<RadioButton>(R.id.type_all).visibility = View.GONE
        }

        typeView.visibility = View.VISIBLE
        typeHeaderView.visibility = View.VISIBLE

        typeView.setOnCheckedChangeListener { group, checkedId ->
            run {
                setFilter(AppliedFilter.TYPE, arrayOf(group.findViewById<RadioButton>(checkedId).text as String?))
               /* if(isAllType){
                    val position = when(checkedId){

                    }
                    iFilters.onDialogApply(AppliedFilter.TYPE,
                }*/
            }
        }
        return this
    }

    fun createSortFilter(): FiltersMenu {
        val sortView: RadioGroup = filtersDialogView.findViewById(R.id.film_sort)
        val sortHeaderView: TextView = filtersDialogView.findViewById(R.id.film_sort_header)

        sortView.visibility = View.VISIBLE
        sortHeaderView.visibility = View.VISIBLE

        sortView.setOnCheckedChangeListener { group, checkedId ->
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

        return this
    }

    fun createDialogFilter(filterType: AppliedFilter, data: Array<String>, isSingle: Boolean): FiltersMenu {
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

                getAppliedFilter(filterType)?.let {
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

    private fun setFilter(key: AppliedFilter, value: Array<String?>) {
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
        iFilters.onApplyFilters(appliedFilters)
    }
}
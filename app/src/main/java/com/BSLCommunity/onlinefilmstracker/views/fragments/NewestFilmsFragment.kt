package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.constants.AppliedFilter
import com.BSLCommunity.onlinefilmstracker.interfaces.IConnection
import com.BSLCommunity.onlinefilmstracker.presenters.NewestFilmsPresenter
import com.BSLCommunity.onlinefilmstracker.views.viewsInterface.FilmListCallView
import com.BSLCommunity.onlinefilmstracker.views.viewsInterface.NewestFilmsView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.RangeSlider

class NewestFilmsFragment : Fragment(), NewestFilmsView, FilmListCallView {
    private lateinit var currentView: View
    private lateinit var newestFilmsPresenter: NewestFilmsPresenter
    private lateinit var filmsListFragment: FilmsListFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_newest_films, container, false) as LinearLayout

        filmsListFragment = FilmsListFragment()
        filmsListFragment.setCallView(this)
        childFragmentManager.beginTransaction().replace(R.id.fragment_newest_films_fcv_container, filmsListFragment).commit()

        createFilters()

        return currentView
    }

    override fun onFilmsListCreated() {
        newestFilmsPresenter = NewestFilmsPresenter(this, filmsListFragment)
        newestFilmsPresenter.initFilms()
    }

    private fun createFilters() {
        activity?.let {
            val filtersDialog = MaterialAlertDialogBuilder(it)

            // Get the layout inflater
            val filtersDialogView: LinearLayout = requireActivity().layoutInflater.inflate(R.layout.dialog_filters, null) as LinearLayout

            // Year range slider
            filtersDialogView.findViewById<RangeSlider>(R.id.year_range_slider).addOnChangeListener { slider, value, fromUser ->
                newestFilmsPresenter.setFilter(AppliedFilter.YEAR, arrayOf(slider.values[0].toString(), slider.values[1].toString()))
            }

            // Rating range slider
            filtersDialogView.findViewById<RangeSlider>(R.id.rating_range_slider).addOnChangeListener { slider, value, fromUser ->
                newestFilmsPresenter.setFilter(AppliedFilter.RATING, arrayOf(slider.values[0].toString(), slider.values[1].toString()))
            }

            // Film type buttons
            filtersDialogView.findViewById<RadioGroup>(R.id.film_types).setOnCheckedChangeListener { group, checkedId ->
                run {
                    val value: String = group.findViewById<RadioButton>(checkedId).text as String
                    newestFilmsPresenter.setFilter(AppliedFilter.TYPE, arrayOf(value))
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
                newestFilmsPresenter.dismissFilters()
                dialog.dismiss()
            }
            filtersDialog.setNeutralButton("Убрать") { dialog, id ->
                newestFilmsPresenter.clearFilters()
                Toast.makeText(requireContext(), "Фильтры убраны!", Toast.LENGTH_LONG).show()
            }
            val d = filtersDialog.create()
            currentView.findViewById<Button>(R.id.fragment_newest_films_bt_filters).setOnClickListener {
                newestFilmsPresenter.initFilters()
                d.show()
            }
        }
    }

    private fun createFilter(title: String, btn: Button, filterType: AppliedFilter, data: Array<String>) {
        activity?.let {
            val builder = MaterialAlertDialogBuilder(it)
            builder.setTitle(title)
            btn.setOnClickListener {
                var checkedItems: Array<String?> = arrayOfNulls(data.size)

                newestFilmsPresenter.getFilter(filterType)?.let {
                    checkedItems = it.clone()
                }

                val booleanArray = BooleanArray(data.size)
                for ((index, item) in data.withIndex()) {
                    booleanArray[index] = item == checkedItems[index]
                }

                builder.setPositiveButton("ОК") { dialog, id ->
                    newestFilmsPresenter.setFilter(filterType, checkedItems)
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

    override fun triggerEnd() {
        newestFilmsPresenter.getNextFilms()
    }

    override fun showConnectionError(type: IConnection.ErrorType) {
        if (type == IConnection.ErrorType.PARSING_ERROR) {
            Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_SHORT).show()
        }
    }
}
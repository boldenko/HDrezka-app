package com.falcofemoralis.hdrezkaapp.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.constants.AppliedFilter
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.interfaces.OnFragmentInteractionListener
import com.falcofemoralis.hdrezkaapp.presenters.NewestFilmsPresenter
import com.falcofemoralis.hdrezkaapp.utils.DialogManager
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmListCallView
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.NewestFilmsView

class NewestFilmsFragment : Fragment(), NewestFilmsView, FilmListCallView {
    private lateinit var currentView: View
    private lateinit var newestFilmsPresenter: NewestFilmsPresenter
    private lateinit var filmsListFragment: FilmsListFragment
    private lateinit var fragmentListener: OnFragmentInteractionListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentListener = context as OnFragmentInteractionListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_newest_films, container, false) as LinearLayout

        initFilmsList()

        // initSeriesUpdatesBtn()

        return currentView
    }

    private fun initFilmsList() {
        filmsListFragment = FilmsListFragment()
        filmsListFragment.setCallView(this)
        childFragmentManager.beginTransaction().replace(R.id.fragment_newest_films_fcv_container, filmsListFragment).commit()
    }

    override fun onFilmsListCreated() {
        newestFilmsPresenter = NewestFilmsPresenter(this, filmsListFragment)
        newestFilmsPresenter.initFilms()

        initFiltersBtn()
    }

    override fun onFilmsListDataInit() {
    }

    override fun triggerEnd() {
        newestFilmsPresenter.filmsListPresenter.getNextFilms()
    }

    private fun initFiltersBtn() {
        val builder = DialogManager.getDialog(requireContext(), null)
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_newest_filters, null)

        val typeGroupView: RadioGroup = dialogView.findViewById(R.id.film_types)
        typeGroupView.setOnCheckedChangeListener { group, checkedId ->
            run {
                if (checkedId != -1) {
                    val pos: Int = when (checkedId) {
                        R.id.type_all -> 0
                        R.id.type_films -> 1
                        R.id.type_serials -> 2
                        R.id.type_multfilms -> 3
                        R.id.type_anime -> 4
                        else -> 0
                    }
                    newestFilmsPresenter.setFilter(AppliedFilter.TYPE, pos)
                } else {
                    group.findViewById<RadioButton>(R.id.type_all).isChecked = true
                }
            }
        }

        val sortGroupView: RadioGroup = dialogView.findViewById(R.id.film_sort)
        sortGroupView.setOnCheckedChangeListener { group, checkedId ->
            run {
                if (checkedId != -1) {
                    val pos: Int = when (checkedId) {
                        R.id.sort_last -> 0
                        R.id.sort_popular -> 1
                        R.id.sort_now -> 2
                        R.id.sort_new -> 3
                        R.id.sort_announce -> 4
                        else -> 0
                    }
                    newestFilmsPresenter.setFilter(AppliedFilter.SORT, pos)
                } else {
                    group.findViewById<RadioButton>(R.id.sort_last).isChecked = true
                }
            }
        }

        builder.setView(dialogView)
        builder.setPositiveButton(R.string.ok_text) { d, i ->
            newestFilmsPresenter.applyFilters()
            d.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { d, i ->
            d.dismiss()
        }

        val d = builder.create()
        currentView.findViewById<TextView>(R.id.fragment_newest_films_tv_filters).setOnClickListener {
            d.show()
        }
    }

    override fun showConnectionError(type: IConnection.ErrorType, errorText: String) {
        try {
            if (context != null) {
                ExceptionHelper.showToastError(requireContext(), type, errorText)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*fun initSeriesUpdatesBtn() {
        val btn = currentView.findViewById<TextView>(R.id.fragment_newest_films_tv_new_series)
        btn.setOnClickListener {
            var dialog: AlertDialog? = null
            val dialogView = layoutInflater.inflate(R.layout.dialog_series_updates, null) as LinearLayout

            newestFilmsPresenter.initSeriesUpdates { seriesUpdates ->
                val container = dialogView.findViewById<LinearLayout>(R.id.series_updates_container)
                dialogView.findViewById<ProgressBar>(R.id.series_updates_progress).visibility = View.GONE

                for ((date, updateItems) in seriesUpdates) {
                    // костыль т.к .collapsed() не срабатывает
                    val layout: LinearLayout = if (date.contains("Сегодня")) {
                        layoutInflater.inflate(R.layout.inflate_season_layout_expanded, null) as LinearLayout
                    } else {
                        layoutInflater.inflate(R.layout.inflate_season_layout, null) as LinearLayout
                    }

                    val expandedList: ExpandableLinearLayout = layout.findViewById(R.id.inflate_season_layout_list)

                    layout.findViewById<TextView>(R.id.inflate_season_layout_header).text = date
                    layout.findViewById<LinearLayout>(R.id.inflate_season_layout_button).setOnClickListener {
                        expandedList.toggle()
                    }

                    var lastColor = R.color.light_background
                    for (item in updateItems) {
                        if (item.title.isEmpty()) {
                            continue
                        }

                        val itemView = layoutInflater.inflate(R.layout.inflate_series_updates_item, null) as LinearLayout
                        itemView.findViewById<TextView>(R.id.inflate_series_updates_item_title).text = item.title
                        itemView.findViewById<TextView>(R.id.inflate_series_updates_item_season).text = item.season
                        itemView.findViewById<TextView>(R.id.inflate_series_updates_item_episode).text = item.episode
                        if (!item.voice.isNullOrEmpty()) {
                            itemView.findViewById<TextView>(R.id.inflate_series_updates_item_voice).text = item.voice
                        }

                        val film = Film(SettingsData.provider + item.filmLink)
                        itemView.setOnClickListener {
                            if (!film.filmLink.isNullOrEmpty()) {
                                dialog?.dismiss()
                                FragmentOpener.openWithData(this, fragmentListener, film, "film")
                            }
                        }

                        if (item.isUserWatch) {
                            itemView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.main_color_3))
                        } else {
                            lastColor = if (lastColor == R.color.dark_background) R.color.light_background else R.color.dark_background
                            itemView.setBackgroundColor(ContextCompat.getColor(requireContext(), lastColor))
                        }
                        expandedList.addView(itemView)
                    }
                    container.addView(layout)
                }
            }

            val builder = DialogManager.getDialog(requireContext(), R.string.series_update_hot)
            builder.setView(dialogView)
            builder.setPositiveButton(getString(R.string.ok_text)) { dialog, id ->
                dialog.cancel()
            }
            dialog = builder.create()
            dialog.show()
        }
    }*/
}
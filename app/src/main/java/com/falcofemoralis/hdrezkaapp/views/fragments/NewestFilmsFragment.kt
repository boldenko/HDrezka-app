package com.falcofemoralis.hdrezkaapp.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.interfaces.OnFragmentInteractionListener
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import com.falcofemoralis.hdrezkaapp.presenters.NewestFilmsPresenter
import com.falcofemoralis.hdrezkaapp.utils.DialogManager
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper
import com.falcofemoralis.hdrezkaapp.utils.FragmentOpener
import com.falcofemoralis.hdrezkaapp.views.elements.FiltersMenu
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmListCallView
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.NewestFilmsView
import com.github.aakira.expandablelayout.ExpandableLinearLayout

class NewestFilmsFragment : Fragment(), NewestFilmsView, FilmListCallView {
    private lateinit var currentView: View
    private lateinit var newestFilmsPresenter: NewestFilmsPresenter
    private lateinit var filmsListFragment: FilmsListFragment
    private lateinit var filtersMenu: FiltersMenu
    private lateinit var fragmentListener: OnFragmentInteractionListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentListener = context as OnFragmentInteractionListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_newest_films, container, false) as LinearLayout

        filmsListFragment = FilmsListFragment()
        filmsListFragment.setCallView(this)
        childFragmentManager.beginTransaction().replace(R.id.fragment_newest_films_fcv_container, filmsListFragment).commit()

        initSeriesUpdatesBtn()

        return currentView
    }

    override fun onFilmsListCreated() {
        newestFilmsPresenter = NewestFilmsPresenter(this, filmsListFragment)
        filtersMenu = FiltersMenu(newestFilmsPresenter, requireActivity(), currentView.findViewById(R.id.fragment_newest_films_tv_filters))
        filtersMenu
            .createDialogFilter(FiltersMenu.AppliedFilter.COUNTRIES, resources.getStringArray(R.array.countries))
            .createDialogFilter(FiltersMenu.AppliedFilter.GENRES, resources.getStringArray(R.array.genres))
            .createDialogFilter(FiltersMenu.AppliedFilter.COUNTRIES_INVERTED, resources.getStringArray(R.array.countries))
            .createDialogFilter(FiltersMenu.AppliedFilter.GENRES_INVERTED, resources.getStringArray(R.array.genres))
            .createRatingFilter()
            .createTypesFilter(true)
            .createSortFilter()
            .apply()
        newestFilmsPresenter.initFilms()
    }

    override fun dataInited() {

    }

    override fun triggerEnd() {
        newestFilmsPresenter.filmsListPresenter.getNextFilms()
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

    override fun showFilterMsg() {
        Toast.makeText(requireContext(), R.string.filter_applied_hint, Toast.LENGTH_LONG).show()
    }

    fun initSeriesUpdatesBtn() {
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
                    var count = 0
                    for (item in updateItems) {
                        if (item.title.isEmpty()) {
                            continue
                        }


                        if(count == 20){
                            break
                        }
                        count++

                        val itemView = layoutInflater.inflate(R.layout.inflate_series_updates_item, null) as LinearLayout
                        itemView.findViewById<TextView>(R.id.inflate_series_updates_item_title).text = item.title
                        itemView.findViewById<TextView>(R.id.inflate_series_updates_item_season).text = item.season
                        itemView.findViewById<TextView>(R.id.inflate_series_updates_item_episode).text = item.episode
                        /*    if(!item.voice.isNullOrEmpty()){
                                itemView.findViewById<TextView>(R.id.inflate_series_updates_item_voice).text = item.voice
                            }*/

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
    }
}
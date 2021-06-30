package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.os.Bundle
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
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

    private var appliedFilters: ArrayMap<AppliedFilter, String> = ArrayMap()

    private lateinit var newestFilmsPresenter: NewestFilmsPresenter
    private lateinit var currentFragment: LinearLayout
    private lateinit var viewList: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentFragment = inflater.inflate(R.layout.fragment_newest_films, container, false) as LinearLayout

        progressBar = currentFragment.findViewById(R.id.fragment_films_list_pb_loading)

        // set up recycler view container
        viewList = currentFragment.findViewById(R.id.fragment_films_list_films_rv_films)
        viewList.layoutManager = GridLayoutManager(context, FILMS_PER_ROW)
        viewList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    progressBar.visibility = View.VISIBLE
                    newestFilmsPresenter.addFilms()
                    // Toast.makeText(activity?.applicationContext, appliedFilters.get(AppliedFilter.COUNTRY), Toast.LENGTH_SHORT).show()
                }
            }
        })

        newestFilmsPresenter = NewestFilmsPresenter(this)
        newestFilmsPresenter.initFilms()

        // createFilters()

        return currentFragment
    }

    override fun setFilms(films: ArrayList<Film>) {
        viewList.adapter = FilmsListRecyclerViewAdapter(films)
        progressBar.visibility = View.GONE
    }


    private fun createFilters() {
        /* val countries = resources.getStringArray(R.array.countries) //already array

         activity?.let {
             val builder = AlertDialog.Builder(it)
             // Get the layout inflater
             val view: LinearLayout = requireActivity().layoutInflater.inflate(R.layout.dialog_signin, null) as LinearLayout

             (view.findViewById<Spinner>(R.id.sp_countries)).onItemSelectedListener =
                 object : AdapterView.OnItemSelectedListener {
                     override fun onNothingSelected(parent: AdapterView<*>?) {

                     }

                     override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                         when (id) {
                             R.id.sp_countries -> {
                                 appliedFilters[AppliedFilter.COUNTRY] = countries[position]
                             }
                         }
                     }

                 }
             // Inflate and set the layout for the dialog
             // Pass null as the parent view because its going in the dialog layout
             builder.setView(view)
                 // Add action buttons
                 .setPositiveButton("set") { dialog, id ->
                     // sign in the user ...
                 }
                 .setNegativeButton("cancel") { dialog, id ->
                     // cancel
                 }
             builder.create()
             builder.show()
         }*/
    }
}
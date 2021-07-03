package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.presenters.FilmPresenter
import com.BSLCommunity.onlinefilmstracker.viewsInterface.FilmView
import com.squareup.picasso.Picasso

class FilmFragment : Fragment(), FilmView {
    private lateinit var filmPresenter: FilmPresenter
    private lateinit var currentFragment: View
    private lateinit var film: Film

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentFragment = inflater.inflate(R.layout.fragment_film, container, false)
        film = (arguments?.getSerializable("film") as Film?)!!

        filmPresenter = FilmPresenter(this)
        filmPresenter.initFilmData(film)

        return currentFragment
    }

    override fun setFilmData(film: Film) {
        currentFragment.findViewById<TextView>(R.id.fragment_film_tv_title).text = film.title
        currentFragment.findViewById<TextView>(R.id.fragment_film_tv_origtitle).text = film.origTitle
        if (film.ratingIMDB != null && film.votes != null) {
            currentFragment.findViewById<TextView>(R.id.fragment_film_tv_rating).text = "${film.ratingIMDB}\n${film.votes}"
        }
        Picasso.get().load(film.posterPath).into(currentFragment.findViewById<ImageView>(R.id.fragment_film_iv_poster))

        var directorsText = ""
        if (film.directors != null && film.directors!!.size > 0) {
            for ((index, director) in film.directors!!.withIndex()) {
                directorsText += director

                if (index != film.directors!!.size - 1) {
                    directorsText += ", "
                }
            }
        }
        currentFragment.findViewById<TextView>(R.id.fragment_film_tv_directors).text = getString(R.string.directors, directorsText)
        currentFragment.findViewById<TextView>(R.id.fragment_film_tv_releaseDate).text = getString(R.string.release_date, "${film.date} ${film.year}")
        currentFragment.findViewById<TextView>(R.id.fragment_film_tv_runtime).text = getString(R.string.runtime, film.runtime)

        var countriesText = ""
        if (film.countries.size > 0) {
            for ((index, country) in film.countries.withIndex()) {
                countriesText += country

                if (index != film.countries.size - 1) {
                    countriesText += ", "
                }
            }
        }
        currentFragment.findViewById<TextView>(R.id.fragment_film_tv_countries).text = getString(R.string.countries, countriesText)
        currentFragment.findViewById<TextView>(R.id.fragment_film_tv_type).text = getString(R.string.film_type, film.type)
        currentFragment.findViewById<TextView>(R.id.fragment_film_tv_plot).text = film.description


        val genresLayout: LinearLayout = currentFragment.findViewById(R.id.fragment_film_ll_genres)

        for (genre in film.genres) {
            val genreView = LayoutInflater.from(context).inflate(R.layout.inflate_tag, null) as TextView
            genreView.text = genre
            genresLayout.addView(genreView)
        }

        currentFragment.findViewById<ProgressBar>(R.id.fragment_film_pb_loading).visibility = View.GONE
        currentFragment.findViewById<LinearLayout>(R.id.fragment_film_ll_content).visibility = View.VISIBLE
    }
}
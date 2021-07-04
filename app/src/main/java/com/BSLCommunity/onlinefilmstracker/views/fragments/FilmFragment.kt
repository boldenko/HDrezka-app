package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.objects.Actor
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.presenters.FilmPresenter
import com.BSLCommunity.onlinefilmstracker.views.OnFragmentInteractionListener
import com.BSLCommunity.onlinefilmstracker.viewsInterface.FilmView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class FilmFragment : Fragment(), FilmView {
    private lateinit var filmPresenter: FilmPresenter
    private lateinit var currentFragment: View
    private lateinit var fragmentListener: OnFragmentInteractionListener
    private var modalDialog: Dialog? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentListener = context as OnFragmentInteractionListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentFragment = inflater.inflate(R.layout.fragment_film, container, false)
        currentFragment.findViewById<ProgressBar>(R.id.fragment_film_pb_loading).visibility = View.VISIBLE

        filmPresenter = FilmPresenter(this, (arguments?.getSerializable("film") as Film?)!!)
        filmPresenter.initFilmData()
        filmPresenter.createFulSizeImage()

        currentFragment.findViewById<ImageView>(R.id.fragment_film_iv_poster).setOnClickListener { openFullSizeImage() }
        /*     val webView = currentFragment.findViewById<WebView>(R.id.webView)
             webView.getSettings().javaScriptEnabled = true
             webView.getSettings().loadWithOverviewMode = true
             webView.getSettings().useWideViewPort = true
             webView.webViewClient = MyWebViewClient()
             webView.webChromeClient = activity?.let { MyChromeClient(it) }
             webView.loadUrl("http://hdrezka.tv/films/action/39912-voyna-buduschego-2021.html")*/

        return currentFragment
    }

    override fun setFilmBaseData(film: Film) {
        filmPresenter.initActors()

        Picasso.get().load(film.posterPath).into(currentFragment.findViewById<ImageView>(R.id.fragment_film_iv_poster))
        val ratingView: TextView = currentFragment.findViewById(R.id.fragment_film_tv_rating)
        if (film.ratingIMDB != null && film.votes != null) {
            ratingView.text = "${film.ratingIMDB}\n${film.votes}"
        } else {
            ratingView.visibility = View.GONE
        }
        currentFragment.findViewById<TextView>(R.id.fragment_film_tv_title).text = film.title
        currentFragment.findViewById<TextView>(R.id.fragment_film_tv_origtitle).text = film.origTitle
        currentFragment.findViewById<TextView>(R.id.fragment_film_tv_releaseDate).text = getString(R.string.release_date, "${film.date} ${film.year}")
        currentFragment.findViewById<TextView>(R.id.fragment_film_tv_runtime).text = getString(R.string.runtime, film.runtime)
        currentFragment.findViewById<TextView>(R.id.fragment_film_tv_type).text = getString(R.string.film_type, film.type)
        currentFragment.findViewById<TextView>(R.id.fragment_film_tv_plot).text = film.description

        currentFragment.findViewById<ProgressBar>(R.id.fragment_film_pb_loading).visibility = View.GONE
        currentFragment.findViewById<NestedScrollView>(R.id.fragment_film_sv_content).visibility = View.VISIBLE
    }

    override fun setActors(actors: ArrayList<Actor?>) {
        val actorsLayout: LinearLayout = currentFragment.findViewById(R.id.fragment_film_ll_actorsLayout)

        for (actor in actors.reversed()) {
            if (actor != null) {
                val layout: LinearLayout = LayoutInflater.from(context).inflate(R.layout.inflate_actor, null) as LinearLayout

                layout.findViewById<TextView>(R.id.actor_name).text = actor.name

                if (actor.photoLink.isNotEmpty() && actor.photoLink != "https://static.hdrezka.ac/i/nopersonphoto.png") {
                    val actorProgress: ProgressBar = layout.findViewById(R.id.actor_loading)
                    val actorLayout: LinearLayout = layout.findViewById(R.id.actor_layout)

                    actorProgress.visibility = View.VISIBLE
                    actorLayout.visibility = View.GONE
                    Picasso.get().load(actor.photoLink).into(layout.findViewById(R.id.actor_photo), object : Callback {
                        override fun onSuccess() {
                            actorProgress.visibility = View.GONE
                            actorLayout.visibility = View.VISIBLE
                            actorsLayout.addView(layout, 0)
                        }

                        override fun onError(e: Exception) {
                            e.printStackTrace()
                        }
                    })
                } else {
                    actorsLayout.addView(layout)
                }
            }
        }
    }

    override fun setDirectors(directors: ArrayList<String>) {
        var directorsText = ""
        for ((index, director) in directors.withIndex()) {
            directorsText += director

            if (index != directors.size - 1) {
                directorsText += ", "
            }
        }
        currentFragment.findViewById<TextView>(R.id.fragment_film_tv_directors).text = getString(R.string.directors, directorsText)
    }

    override fun setCountries(countries: ArrayList<String>) {
        var countriesText = ""
        for ((index, country) in countries.withIndex()) {
            countriesText += country

            if (index != countries.size - 1) {
                countriesText += ", "
            }
        }

        currentFragment.findViewById<TextView>(R.id.fragment_film_tv_countries).text = getString(R.string.countries, countriesText)
    }

    override fun setGenres(genres: ArrayList<String>) {
        val genresLayout: LinearLayout = currentFragment.findViewById(R.id.fragment_film_ll_genres)

        for (genre in genres) {
            val genreView = LayoutInflater.from(context).inflate(R.layout.inflate_tag, null) as TextView
            genreView.text = genre
            genresLayout.addView(genreView)
        }
    }

    override fun setFilmLink(link: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        currentFragment.findViewById<Button>(R.id.fragment_film_bt_film_link).setOnClickListener {
            startActivity(intent)
        }
    }

    override fun setFullSizeImage(posterPath: String) {
        val dialog: Dialog? = Dialog(requireActivity())
        val layout: LinearLayout = layoutInflater.inflate(R.layout.modal_image, null) as LinearLayout
        Picasso.get().load(posterPath).into(layout.findViewById(R.id.modal_image), object : Callback {
            override fun onSuccess() {
                layout.findViewById<ProgressBar>(R.id.modal_progress).visibility = View.GONE
                layout.findViewById<ImageView>(R.id.modal_image).visibility = View.VISIBLE
            }

            override fun onError(e: Exception) {
            }
        })
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(layout)
        modalDialog = dialog
    }

    private fun openFullSizeImage() {
        modalDialog?.show()
    }
}
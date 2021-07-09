package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.clients.PlayerChromeClient
import com.BSLCommunity.onlinefilmstracker.clients.PlayerWebViewClient
import com.BSLCommunity.onlinefilmstracker.models.UserModel
import com.BSLCommunity.onlinefilmstracker.objects.Actor
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.presenters.FilmPresenter
import com.BSLCommunity.onlinefilmstracker.views.OnFragmentInteractionListener
import com.BSLCommunity.onlinefilmstracker.viewsInterface.FilmView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class FilmFragment : Fragment(), FilmView {
    private lateinit var currentView: View
    private lateinit var filmPresenter: FilmPresenter
    private lateinit var fragmentListener: OnFragmentInteractionListener
    private lateinit var playerView: WebView
    private var modalDialog: Dialog? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentListener = context as OnFragmentInteractionListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_film, container, false)
        Log.d("FRAGMENT_TEST", "film init")

        currentView.findViewById<ProgressBar>(R.id.fragment_film_pb_loading).visibility = View.VISIBLE
        playerView = currentView.findViewById(R.id.fragment_film_wv_player)

        filmPresenter = FilmPresenter(this, (arguments?.getSerializable("film") as Film?)!!)
        filmPresenter.initFilmData()
        filmPresenter.initPlayer()

        currentView.findViewById<ImageView>(R.id.fragment_film_iv_poster).setOnClickListener { openFullSizeImage() }

        return currentView
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun setPlayer(link: String) {
        playerView.settings.javaScriptEnabled = true
        playerView.webViewClient = PlayerWebViewClient {
            currentView.findViewById<ProgressBar>(R.id.fragment_film_pb_player_loading).visibility = View.GONE
            playerView.visibility = View.VISIBLE
        }

        playerView.webChromeClient = activity?.let { PlayerChromeClient(it) }
        playerView.loadUrl(link)
    }

    override fun setFilmBaseData(film: Film) {
        filmPresenter.initActors()
        filmPresenter.initFullSizeImage()

        Picasso.get().load(film.posterPath).into(currentView.findViewById<ImageView>(R.id.fragment_film_iv_poster))
        val ratingView: TextView = currentView.findViewById(R.id.fragment_film_tv_rating)
        if (film.ratingIMDB != null && film.votes != null) {
            ratingView.text = "${film.ratingIMDB}\n${film.votes}"
        } else {
            ratingView.visibility = View.GONE
        }
        currentView.findViewById<TextView>(R.id.fragment_film_tv_title).text = film.title
        currentView.findViewById<TextView>(R.id.fragment_film_tv_origtitle).text = film.origTitle
        currentView.findViewById<TextView>(R.id.fragment_film_tv_releaseDate).text = getString(R.string.release_date, "${film.date} ${film.year}")
        currentView.findViewById<TextView>(R.id.fragment_film_tv_runtime).text = getString(R.string.runtime, film.runtime)
        currentView.findViewById<TextView>(R.id.fragment_film_tv_type).text = getString(R.string.film_type, film.type)
        currentView.findViewById<TextView>(R.id.fragment_film_tv_plot).text = film.description

        currentView.findViewById<ProgressBar>(R.id.fragment_film_pb_loading).visibility = View.GONE
        currentView.findViewById<NestedScrollView>(R.id.fragment_film_sv_content).visibility = View.VISIBLE
    }

    override fun setActors(actors: ArrayList<Actor?>) {
        val actorsLayout: LinearLayout = currentView.findViewById(R.id.fragment_film_ll_actorsLayout)

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
        currentView.findViewById<TextView>(R.id.fragment_film_tv_directors).text = getString(R.string.directors, directorsText)
    }

    override fun setCountries(countries: ArrayList<String>) {
        var countriesText = ""
        for ((index, country) in countries.withIndex()) {
            countriesText += country

            if (index != countries.size - 1) {
                countriesText += ", "
            }
        }

        currentView.findViewById<TextView>(R.id.fragment_film_tv_countries).text = getString(R.string.countries, countriesText)
    }

    override fun setGenres(genres: ArrayList<String>) {
        val genresLayout: LinearLayout = currentView.findViewById(R.id.fragment_film_ll_genres)

        for (genre in genres) {
            val genreView = LayoutInflater.from(context).inflate(R.layout.inflate_tag, null) as TextView
            genreView.text = genre
            genresLayout.addView(genreView)
        }
    }

    override fun setFilmLink(link: String) {
        /*val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        currentFragment.findViewById<Button>(R.id.fragment_film_bt_film_link).setOnClickListener {
            startActivity(intent)
        }*/
    }

    override fun setFullSizeImage(posterPath: String) {
        val dialog = Dialog(requireActivity())
        val layout: LinearLayout = layoutInflater.inflate(R.layout.modal_image, null) as LinearLayout
        Picasso.get().load(posterPath).into(layout.findViewById(R.id.modal_image), object : Callback {
            override fun onSuccess() {
                layout.findViewById<ProgressBar>(R.id.modal_progress).visibility = View.GONE
                layout.findViewById<ImageView>(R.id.modal_image).visibility = View.VISIBLE
            }

            override fun onError(e: Exception) {
            }
        })
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(layout)

        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window?.attributes);
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = lp;

        modalDialog = dialog
    }

    private fun openFullSizeImage() {
        modalDialog?.show()
    }
}
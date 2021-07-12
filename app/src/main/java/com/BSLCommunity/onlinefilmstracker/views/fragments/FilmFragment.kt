package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.webkit.WebView
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.clients.PlayerChromeClient
import com.BSLCommunity.onlinefilmstracker.clients.PlayerWebViewClient
import com.BSLCommunity.onlinefilmstracker.models.UserModel
import com.BSLCommunity.onlinefilmstracker.objects.*
import com.BSLCommunity.onlinefilmstracker.presenters.FilmPresenter
import com.BSLCommunity.onlinefilmstracker.utils.FragmentOpener
import com.BSLCommunity.onlinefilmstracker.utils.UnitsConverter
import com.BSLCommunity.onlinefilmstracker.interfaces.OnFragmentInteractionListener
import com.BSLCommunity.onlinefilmstracker.views.adapters.CommentsRecyclerViewAdapter
import com.BSLCommunity.onlinefilmstracker.views.viewsInterface.FilmView
import com.github.aakira.expandablelayout.ExpandableLinearLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class FilmFragment : Fragment(), FilmView {
    private lateinit var currentView: View
    private lateinit var filmPresenter: FilmPresenter
    private lateinit var fragmentListener: OnFragmentInteractionListener
    private lateinit var playerView: WebView
    private lateinit var scrollView: NestedScrollView
    private lateinit var commentsList: RecyclerView
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
        commentsList = currentView.findViewById(R.id.fragment_film_rv_comments)

        filmPresenter = FilmPresenter(this, (arguments?.getSerializable("film") as Film?)!!)
        filmPresenter.initFilmData()
        filmPresenter.initPlayer()

        scrollView = currentView.findViewById(R.id.fragment_film_sv_content)
        scrollView.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener {
            override fun onScrollChange(v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
                val view = scrollView.getChildAt(scrollView.childCount - 1)
                val diff = view.bottom - (scrollView.height + scrollView.scrollY)

                if (diff == 0) {
                    filmPresenter.initComments()
                    filmPresenter.getNextComments()
                }
            }
        })

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
        scrollView.visibility = View.VISIBLE
    }

    override fun setActors(actors: ArrayList<Actor?>) {
        val actorsLayout: LinearLayout = currentView.findViewById(R.id.fragment_film_ll_actorsLayout)

        context?.let {
            for (actor in actors.reversed()) {
                if (actor != null) {
                    val layout: LinearLayout = LayoutInflater.from(it).inflate(R.layout.inflate_actor, null) as LinearLayout

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

    override fun setSchedule(schedule: ArrayList<Pair<String, ArrayList<Schedule>>>) {
        if (schedule.size == 0) {
            currentView.findViewById<TextView>(R.id.fragment_film_tv_schedule_header).visibility = View.GONE
            return
        }

        // get mount layout
        val scheduleLayout: LinearLayout = currentView.findViewById(R.id.fragment_film_ll_schedule)
        for (sch in schedule) {
            // create season layout
            val layout: LinearLayout = layoutInflater.inflate(R.layout.inflate_schedule_layout, null) as LinearLayout
            val expandedList: ExpandableLinearLayout = layout.findViewById(R.id.inflate_layout_list)
            layout.findViewById<TextView>(R.id.inflate_layout_header).text = sch.first
            layout.findViewById<LinearLayout>(R.id.inflate_layout_button).setOnClickListener {
                expandedList.toggle()
            }

            // fill episodes layout
            for (item in sch.second) {
                val itemLayout: LinearLayout = layoutInflater.inflate(R.layout.inflate_schedule_item, null) as LinearLayout
                itemLayout.findViewById<TextView>(R.id.inflate_item_episode).text = item.episode
                itemLayout.findViewById<TextView>(R.id.inflate_item_name).text = item.name
                itemLayout.findViewById<TextView>(R.id.inflate_item_date).text = item.date
                itemLayout.findViewById<TextView>(R.id.inflate_item_next_episode).text = item.nextEpisodeIn
                expandedList.addView(itemLayout)
            }
            scheduleLayout.addView(layout)
        }
    }

    override fun setCollection(collection: ArrayList<Film>) {
        if (collection.size == 0) {
            currentView.findViewById<TextView>(R.id.fragment_film_tv_collection_header).visibility = View.GONE
            return
        }

        val collectionLayout: LinearLayout = currentView.findViewById(R.id.fragment_film_tv_collection_list)
        for ((index, film) in collection.withIndex()) {
            val layout: LinearLayout = layoutInflater.inflate(R.layout.inflate_colletion_item, null) as LinearLayout
            layout.findViewById<TextView>(R.id.inflate_collection_item_n).text = (index + 1).toString()
            layout.findViewById<TextView>(R.id.inflate_collection_item_name).text = film.title
            layout.findViewById<TextView>(R.id.inflate_collection_item_year).text = film.year
            layout.findViewById<TextView>(R.id.inflate_collection_item_rating).text = film.ratingKP

            if (film.link.isNotEmpty()) {
                val outValue = TypedValue()
                requireContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
                layout.setBackgroundResource(outValue.resourceId)
                layout.setOnClickListener {
                    FragmentOpener.openFilm(film, this, fragmentListener)
                }
            } else {
                layout.findViewById<TextView>(R.id.inflate_collection_item_name).setTextColor(requireContext().getColor(R.color.gray))
            }
            collectionLayout.addView(layout)
        }
    }

    override fun setRelated(relatedList: ArrayList<Film>) {
        val relatedLayout = currentView.findViewById<LinearLayout>(R.id.fragment_film_tv_related_list)

        for (film in relatedList) {
            val layout: LinearLayout = layoutInflater.inflate(R.layout.inflate_film, null) as LinearLayout
            val titleView: TextView = layout.findViewById(R.id.film_title)
            val infoView: TextView = layout.findViewById(R.id.film_info)
            layout.findViewById<TextView>(R.id.film_type).visibility = View.GONE
            titleView.text = film.title
            titleView.textSize = 10F
            infoView.text = film.relatedMisc
            infoView.textSize = 10F

            val filmPoster: ImageView = layout.findViewById(R.id.film_poster)
            Picasso.get().load(film.posterPath).into(filmPoster, object : Callback {
                override fun onSuccess() {
                    layout.findViewById<ProgressBar>(R.id.film_loading).visibility = View.GONE
                    layout.findViewById<RelativeLayout>(R.id.film_posterLayout).visibility = View.VISIBLE
                }

                override fun onError(e: Exception) {
                }
            })

            val params = LinearLayout.LayoutParams(
                UnitsConverter.getPX(requireContext(), 80),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val m = UnitsConverter.getPX(requireContext(), 5)
            params.setMargins(m, m, m, m)
            layout.layoutParams = params
            layout.setOnClickListener {
                FragmentOpener.openFilm(film, this, fragmentListener)
            }

            relatedLayout.addView(layout)
        }
    }

    override fun setBookmarksList(bookmarks: ArrayList<Bookmark>) {
        val btn: ImageView = currentView.findViewById(R.id.fragment_film_iv_bookmark)
        if (UserData.isLoggedIn == true) {
            val data: Array<String?> = arrayOfNulls(bookmarks.size)
            val checkedItems = BooleanArray(bookmarks.size)

            for ((index, bookmark) in bookmarks.withIndex()) {
                data[index] = bookmark.name
                checkedItems[index] = bookmark.isChecked == true
            }

            activity?.let {
                val builder = MaterialAlertDialogBuilder(it)
                builder.setTitle("Выбери разделы закладок")
                builder.setMultiChoiceItems(data, checkedItems) { dialog, which, isChecked ->
                    filmPresenter.setBookmark(bookmarks[which].catId)
                    checkedItems[which] = isChecked
                }
                builder.setPositiveButton("ОК") { dialog, id ->
                    dialog.dismiss()
                }
                val d = builder.create()
                btn.setOnClickListener {
                    d.show()
                }
            }
        } else {
            btn.visibility = View.GONE
        }
    }

    override fun setCommentsList(list: ArrayList<Comment>) {
        commentsList.adapter = CommentsRecyclerViewAdapter(requireContext(), list)
    }

    override fun redrawComments() {
        commentsList.adapter?.notifyDataSetChanged()
    }

    override fun setCommentsProgressState(state: Boolean) {
        currentView.findViewById<ProgressBar>(R.id.fragment_film_pb_comments_loading).visibility = if (state) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}
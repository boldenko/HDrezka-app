package com.falcofemoralis.hdrezkaapp.views.fragments

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.*
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import android.view.inputmethod.InputMethodManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.*
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.underline
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.clients.PlayerChromeClient
import com.falcofemoralis.hdrezkaapp.clients.PlayerWebViewClient
import com.falcofemoralis.hdrezkaapp.constants.UpdateItem
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.interfaces.OnFragmentInteractionListener
import com.falcofemoralis.hdrezkaapp.models.ActorModel
import com.falcofemoralis.hdrezkaapp.objects.*
import com.falcofemoralis.hdrezkaapp.presenters.FilmPresenter
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper
import com.falcofemoralis.hdrezkaapp.utils.FragmentOpener
import com.falcofemoralis.hdrezkaapp.utils.UnitsConverter
import com.falcofemoralis.hdrezkaapp.views.MainActivity
import com.falcofemoralis.hdrezkaapp.views.adapters.CommentsRecyclerViewAdapter
import com.falcofemoralis.hdrezkaapp.views.elements.CommentEditor
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmView
import com.github.aakira.expandablelayout.ExpandableLinearLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.willy.ratingbar.ScaleRatingBar


class FilmFragment : Fragment(), FilmView {
    private val FILM_ARG = "film"
    private lateinit var currentView: View
    private lateinit var filmPresenter: FilmPresenter
    private lateinit var fragmentListener: OnFragmentInteractionListener
    private lateinit var playerView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollView: NestedScrollView
    private lateinit var commentsList: RecyclerView
    private lateinit var imm: InputMethodManager
    private var commentsAdded: Boolean = false
    private var modalDialog: Dialog? = null
    private var commentEditor: CommentEditor? = null
    private var bookmarksDialog: AlertDialog? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentListener = context as OnFragmentInteractionListener
    }

    override fun onDestroy() {
        playerView.destroy()
        activity?.window?.clearFlags(FLAG_KEEP_SCREEN_ON)

        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_film, container, false)

        progressBar = currentView.findViewById(R.id.fragment_film_pb_loading)
        playerView = currentView.findViewById(R.id.fragment_film_wv_player)
        commentsList = currentView.findViewById(R.id.fragment_film_rv_comments)
        activity?.window?.addFlags(FLAG_KEEP_SCREEN_ON)

        filmPresenter = FilmPresenter(this, (arguments?.getSerializable(FILM_ARG) as Film?)!!)
        filmPresenter.initFilmData()
        filmPresenter.initPlayer()

        scrollView = currentView.findViewById(R.id.fragment_film_sv_content)
        scrollView.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener {
            override fun onScrollChange(v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
                val view = scrollView.getChildAt(scrollView.childCount - 1)
                val diff = view.bottom - (scrollView.height + scrollView.scrollY)

                if (diff == 0) {
                    if (!commentsAdded) {
                        filmPresenter.initComments()
                        commentsAdded = true
                    }
                    filmPresenter.getNextComments()
                }
            }
        })
        scrollView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        currentView.findViewById<ImageView>(R.id.fragment_film_iv_poster).setOnClickListener { openFullSizeImage() }

        return currentView
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun setPlayer(link: String) {
        val container: LinearLayout = currentView.findViewById(R.id.fragment_film_ll_player_container)

        playerView.settings.javaScriptEnabled = true
        playerView.settings.domStorageEnabled = true
        playerView.addJavascriptInterface(WebAppInterface(requireActivity()), "Android")
        playerView.webViewClient = PlayerWebViewClient(requireContext(), this) {
            container.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            currentView.findViewById<ProgressBar>(R.id.fragment_film_pb_player_loading).visibility = View.GONE
            playerView.visibility = View.VISIBLE
        }

        playerView.webChromeClient = activity?.let { PlayerChromeClient(it) }
        playerView.loadUrl(link)
    }

    class WebAppInterface(private val act: FragmentActivity) {
        @JavascriptInterface
        fun updateWatchLater() {
            (act as MainActivity).redrawPage(UpdateItem.WATCH_LATER_CHANGED)
        }
    }

    override fun setFilmBaseData(film: Film) {
        filmPresenter.initActors()
        filmPresenter.initFullSizeImage()

        Picasso.get().load(film.posterPath).into(currentView.findViewById<ImageView>(R.id.fragment_film_iv_poster))

        currentView.findViewById<TextView>(R.id.fragment_film_tv_title).text = film.title
        currentView.findViewById<TextView>(R.id.fragment_film_tv_origtitle).text = film.origTitle

        val dateView = currentView.findViewById<TextView>(R.id.fragment_film_tv_releaseDate)
        if (film.date != null) {
            dateView.text = getString(R.string.release_date, "${film.date} ${film.year}")
        } else {
            dateView.visibility = View.GONE
        }
        currentView.findViewById<TextView>(R.id.fragment_film_tv_runtime).text = getString(R.string.runtime, film.runtime)
        currentView.findViewById<TextView>(R.id.fragment_film_tv_type).text = getString(R.string.film_type, film.type)
        currentView.findViewById<TextView>(R.id.fragment_film_tv_plot).text = film.description

        // data loaded
        scrollView.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    override fun setFilmRatings(film: Film) {
        setRating(R.id.fragment_film_tv_ratingIMDB, R.string.imdb, film.ratingIMDB, film.votesIMDB)
        setRating(R.id.fragment_film_tv_ratingKP, R.string.kp, film.ratingKP, film.votesKP)
        setRating(R.id.fragment_film_tv_ratingWA, R.string.wa, film.ratingWA, film.votesWA)
        setRating(R.id.fragment_film_tv_ratingHR, R.string.hr, film.ratingHR, "(${film.votesHR})")
    }

    private fun setRating(viewId: Int, stringId: Int, rating: String?, votes: String?) {
        val ratingTextView: TextView = currentView.findViewById(viewId)
        if (rating != null && votes != null && rating.isNotEmpty() && votes.isNotEmpty()) {
            val ss = SpannableStringBuilder()
            ss.bold { ss.underline { append(getString(stringId)) } }
            ss.append(": $rating $votes")
            ratingTextView.text = ss
        } else {
            ratingTextView.visibility = View.GONE
        }
    }

    override fun setActors(actors: ArrayList<Actor>?) {
        val actorsLayout: LinearLayout = currentView.findViewById(R.id.fragment_film_ll_actorsLayout)

        if (actors == null) {
            return
        }

        context?.let {
            for (actor in actors.reversed()) {
                val layout: LinearLayout = LayoutInflater.from(it).inflate(R.layout.inflate_actor, null) as LinearLayout

                layout.findViewById<TextView>(R.id.actor_name).text = actor.name
                layout.findViewById<TextView>(R.id.actor_career).text = actor.careers

                if (actor.photo != null && actor.photo!!.isNotEmpty() && actor.photo != SettingsData.staticProvider + ActorModel.NO_PHOTO) {
                    val actorProgress: ProgressBar = layout.findViewById(R.id.actor_loading)
                    val actorLayout: LinearLayout = layout.findViewById(R.id.actor_layout)

                    actorProgress.visibility = View.VISIBLE
                    actorLayout.visibility = View.GONE
                    Picasso.get().load(actor.photo).into(layout.findViewById(R.id.actor_photo), object : Callback {
                        override fun onSuccess() {
                            actorProgress.visibility = View.GONE
                            actorLayout.visibility = View.VISIBLE
                            actorsLayout.addView(layout, 0)
                        }

                        override fun onError(e: Exception) {
                            e.printStackTrace()
                        }
                    })
                    actorLayout.setOnClickListener {
                        FragmentOpener.openWithData(this, fragmentListener, actor, "actor")
                    }
                } else {
                    actorsLayout.addView(layout)
                }

            }
        }
    }

    override fun setDirectors(directors: ArrayList<Actor>) {
        val directorsView: TextView = currentView.findViewById(R.id.fragment_film_tv_directors)
        val spannablePersonNamesList: ArrayList<SpannableString> = ArrayList()
        for (director in directors) {
            spannablePersonNamesList.add(setClickableActorName(directorsView, director))
        }

        directorsView.movementMethod = LinkMovementMethod.getInstance()
        directorsView.text = getString(R.string.directors)
        directorsView.append(" ")
        for ((index, item) in spannablePersonNamesList.withIndex()) {
            directorsView.append(item)

            if (index != spannablePersonNamesList.size - 1) {
                directorsView.append(", ")
            }
        }
    }

    private fun setClickableActorName(textView: TextView, actor: Actor): SpannableString {
        val ss = SpannableString(actor.name)
        val fr = this
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
               FragmentOpener.openWithData(fr, fragmentListener, actor, "actor")
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        ss.setSpan(clickableSpan, 0, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return ss
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

    override fun setFullSizeImage(posterPath: String) {
        val dialog = Dialog(requireActivity())
        val layout: RelativeLayout = layoutInflater.inflate(R.layout.modal_image, null) as RelativeLayout
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
        layout.findViewById<Button>(R.id.modal_bt_close).setOnClickListener {
            dialog.dismiss()
        }
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
            for ((i, item) in sch.second.withIndex()) {
                val itemLayout: LinearLayout = layoutInflater.inflate(R.layout.inflate_schedule_item, null) as LinearLayout
                itemLayout.findViewById<TextView>(R.id.inflate_item_episode).text = item.episode
                itemLayout.findViewById<TextView>(R.id.inflate_item_name).text = item.name
                itemLayout.findViewById<TextView>(R.id.inflate_item_date).text = item.date

                val watchBtn = itemLayout.findViewById<ImageView>(R.id.inflate_item_watch)
                val nextEpisodeIn = itemLayout.findViewById<TextView>(R.id.inflate_item_next_episode)
                if (item.nextEpisodeIn == "✓" || item.nextEpisodeIn == "сегодня") {
                    watchBtn.visibility = View.VISIBLE

                    changeWatchState(item.isWatched, watchBtn)
                    watchBtn.setOnClickListener {
                        if (UserData.isLoggedIn == true) {
                            filmPresenter.updateWatch(item, watchBtn)
                        } else {
                            Toast.makeText(requireContext(), getString(R.string.need_register), Toast.LENGTH_SHORT).show()
                        }
                    }
                    nextEpisodeIn.visibility = View.GONE
                } else {
                    watchBtn.visibility = View.GONE
                    nextEpisodeIn.visibility = View.VISIBLE
                    nextEpisodeIn.text = item.nextEpisodeIn
                }


                val color = if (i % 2 == 0) {
                    R.color.light_bg
                } else {
                    R.color.dark_background
                }
                itemLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), color))

                expandedList.addView(itemLayout)
            }
            scheduleLayout.addView(layout)
        }
    }

    override fun changeWatchState(state: Boolean, btn: ImageView) {
        if (state) {
            ImageViewCompat.setImageTintList(btn, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.main_color_3)))
        } else {
            ImageViewCompat.setImageTintList(btn, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)))
        }
    }

    override fun setCollection(collection: ArrayList<Film>) {
        if (collection.size == 0) {
            currentView.findViewById<TextView>(R.id.fragment_film_tv_collection_header).visibility = View.GONE
            return
        }

        val collectionLayout: LinearLayout = currentView.findViewById(R.id.fragment_film_tv_collection_list)
        for (i in collection.lastIndex downTo 0) {
            val film = collection.reversed()[i]
            val layout: LinearLayout = layoutInflater.inflate(R.layout.inflate_colletion_item, null) as LinearLayout
            layout.findViewById<TextView>(R.id.inflate_collection_item_n).text = (i + 1).toString()
            layout.findViewById<TextView>(R.id.inflate_collection_item_name).text = film.title
            layout.findViewById<TextView>(R.id.inflate_collection_item_year).text = film.year
            layout.findViewById<TextView>(R.id.inflate_collection_item_rating).text = film.ratingKP

            if (film.filmLink?.isNotEmpty() == true) {
                val outValue = TypedValue()
                requireContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
                layout.setBackgroundResource(outValue.resourceId)
                layout.setOnClickListener {
                    FragmentOpener.openWithData(this, fragmentListener, film, "film")
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
            titleView.textSize = 12F
            infoView.text = film.relatedMisc
            infoView.textSize = 12F

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
                FragmentOpener.openWithData(this, fragmentListener, film, "film")
            }

            relatedLayout.addView(layout)
        }
    }

    override fun updateBookmarksPager() {
        requireActivity().let {
            (it as MainActivity).redrawPage(UpdateItem.BOOKMARKS_CHANGED)
        }
    }

    override fun updateBookmarksFilmsPager() {
        requireActivity().let {
            (it as MainActivity).redrawPage(UpdateItem.BOOKMARKS_FILMS_CHANGED)
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
                builder.setTitle(getString(R.string.choose_bookmarks))
                builder.setMultiChoiceItems(data, checkedItems) { dialog, which, isChecked ->
                    filmPresenter.setBookmark(bookmarks[which].catId)
                    updateBookmarksFilmsPager()
                    checkedItems[which] = isChecked
                }
                builder.setPositiveButton(getString(R.string.ok)) { dialog, id ->
                    dialog.dismiss()
                }

                // new catalog btn
                val catalogDialogBuilder = MaterialAlertDialogBuilder(it)
                catalogDialogBuilder.setTitle(getString(R.string.new_cat))

                val dialogCatLayout: LinearLayout = layoutInflater.inflate(R.layout.dialog_new_cat, null) as LinearLayout
                val input: EditText = dialogCatLayout.findViewById(R.id.dialog_cat_input)

                catalogDialogBuilder.setView(dialogCatLayout)
                catalogDialogBuilder.setPositiveButton(getString(R.string.ok)) { dialog, id ->
                    filmPresenter.createNewCatalogue(input.text.toString())
                    Toast.makeText(requireContext(), getString(R.string.created_cat), Toast.LENGTH_SHORT).show()
                }
                catalogDialogBuilder.setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                    dialog.cancel()
                }

                val n = catalogDialogBuilder.create()

                builder.setNeutralButton(getString(R.string.new_cat)) { dialog, id ->
                    n.show()
                }

                if (bookmarksDialog != null) {
                    bookmarksDialog?.dismiss()
                }
                bookmarksDialog = builder.create()
                btn.setOnClickListener {
                    bookmarksDialog?.show()
                }
            }
        } else {
            currentView.findViewById<LinearLayout>(R.id.fragment_film_ll_title_layout).layoutParams = LinearLayout.LayoutParams(0, WindowManager.LayoutParams.WRAP_CONTENT, 0.85f)
            btn.visibility = View.GONE
        }
    }

    override fun setShareBtn(title: String, link: String) {
        val btn: ImageView = currentView.findViewById(R.id.fragment_film_iv_share)
        btn.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            val body: String = getString(R.string.share_body, title, link)
            sharingIntent.putExtra(Intent.EXTRA_TEXT, body)
            startActivity(sharingIntent)
        }
    }

    override fun showConnectionError(type: IConnection.ErrorType) {
        ExceptionHelper.showToastError(requireContext(), type)
    }

    override fun setCommentsList(list: ArrayList<Comment>, filmId: String) {
        commentsList.adapter = CommentsRecyclerViewAdapter(requireContext(), list, commentEditor, this, this)
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

    override fun setCommentEditor(filmId: String) {
        val commentEditorCont: LinearLayout = currentView.findViewById(R.id.fragment_film_ll_comment_editor_container) as LinearLayout
        val commentEditorOpener: TextView = currentView.findViewById<TextView>(R.id.fragment_film_view_comment_editor_opener)

        if (UserData.isLoggedIn == true) {
            commentEditor = CommentEditor(commentEditorCont, requireContext(), filmId, this, this)
            imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            commentEditorOpener.setOnClickListener {
                commentEditor?.setCommentSource(0, 0, 0, "")
                changeCommentEditorState(true)
            }
        } else {
            commentEditorCont.visibility = View.GONE
            commentEditorOpener.visibility = View.GONE
        }
    }

    override fun changeCommentEditorState(isKeyboard: Boolean) {
        if (commentEditor != null) {
            if (commentEditor?.editorContainer?.visibility == View.VISIBLE) {
                if (commentsAdded) {
                    if (isKeyboard) imm.hideSoftInputFromWindow(commentEditor?.textArea?.windowToken, 0)
                    commentEditor?.editorContainer?.animate()?.translationY(commentEditor?.editorContainer?.height!!.toFloat())?.setListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator?) {
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            commentEditor?.editorContainer?.visibility = View.GONE
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                        }

                        override fun onAnimationRepeat(animation: Animator?) {
                        }
                    })
                }
            } else {
                commentEditor?.editorContainer?.visibility = View.VISIBLE
                commentEditor?.textArea?.requestFocus()
                if (isKeyboard) imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                commentEditor?.editorContainer?.animate()?.translationY(0F)?.setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationRepeat(animation: Animator?) {
                    }
                })
            }
        }
    }

    override fun onCommentPost(comment: Comment, position: Int) {
        filmPresenter.addComment(comment, position)
        commentEditor?.editorContainer?.visibility = View.GONE
    }

    override fun onDialogVisible() {
        commentEditor?.editorContainer?.visibility = View.GONE
        imm.hideSoftInputFromWindow(commentEditor?.textArea?.windowToken, 0)
    }

    override fun onNothingEntered() {
        Toast.makeText(requireContext(), getString(R.string.enter_comment_text), Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setHRrating(rating: Float, isActive: Boolean) {
        val selectableRatingBar: ScaleRatingBar = currentView.findViewById(R.id.fragment_film_srb_rating_hdrezka_select)
        val ratingBar: ScaleRatingBar = currentView.findViewById(R.id.fragment_film_srb_rating_hdrezka)

        selectableRatingBar.setIsIndicator(isActive)
        ratingBar.rating = rating
        selectableRatingBar.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                selectableRatingBar.visibility = View.GONE
                filmPresenter.updateRating(selectableRatingBar.rating)
            }
            false
        }
    }
}
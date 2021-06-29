package com.BSLCommunity.onlinefilmstracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.objects.Film
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.IOException


class FilmsListFragment : Fragment() {
    private lateinit var currentFragment: LinearLayout
    private lateinit var viewList: RecyclerView
    private lateinit var elements: Elements
    private lateinit var progressBar: ProgressBar
    private lateinit var doc: Document
    private val films: ArrayList<Film> = ArrayList()
    private var isLoading: Boolean = false
    private var currentPage = 1

    private val FILMS_PER_PAGE: Int = 9
    private val HDREZKA_NEWEST = "http://hdrezka.tv/new/page/"
    private val FILMS = "div.b-content__inline_item"
    private val FILM_LINK = "div.b-content__inline_item-cover a"
    private val FILM_TITLE = "div.b-post__title h1"
    private val FILM_POSTER = "div.b-sidecover a img"
    private val FILM_RATING_IMDB = "span.b-post__info_r"
    private val FILM_TABLE_INFO = "table.b-post__info tbody tr"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        currentFragment = inflater.inflate(
            R.layout.fragment_films_list,
            container,
            false
        ) as LinearLayout

        progressBar = currentFragment.findViewById(R.id.fragment_films_list_pb_loading)

        // set listener on scroll end for loading films
        viewList = currentFragment.findViewById(R.id.fragment_films_list_films_rv_films)
        viewList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && !isLoading) {
                    progressBar.visibility = View.VISIBLE
                    addFilms()
                }
            }
        })

        // load films at start
        initFilms()

        return currentFragment
    }


    private fun initFilms() {
        viewList.layoutManager = GridLayoutManager(context, 3)

        GlobalScope.launch {
            getNextFilmsPage() // load films from 1 page
            addFilms() // show films
        }
    }

    private fun addFilms() {
        GlobalScope.launch {
            isLoading = true
            var count = 0 // films added
            val elementsCopy: Elements = elements.clone()

            for (element in elementsCopy) {
                // if films loaded more than 9, break!
                if (count > (FILMS_PER_PAGE - 1)) {
                    break
                }

                // load film and add it to arrayList
                val film: Film? = getFilm(element.select(FILM_LINK).attr("href"))
                if (film != null) {
                    films.add(film)
                    count++
                }

                elements.removeAt(0)
            }

            // if films loaded more than 0, update adapter
            if (count > 0) {
                withContext(Dispatchers.Main) {
                    viewList.adapter = FilmsListRecyclerViewAdapter(films)
                    isLoading = false
                    progressBar.visibility = View.GONE
                }
            } else {
                // load films from next page
                elements.clear()
                getNextFilmsPage()
                addFilms()
            }
        }
    }

    private fun getNextFilmsPage() {
        doc = Jsoup.connect(HDREZKA_NEWEST + currentPage).get()
        elements = doc.select(FILMS)
        currentPage++
    }

    private fun getFilm(link: String): Film? {
        return try {
            val filmPage: Document = Jsoup.connect(link).get()
            val table: Elements = filmPage.select(FILM_TABLE_INFO)

            val title: String = filmPage.select(FILM_TITLE).text();
            val poster: String = filmPage.select(FILM_POSTER).attr("src")
            val ratingIMDB: String = filmPage.select(FILM_RATING_IMDB).select("span").text()
            var date: String = ""
            var country: String = ""

            for (tr in table) {
                val td: Elements = tr.select("td")
                if (td[0].select("h2").text().equals("Дата выхода")) {
                    date = td[1].text()
                }

                if (td[0].select("h2").text().equals("Страна")) {
                    country = td[1].select("a").text()
                }
            }

            Film(title, date, poster, country, ratingIMDB, ArrayList())
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun createFilters() {
        activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.dialog_signin, null))
                // Add action buttons
                .setPositiveButton(
                    "set"
                ) { dialog, id ->
                    // sign in the user ...
                }
                .setNegativeButton(
                    "cancel"
                ) { dialog, id ->
                    // cancel
                }
            builder.create()
            builder.show()
        }
    }

}
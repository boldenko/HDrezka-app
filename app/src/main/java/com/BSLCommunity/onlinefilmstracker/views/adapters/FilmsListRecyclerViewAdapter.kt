package com.BSLCommunity.onlinefilmstracker.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class FilmsListRecyclerViewAdapter(private val context: Context, private val films: ArrayList<Film>, private val openFilm: (film: Film) -> Unit) :
    RecyclerView.Adapter<FilmsListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.inflate_film, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val film = films[position]
        Picasso.get().load(film.posterPath).into(holder.filmPoster, object : Callback {
            override fun onSuccess() {
                holder.progressView.visibility = View.GONE
                holder.posterLayoutView.visibility = View.VISIBLE
            }

            override fun onError(e: Exception) {
            }
        })
        holder.titleView.text = film.title

        var info = ""
        info += film.year
        film.countries?.let {
            if (film.countries!!.size > 0) {
                info += ", " + film.countries!![0]
            }
        }
        if (film.ratingIMDB?.isNotEmpty() == true) {
            info += ", " + film.ratingIMDB
        }

        holder.infoView.text = info
        holder.typeView.text = film.type

        val color: Int
        val res = context.resources
        color = when (film.type?.take(4)) {
            res.getString(R.string.films).take(4) -> ContextCompat.getColor(context, R.color.film)
            res.getString(R.string.multfilms).take(4) -> ContextCompat.getColor(context, R.color.multfilm)
            res.getString(R.string.serials).take(4) -> ContextCompat.getColor(context, R.color.serial)
            res.getString(R.string.anime).take(4) -> ContextCompat.getColor(context, R.color.anime)
            else -> ContextCompat.getColor(context, R.color.background)
        }
        holder.typeView.setBackgroundColor(color)

        holder.layout.setOnClickListener {
            openFilm(film)
        }
    }

    override fun getItemCount(): Int = films.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: LinearLayout = view.findViewById(R.id.film_layout)
        val filmPoster: ImageView = view.findViewById(R.id.film_poster)
        val titleView: TextView = view.findViewById(R.id.film_title)
        val infoView: TextView = view.findViewById(R.id.film_info)
        val typeView: TextView = view.findViewById(R.id.film_type)
        val progressView: ProgressBar = view.findViewById(R.id.film_loading)
        val posterLayoutView: RelativeLayout = view.findViewById(R.id.film_posterLayout)
    }
}
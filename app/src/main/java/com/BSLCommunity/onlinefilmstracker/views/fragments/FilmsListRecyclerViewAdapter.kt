package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.squareup.picasso.Picasso

class FilmsListRecyclerViewAdapter(private val films: ArrayList<Film>) :
    RecyclerView.Adapter<FilmsListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.inflate_film, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val film = films[position]
        Picasso.get().load(film.posterPath).into(holder.filmPoster)
        holder.titleView.text = film.title

        var info = ""
        info += film.year
        if (film.countries.size > 0) {
            info += ", " + film.countries[0]
        }
        if (film.ratingIMDB != null) {
            info += ", " + film.ratingIMDB
        }

        holder.infoView.text = info
    }

    override fun getItemCount(): Int = films.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val filmPoster: ImageView = view.findViewById(R.id.film_poster)
        val titleView: TextView = view.findViewById(R.id.film_title)
        val infoView: TextView = view.findViewById(R.id.film_info)
    }
}
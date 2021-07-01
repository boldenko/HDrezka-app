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
        holder.yearView.text = film.date
        holder.countryView.text = film.country
    }

    override fun getItemCount(): Int = films.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val filmPoster: ImageView = view.findViewById(R.id.film_poster)
        val titleView: TextView = view.findViewById(R.id.film_title)
        val yearView: TextView = view.findViewById(R.id.film_year)
        val countryView: TextView = view.findViewById(R.id.film_country)
    }
}
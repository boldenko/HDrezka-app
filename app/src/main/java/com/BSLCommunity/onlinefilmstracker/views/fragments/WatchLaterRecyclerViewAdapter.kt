package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.objects.WatchLater

class WatchLaterRecyclerViewAdapter(private val watchLaterList: ArrayList<WatchLater>, private val openFilm: (film: Film) -> Unit) :
    RecyclerView.Adapter<WatchLaterRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.inflate_watch_later, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val watchLaterItem = watchLaterList[position]

        holder.dateView.text = watchLaterItem.date
        holder.nameView.text = watchLaterItem.name
        holder.infoView.text = watchLaterItem.additionalInfo

        holder.layout.setOnClickListener {
            openFilm(Film(watchLaterItem.filmLInk))
        }
    }

    override fun getItemCount(): Int = watchLaterList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: LinearLayout = view.findViewById(R.id.watchLater_layout)
        val dateView: TextView = view.findViewById(R.id.watchLater_date)
        val nameView: TextView = view.findViewById(R.id.watchLater_name)
        val infoView: TextView = view.findViewById(R.id.watchLater_info)
    }
}
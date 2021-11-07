package com.falcofemoralis.hdrezkaapp.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.WatchLater
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class WatchLaterRecyclerViewAdapter(private val watchLaterList: ArrayList<WatchLater>, private val openFilm: (film: Film) -> Unit, private val deleteHandler: (id: String, pos: Int) -> Unit) :
    RecyclerView.Adapter<WatchLaterRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.inflate_watch_later, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val watchLaterItem = watchLaterList[position]

        if(position == 0){
            holder.layout.requestFocus()
        }

        Picasso.get().load(watchLaterItem.posterPath).into(holder.posterView, object : Callback {
            override fun onSuccess() {
                holder.posterProgressBar.visibility = View.GONE
                holder.posterView.visibility = View.VISIBLE
            }

            override fun onError(e: Exception) {
            }
        })
        holder.dateView.text = watchLaterItem.date
        holder.nameView.text = watchLaterItem.name
        holder.infoView.text = watchLaterItem.additionalInfo

        holder.layout.setOnClickListener {
            openFilm(Film(watchLaterItem.filmLInk))
        }

        holder.deleteView.setOnClickListener {
            deleteHandler(watchLaterItem.id, position)
        }
    }

    override fun getItemCount(): Int = watchLaterList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: LinearLayout = view.findViewById(R.id.watchLater_layout)
        val posterView: ImageView = view.findViewById(R.id.inflate_watch_poster)
        val posterProgressBar: ProgressBar = view.findViewById(R.id.inflate_watch_poster_loading)
        val dateView: TextView = view.findViewById(R.id.watchLater_date)
        val nameView: TextView = view.findViewById(R.id.watchLater_name)
        val infoView: TextView = view.findViewById(R.id.watchLater_info)
        val deleteView: TextView = view.findViewById(R.id.watchLater_delete)
    }
}
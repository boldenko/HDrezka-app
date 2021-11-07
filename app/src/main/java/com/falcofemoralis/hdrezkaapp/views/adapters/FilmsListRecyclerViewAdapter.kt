package com.falcofemoralis.hdrezkaapp.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.constants.DeviceType
import com.falcofemoralis.hdrezkaapp.models.FilmModel
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.ColorFilterTransformation


class FilmsListRecyclerViewAdapter(private val context: Context, private val films: ArrayList<Film>, private val openFilm: (film: Film) -> Unit) :
    RecyclerView.Adapter<FilmsListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.inflate_film, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        zoom(holder.layout, holder.posterLayoutView, holder.titleView, holder.infoView, context)

        val film = films[position]
        val picasso = Picasso.get().load(film.posterPath)
        if (film.subInfo?.contains(FilmModel.AWAITING_TEXT) == true) {
            picasso.transform(ColorFilterTransformation(R.color.black))
        }
        picasso.into(holder.filmPoster, object : Callback {
            override fun onSuccess() {
                holder.progressView.visibility = View.GONE
                holder.posterLayoutView.visibility = View.VISIBLE
            }

            override fun onError(e: Exception) {
            }
        })

        holder.titleView.text = film.title

        var info = ""
        film.year?.let {
            info += film.year
        }
        film.countries?.let {
            if (film.countries!!.size > 0) {
                info = addComma(info)
                info += film.countries!![0]
            }
        }
        film.genres?.let {
            if (film.genres!!.size > 0) {
                info = addComma(info)
                info += film.genres!![0]
            }
        }
        if (film.ratingIMDB?.isNotEmpty() == true) {
            info = addComma(info)
            info += film.ratingIMDB
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
            res.getString(R.string.tv_show).take(4) -> ContextCompat.getColor(context, R.color.tv_show)
            else -> ContextCompat.getColor(context, R.color.background)
        }
        holder.typeView.setBackgroundColor(color)

        if (film.subInfo?.isNotEmpty() == true) {
            holder.subInfoView.visibility = View.VISIBLE
            holder.subInfoView.text = film.subInfo
            holder.subInfoView.setBackgroundColor(color)
        } else {
            holder.subInfoView.visibility = View.GONE
        }

        holder.layout.setOnClickListener {
            openFilm(film)
        }
    }

    private fun addComma(text: String): String {
        var info = text
        if (text.isNotEmpty()) {
            info += ", "
        }
        return info
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
        val subInfoView: TextView = view.findViewById(R.id.film_sub_info)
    }

    companion object{
        fun zoom(layout: LinearLayout, posterLayout: View, titleView: TextView, subView: TextView?, context: Context){
            if (SettingsData.deviceType == DeviceType.TV) {
                layout.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) {
                        posterLayout.foreground = ColorDrawable(ContextCompat.getColor(context, R.color.transparent))
                        titleView.setTextColor(context.getColor(R.color.white))
                        subView?.setTextColor(context.getColor(R.color.gray))

                        val anim: Animation = AnimationUtils.loadAnimation(context, R.anim.scale_in_tv)
                        v.startAnimation(anim)
                        anim.fillAfter = true
                    } else {
                        posterLayout.foreground = ColorDrawable(ContextCompat.getColor(context, R.color.unselected_film))
                        titleView.setTextColor(context.getColor(R.color.unselected_title))
                        subView?.setTextColor(context.getColor(R.color.unselected_subtitle))

                        val anim: Animation = AnimationUtils.loadAnimation(context, R.anim.scale_out_tv)
                        v.startAnimation(anim)
                        anim.fillAfter = true
                    }
                }
            }
        }
    }
}
package com.falcofemoralis.hdrezkaapp.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.constants.DeviceType
import com.falcofemoralis.hdrezkaapp.constants.FilmType
import com.falcofemoralis.hdrezkaapp.models.FilmModel
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import jp.wasabeef.glide.transformations.ColorFilterTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FilmsListRecyclerViewAdapter(private val context: Context, private val films: ArrayList<Film>, private val openFilm: (film: Film) -> Unit) :
    RecyclerView.Adapter<FilmsListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.inflate_film, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        zoom(context, holder.layout, holder.posterLayoutView, holder.titleView, holder.infoView)

        val film = films[position]

        var glide = Glide.with(context).load(film.posterPath).fitCenter()
        if (film.subInfo?.contains(FilmModel.AWAITING_TEXT) == true) {
            glide = glide.transform(ColorFilterTransformation(R.color.black))
        }

        glide = glide.listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: Target<Drawable>?, p3: Boolean): Boolean {
                return false
            }

            override fun onResourceReady(p0: Drawable?, p1: Any?, p2: Target<Drawable>?, p3: DataSource?, p4: Boolean): Boolean {
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        holder.progressView.visibility = View.GONE
                        holder.posterLayoutView.visibility = View.VISIBLE
                    }
                }
                return false
            }
        })
        glide = glide.error(R.drawable.nopersonphoto) // TODO
        glide.into(holder.filmPoster)
        glide.submit()

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

        holder.infoView.text = info

        var typeText = film.type
        if (film.ratingKP?.isNotEmpty() == true) {
            typeText = typeText?.let { addComma(it) }
            typeText += film.ratingKP
        }
        holder.typeView.text = typeText

        val color: Int = when (film.constFilmType) {
            FilmType.FILM -> ContextCompat.getColor(context, R.color.film)
            FilmType.MULTFILMS -> ContextCompat.getColor(context, R.color.multfilm)
            FilmType.SERIES -> ContextCompat.getColor(context, R.color.serial)
            FilmType.ANIME -> ContextCompat.getColor(context, R.color.anime)
            FilmType.TVSHOWS -> ContextCompat.getColor(context, R.color.tv_show)
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

    companion object {
        fun zoom(context: Context, layout: LinearLayout, posterLayout: View, titleView: TextView, vararg subViews: TextView?) {
            if (SettingsData.deviceType == DeviceType.TV) {
                layout.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) {
                        posterLayout.foreground = ColorDrawable(ContextCompat.getColor(context, R.color.transparent))
                        titleView.setTextColor(context.getColor(R.color.white))
                        for (subView in subViews) {
                            subView?.setTextColor(context.getColor(R.color.gray))
                        }

                        val anim: Animation = AnimationUtils.loadAnimation(context, R.anim.scale_in_tv)
                        v.startAnimation(anim)
                        anim.fillAfter = true
                    } else {
                        posterLayout.foreground = ColorDrawable(ContextCompat.getColor(context, R.color.unselected_film))
                        titleView.setTextColor(context.getColor(R.color.unselected_title))
                        for (subView in subViews) {
                            subView?.setTextColor(context.getColor(R.color.unselected_subtitle))
                        }

                        val anim: Animation = AnimationUtils.loadAnimation(context, R.anim.scale_out_tv)
                        v.startAnimation(anim)
                        anim.fillAfter = true
                    }
                }
            }
        }
    }
}
package com.BSLCommunity.onlinefilmstracker.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.objects.Comment
import com.BSLCommunity.onlinefilmstracker.utils.UnitsConverter
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class CommentsRecyclerViewAdapter(private val context: Context, private val comments: ArrayList<Comment>) : RecyclerView.Adapter<CommentsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.inflate_comment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]

        Picasso.get().load(comment.avatarPath).into(holder.avatarView, object : Callback {
            override fun onSuccess() {
                holder.avatarProgressBar.visibility = View.GONE
                holder.avatarView.visibility = View.VISIBLE
            }

            override fun onError(e: Exception) {
            }
        })
        holder.infoView.text = "${comment.nickname}, ${comment.date}"
        holder.textView.text = comment.text

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(UnitsConverter.getPX(context, comment.indent * 16), UnitsConverter.getPX(context, 6), 0, 0)
        holder.layout.layoutParams = params
        if (position % 2 == 0) {
            holder.layout.setBackgroundColor(ContextCompat.getColor(context, R.color.light_bg))
        }

        if (comment.indent > 0) {
            holder.indentView.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = comments.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: LinearLayout = view.findViewById(R.id.inflate_comment_layout)
        val avatarView: ImageView = view.findViewById(R.id.inflate_comment_avatar)
        val avatarProgressBar: ProgressBar = view.findViewById(R.id.inflate_comment_avatar_loading)
        val infoView: TextView = view.findViewById(R.id.inflate_comment_nickname_date)
        val textView: TextView = view.findViewById(R.id.inflate_comment_text)
        val indentView: View = view.findViewById(R.id.inflate_comment_indent)
    }
}
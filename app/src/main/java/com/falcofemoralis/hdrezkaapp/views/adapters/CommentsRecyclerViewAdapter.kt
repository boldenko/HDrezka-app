package com.falcofemoralis.hdrezkaapp.views.adapters

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.objects.Comment
import com.falcofemoralis.hdrezkaapp.utils.UnitsConverter
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso


class CommentsRecyclerViewAdapter(private val context: Context, private val comments: ArrayList<Comment>) : RecyclerView.Adapter<CommentsRecyclerViewAdapter.ViewHolder>() {
    enum class CommentColor {
        DARK,
        LIGHT
    }

    private val spoilerTag = "||"
    private val spoilerText = context.getString(R.string.spoiler)
    private var isNextColor = true
    private var lastColor: CommentColor = CommentColor.DARK

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

        holder.textView.text = ""
        val spoilers: ArrayList<String> = ArrayList()
        for (item in comment.text) {
            when (item.first) {
                Comment.TextType.REGULAR -> holder.textView.text = "${holder.textView.text}${item.second} "
                Comment.TextType.SPOILER -> {
                    spoilers.add(item.second)
                    holder.textView.text = "${holder.textView.text}$spoilerTag${item.second}$spoilerTag "
                }
            }
        }
        if (spoilers.size > 0) {
            holder.textView.createSpoilerText(spoilers)
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(UnitsConverter.getPX(context, comment.indent * 20), UnitsConverter.getPX(context, 6), 0, 0)
        holder.layout.layoutParams = params


        if (comment.indent > 0) {
            holder.indentLineView.visibility = View.VISIBLE
            isNextColor = false
        } else {
            isNextColor = true
        }

        if (isNextColor) {
            lastColor = when (lastColor) {
                CommentColor.LIGHT -> CommentColor.DARK
                CommentColor.DARK -> CommentColor.LIGHT
            }
        }

        holder.layout.setBackgroundColor(
            ContextCompat.getColor(
                context, when (lastColor) {
                    CommentColor.DARK -> R.color.dark_background
                    CommentColor.LIGHT -> R.color.light_bg
                }
            )
        )
    }

    private fun TextView.createSpoilerText(spoilers: ArrayList<String>) {
        var text = this.text.toString()

        val spoilersToShow: ArrayList<String> = ArrayList()
        val ranges = arrayListOf<Pair<Pair<Int, Int>, Boolean>>()
        var index = 0
        while (text.contains(spoilerTag)) {
            val start = text.indexOf(spoilerTag)
            val end = text.indexOf(spoilerTag, start + spoilerTag.length)

            text = text.replaceRange(start, end + spoilerTag.length, spoilerText)

            ranges.add(Pair(Pair(start, start + spoilerText.length), false))
            spoilersToShow.add(spoilers[index])

            index++
        }

        this.movementMethod = LinkMovementMethod.getInstance()

        updateTextView(text, this, ranges, spoilersToShow)
    }

    private fun updateTextView(plainText: String, textView: TextView, ranges: ArrayList<Pair<Pair<Int, Int>, Boolean>>, spoilers: ArrayList<String>) {
        var text = plainText
        var diff = 0
        // replace [Spoiler] with original text
        for ((index, item) in (ranges.clone() as ArrayList<Pair<Pair<Int, Int>, Boolean>>).withIndex()) {
            if (item.second) {
                val range = item.first
                text = plainText.replaceRange(range.first, range.second, spoilers[index])
                diff += spoilers[index].length - spoilerText.length

                spoilers.removeAt(index)
                ranges.removeAt(index)
            }
        }

        // Create clickable span
        val spannableString = SpannableString(text)
        for ((index, item) in ranges.withIndex()) {
            if (!item.second) {

                val range = Pair(item.first.first, item.first.second)

                spannableString.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        ranges[index] = Pair(range, true)
                        updateTextView(plainText, textView, ranges, spoilers)
                    }
                }, range.first + diff, range.second + diff, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                spannableString.setSpan(
                    ForegroundColorSpan(Color.RED),
                    range.first + diff,
                    range.second + diff,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        textView.text = spannableString
    }


    override fun getItemCount(): Int = comments.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: LinearLayout = view.findViewById(R.id.inflate_comment_layout)
        val avatarView: ImageView = view.findViewById(R.id.inflate_comment_avatar)
        val avatarProgressBar: ProgressBar = view.findViewById(R.id.inflate_comment_avatar_loading)
        val infoView: TextView = view.findViewById(R.id.inflate_comment_nickname_date)
        val textView: TextView = view.findViewById(R.id.inflate_comment_text)
        val indentLineView: View = view.findViewById(R.id.inflate_comment_indent)
    }
}
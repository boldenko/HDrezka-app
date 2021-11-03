package com.falcofemoralis.hdrezkaapp.views.tv.player.seek

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.falcofemoralis.hdrezkaapp.objects.Thumbnail

class StoryboardManager(private val context: Context) {
    private var mSeekPositions: LongArray? = null
    private var mThumbnails: ArrayList<Thumbnail>? = null

    fun setThumbnails(thumbnails: ArrayList<Thumbnail>) {
        mThumbnails = thumbnails

        initSeekPositions()
    }

    private fun initSeekPositions() {
        if (mThumbnails == null) {
            return
        }

        mSeekPositions = mThumbnails?.size?.let { LongArray(it) }

        if (mSeekPositions != null) {
            for (i in mSeekPositions!!.indices) {
                mSeekPositions!![i] = mThumbnails?.get(i)?.t2?.toLong()!! * 1000 // convert to ms
            }
        }
    }

    fun getSeekPositions(): LongArray? {
        return if (mSeekPositions == null || mSeekPositions!!.size < 10) {
            // Preventing from video being skipped fully
            null
        } else {
            mSeekPositions
        }
    }


    fun getBitmap(index: Int, callback: (bitmap: Bitmap?) -> Unit) {
        if (mThumbnails == null || mSeekPositions == null || index >= mSeekPositions!!.size) {
            return
        }
        loadPreview(mThumbnails!![index], callback)
    }

    private fun loadPreview(thumb: Thumbnail, callback: (bitmap: Bitmap?) -> Unit) {
        if (mThumbnails == null) {
            return
        }

        val transformation = GlideThumbnailTransformation(thumb.x, thumb.y, thumb.width, thumb.height)

        Glide.with(context)
            .asBitmap()
            .load(thumb.url)
            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
            .transform(transformation)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                    // NOP
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                    callback(resource)
                }
            })

        /*  if (mCurrentImgNum != imgNum) {
              mSeekDirection = if (mCurrentImgNum < imgNum) StoryboardManager.DIRECTION_RIGHT else StoryboardManager.DIRECTION_LEFT
              mCachedImageNums.add(imgNum)
              mCurrentImgNum = imgNum
              preloadNextImage()
          }*/
    }

/*    private fun preloadNextImage() {
        if (mStoryboard == null) {
            return
        }
        for (i in 1..MAX_PRELOADED_IMAGES) {
            val imgNum: Int = if (mSeekDirection == StoryboardManager.DIRECTION_RIGHT) mCurrentImgNum + i else mCurrentImgNum - i // get next image
            preloadImage(imgNum)
        }
    }*/

/*    private fun preloadImage(imgNum: Int) {
        if (mCachedImageNums.contains(imgNum) || imgNum < 0) {
            return
        }
        mCachedImageNums.add(imgNum)
        val link = mStoryboard!!.getGroupUrl(imgNum)
        Glide.with(mContext)
            .load(link)
            .preload()
    }*/

    companion object {
        private const val MAX_PRELOADED_IMAGES = 3
    }
}

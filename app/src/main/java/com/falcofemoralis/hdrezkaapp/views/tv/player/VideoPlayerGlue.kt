package com.falcofemoralis.hdrezkaapp.views.tv.player

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.PlaybackControlsRow.*
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.constants.ActionConstants
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import java.util.concurrent.TimeUnit

class VideoPlayerGlue(
    context: Context?,
    playerAdapter: LeanbackPlayerAdapter?,
    private val mActionListener: PlaybackActionListener,
    private val isSerial: Boolean
) : PlaybackTransportControlGlue<LeanbackPlayerAdapter?>(context, playerAdapter) {
    private var mRepeatAction: RepeatAction? = null
    private var mThumbsUpAction: ThumbsUpAction? = null
    private var mThumbsDownAction: ThumbsDownAction? = null
    private var mSkipPreviousAction: SkipPreviousAction? = null
    private var mSkipNextAction: SkipNextAction? = null
    private var mFastForwardAction: FastForwardAction? = null
    private var mRewindAction: RewindAction? = null
    private var mActionsVisible = false
    private var mOffsetMillis: Long = 0
    private var mSavedDuration: Long = -1
    private var mSpeedAction: MyAction? = null
    private var mClosedCaptioningAction: ClosedCaptioningAction? = null
    private var mQualityAction: MyAction? = null

    init {
        mSkipPreviousAction = SkipPreviousAction(context)
        mSkipNextAction = SkipNextAction(context)
        mFastForwardAction = FastForwardAction(context)
        mRewindAction = RewindAction(context)

        mThumbsUpAction = ThumbsUpAction(context)
        mThumbsUpAction?.index = ThumbsUpAction.INDEX_OUTLINE
        mThumbsDownAction = ThumbsDownAction(context)
        mThumbsDownAction?.index = ThumbsDownAction.INDEX_OUTLINE
        mRepeatAction = RepeatAction(context)

        mSpeedAction = MyAction(context!!, ActionConstants.ACTION_SPEEDUP, R.drawable.ic_speed_increase, R.string.button_speedup)
        mQualityAction = MyAction(context!!, ActionConstants.ACTION_SELECT_QUALITY, R.drawable.ic_high_quality, R.string.select_quality)
        mClosedCaptioningAction = ClosedCaptioningAction(context)
        val res = context.resources
        val labels = arrayOfNulls<String>(1)
        labels[0] = res.getString(R.string.button_cc)
        mClosedCaptioningAction?.setLabels(labels)
    }

    override fun onCreatePrimaryActions(adapter: ArrayObjectAdapter) {
        // Order matters, super.onCreatePrimaryActions() will create the play / pause action.
        // Will display as follows:
        // play/pause, previous, rewind, fast forward, next
        //   > /||      |<        <<        >>         >|
        super.onCreatePrimaryActions(adapter)
        adapter.add(mRewindAction)
        adapter.add(mFastForwardAction)

        if (isSerial) {
            adapter.add(mSkipPreviousAction)
            adapter.add(mSkipNextAction)
        }
    }

    override fun onCreateSecondaryActions(adapter: ArrayObjectAdapter) {
        super.onCreateSecondaryActions(adapter)
        adapter.add(mClosedCaptioningAction)
        adapter.add(mQualityAction)
        adapter.add(mSpeedAction)
        // adapter.add(mThumbsDownAction)
        // adapter.add(mThumbsUpAction)
        // adapter.add(mRepeatAction)
    }

    // отправлять
    override fun onActionClicked(action: Action?) {
        if (shouldDispatchAction(action)) {
            if (action != null) {
                dispatchAction(action)
            }
            return
        }
        // Super class handles play/pause and delegates to abstract methods next()/previous().
        super.onActionClicked(action)
    }

    // Should dispatch actions that the super class does not supply callbacks for.
    // mRewindAction | mSkipNextAction will return false
    private fun shouldDispatchAction(action: Action?): Boolean {
        return action === mRewindAction ||
                action === mFastForwardAction ||
                action === mThumbsDownAction ||
                action === mThumbsUpAction ||
                action === mRepeatAction ||
                action === mSpeedAction ||
                action === mClosedCaptioningAction ||
                action === mQualityAction
    }

    private fun dispatchAction(action: Action) {
        // Primary actions are handled manually.
        if (action === mRewindAction) {
            rewind()
        } else if (action === mFastForwardAction) {
            fastForward()
        } else if (action === mSpeedAction) {
            mActionListener.onSpeed()
        } else if (action === mClosedCaptioningAction) {
            mActionListener.onCaption()
        } else if (action === mQualityAction) {
            mActionListener.onQualitySelected()
        } else if (action is MultiAction) {
            val multiAction = action
            multiAction.nextIndex()

            // Notify adapter of action changes to handle secondary actions, such as, thumbs up/down
            // and repeat.
            notifyActionChanged(
                multiAction,
                controlsRow.secondaryActionsAdapter as ArrayObjectAdapter
            )
        }
    }

    private fun notifyActionChanged(action: MultiAction, adapter: ArrayObjectAdapter?) {
        if (adapter != null) {
            val index = adapter.indexOf(action)
            if (index >= 0) {
                adapter.notifyArrayItemRangeChanged(index, 1)
            }
        }
    }

    /** Skips backwards 10 seconds.  */
    fun rewind() {
        var newPosition: Long = currentPosition - TEN_SECONDS
        newPosition = if (newPosition < 0) 0 else newPosition
        playerAdapter?.seekTo(newPosition)
    }

    /** Skips forward 10 seconds.  */
    fun fastForward() {
        if (duration > -1) {
            var newPosition: Long = currentPosition + TEN_SECONDS
            newPosition = if (newPosition > duration) duration else newPosition
            playerAdapter?.seekTo(newPosition)
        }
    }

    override fun previous() {
        mActionListener.onPrevious()
    }

    override fun next() {
        mActionListener.onNext()
    }

    fun setActions(showActions: Boolean) {
        if (showActions) {
            if (mActionsVisible) return
            val row = controlsRow
            var adapter = row.primaryActionsAdapter as ArrayObjectAdapter
            adapter.clear()
            onCreatePrimaryActions(adapter)
            adapter.notifyArrayItemRangeChanged(0, adapter.size())
            adapter = row.secondaryActionsAdapter as ArrayObjectAdapter
            adapter.clear()
            onCreateSecondaryActions(adapter)
            adapter.notifyArrayItemRangeChanged(0, adapter.size())
            mActionsVisible = true
            onPlayStateChanged()
        } else {
            if (!mActionsVisible) return
            val row = controlsRow
            var adapter = row.primaryActionsAdapter as ArrayObjectAdapter
            adapter.clear()
            adapter.notifyArrayItemRangeChanged(0, 0)
            adapter = row.secondaryActionsAdapter as ArrayObjectAdapter
            adapter.clear()
            adapter.notifyArrayItemRangeChanged(0, 0)
            mActionsVisible = false
        }
    }

    fun myGetDuration(): Long {
        var duration = duration
        if (duration >= 0) duration += mOffsetMillis
        if (duration > 0) mSavedDuration = duration
        return duration
    }

    fun setOffsetMillis(offsetMillis: Long) {
        mOffsetMillis = offsetMillis
    }

    override fun onPlayCompleted() {
        if(SettingsData.autoPlayNextEpisode == true && isSerial){
            mActionListener.onNext()
        }
        super.onPlayCompleted()
    }

    interface OnActionClickedListener {
        /** Skip to the previous item in the queue.  */
        fun onPrevious()

        /** Skip to the next item in the queue.  */
        fun onNext()

        //  fun onPlayCompleted(playlistPlayAction: org.mythtv.leanfront.player.VideoPlayerGlue.MyAction?)
        fun onZoom()
        fun onAspect()
        fun onCaption()
        fun onPivot()
        fun onRewind()
        fun onFastForward()
        fun onJumpForward()
        fun onJumpBack()
        fun onSpeed()
        fun onAudioTrack()
        fun onAudioSync()
        fun onActionSelected(action: Action?)
        fun onQualitySelected()
    }

    /**
     * Our custom actions
     */
    class MyAction(context: Context, id: Int, icons: IntArray, labels: IntArray) : MultiAction(id) {
        constructor(context: Context, id: Int, icon: Int, label: Int) : this(context, id, intArrayOf(icon), intArrayOf(label)) {}

        init {
            val res = context.resources
            val drawables = arrayOfNulls<Drawable>(icons.size)
            val labelStr = arrayOfNulls<String>(icons.size)
            for (i in icons.indices) {
                drawables[i] = ResourcesCompat.getDrawable(res, icons[i], null)
                labelStr[i] = res.getString(labels[i])
            }
            setDrawables(drawables)
            setLabels(labelStr)
        }
    }

    companion object {
        val TEN_SECONDS = TimeUnit.SECONDS.toMillis(10)
    }
}
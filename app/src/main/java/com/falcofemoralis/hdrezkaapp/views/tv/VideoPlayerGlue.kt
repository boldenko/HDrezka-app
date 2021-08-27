package com.falcofemoralis.hdrezkaapp.views.tv

import android.content.Context
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.PlaybackControlsRow.*
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import java.util.concurrent.TimeUnit

class VideoPlayerGlue(context: Context, playerAdapter: MediaPlayerAdapter, private val mActionListener: OnActionClickedListener, private val isSerial: Boolean) :
    PlaybackTransportControlGlue<MediaPlayerAdapter>(context, playerAdapter) {

    // https://github.com/googlearchive/androidtv-Leanback/blob/master/app/src/main/java/com/example/android/tvleanback/player/VideoPlayerGlue.java

    /** Listens for when skip to next and previous actions have been dispatched.  */
    interface OnActionClickedListener {
        /** Skip to the previous item in the queue.  */
        fun onPrevious()

        /** Skip to the next item in the queue.  */
        fun onNext()
    }

   // private val mRepeatAction: RepeatAction
  //  private val mThumbsUpAction: ThumbsUpAction
   // private val mThumbsDownAction: ThumbsDownAction
    private val mSkipPreviousAction: SkipPreviousAction
    private val mSkipNextAction: SkipNextAction
    private val mFastForwardAction: FastForwardAction
    private val mRewindAction: RewindAction

    init {
        mSkipPreviousAction = SkipPreviousAction(context)
        mSkipNextAction = SkipNextAction(context)
        mFastForwardAction = FastForwardAction(context)
        mRewindAction = RewindAction(context)
   /*     mThumbsUpAction = ThumbsUpAction(context)
        mThumbsUpAction.index = ThumbsUpAction.INDEX_OUTLINE
        mThumbsDownAction = ThumbsDownAction(context)
        mThumbsDownAction.index = ThumbsDownAction.INDEX_OUTLINE
        mRepeatAction = RepeatAction(context)*/
    }

    override fun onPlayCompleted() {
        if(SettingsData.autoPlayNextEpisode == true){
            mActionListener.onNext()
        }
        super.onPlayCompleted()
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
        // adapter.add(mThumbsDownAction)
        //adapter.add(mThumbsUpAction)
       // adapter.add(mRepeatAction)
    }

    override fun onActionClicked(action: Action) {
        if (shouldDispatchAction(action)) {
            dispatchAction(action)
            return
        }
        // Super class handles play/pause and delegates to abstract methods next()/previous().
        super.onActionClicked(action)
    }

    // Should dispatch actions that the super class does not supply callbacks for.
    private fun shouldDispatchAction(action: Action): Boolean {
        return action === mRewindAction || action === mFastForwardAction
    }

    private fun dispatchAction(action: Action) {
        // Primary actions are handled manually.
        if (action === mRewindAction) {
            rewind()
        } else if (action === mFastForwardAction) {
            fastForward()
        } else if (action is MultiAction) {
            val multiAction = action as MultiAction
            multiAction.nextIndex()
            // Notify adapter of action changes to handle secondary actions, such as, thumbs up/down
            // and repeat.
            notifyActionChanged(multiAction, controlsRow.secondaryActionsAdapter as ArrayObjectAdapter)
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

    override fun next() {
        mActionListener.onNext()
    }

    override fun previous() {
        mActionListener.onPrevious()
    }

    /** Skips backwards 10 seconds.  */
    private fun rewind() {
        var newPosition = currentPosition - TEN_SECONDS
        newPosition = if (newPosition < 0) 0 else newPosition
        playerAdapter.seekTo(newPosition)
    }

    /** Skips forward 10 seconds.  */
    private fun fastForward() {
        if (duration > -1) {
            var newPosition = currentPosition + TEN_SECONDS
            newPosition = if (newPosition > duration) duration else newPosition
            playerAdapter.seekTo(newPosition)
        }
    }

    companion object {
        private val TEN_SECONDS: Long = TimeUnit.SECONDS.toMillis(10)
    }
}
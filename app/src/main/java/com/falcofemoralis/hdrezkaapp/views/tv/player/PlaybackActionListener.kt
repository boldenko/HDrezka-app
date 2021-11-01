package com.falcofemoralis.hdrezkaapp.views.tv.player

import androidx.leanback.widget.Action
import com.falcofemoralis.hdrezkaapp.objects.Playlist

class PlaybackActionListener(private val playerFragment: PlayerFragment, private val mPlaylist: Playlist) : VideoPlayerGlue.OnActionClickedListener {
    override fun onPrevious() {
        playerFragment.skipToPrevious()
    }

    override fun onNext() {
        playerFragment.skipToNext()
    }

    override fun onZoom() {
        TODO("Not yet implemented")
    }

    override fun onAspect() {
        TODO("Not yet implemented")
    }

    override fun onCaption() {
        TODO("Not yet implemented")
    }

    override fun onPivot() {
        TODO("Not yet implemented")
    }

    override fun onRewind() {
        TODO("Not yet implemented")
    }

    override fun onFastForward() {
        TODO("Not yet implemented")
    }

    override fun onJumpForward() {
        TODO("Not yet implemented")
    }

    override fun onJumpBack() {
        TODO("Not yet implemented")
    }

    override fun onSpeed() {
        TODO("Not yet implemented")
    }

    override fun onAudioTrack() {
        TODO("Not yet implemented")
    }

    override fun onAudioSync() {
        TODO("Not yet implemented")
    }

    override fun onActionSelected(action: Action?) {
        TODO("Not yet implemented")
    }

    fun onMenu(): Boolean? {
        return true
    }
}
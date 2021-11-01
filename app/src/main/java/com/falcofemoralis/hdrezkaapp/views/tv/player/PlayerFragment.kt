package com.falcofemoralis.hdrezkaapp.views.tv.player

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.leanback.app.PlaybackSupportFragment
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.models.FilmModel
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.Playlist
import com.falcofemoralis.hdrezkaapp.objects.Stream
import com.falcofemoralis.hdrezkaapp.objects.Voice
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerFragment : VideoSupportFragment() {
    /* Player elements */
    private var mPlayerGlue: VideoPlayerGlue? = null
    private var mPlayerAdapter: LeanbackPlayerAdapter? = null
    var mPlayer: SimpleExoPlayer? = null
    private var mTrackSelector: TrackSelector? = null
    var mPlaybackActionListener: PlaybackActionListener? = null
    private var mFocusView: View? = null
    private var mCurrentAction: Action? = null
    private val mRecordid: Long = -1
    private val mJump: Int = 60000 * 5 // 5 min
    var mIsBounded = true
    var mBookmark: Long = 0 // milliseconds
    private var mOffsetBytes: Long = 0
    var mSpeed: Float = SPEED_START_VALUE

    /* Data elements */
    private var mFilm: Film? = null
    private var mStream: Stream? = null
    private var mTranslation: Voice? = null
    private var mPlaylist: Playlist = Playlist()

    /* Variable elements */
    private var title: String? = null
    private var isSerial: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeData()
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || mPlayer == null) {
            initializePlayer()
        }

        hideNavigation()
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun onPause() {
        super.onPause()

        if (mPlayerGlue != null && mPlayerGlue?.isPlaying == true) {
            mPlayerGlue?.pause()
        }

        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()

        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun initializeData() {
        mFilm = activity?.intent?.getSerializableExtra(PlayerActivity.FILM) as Film
        mStream = activity?.intent?.getSerializableExtra(PlayerActivity.STREAM) as Stream
        mTranslation = activity?.intent?.getSerializableExtra(PlayerActivity.TRANSLATION) as Voice

        if (mTranslation?.seasons != null && mTranslation?.seasons!!.size > 0) {
            isSerial = true
            val seasons: HashMap<String, ArrayList<String>> = mTranslation?.seasons!!
            for ((season, episodes) in seasons) {
                for (episode in episodes) {
                    val item = Playlist.PlaylistItem(season, episode)
                    mPlaylist.add(item)

                    if (season == mTranslation?.selectedEpisode?.first && episode == mTranslation?.selectedEpisode?.second) {
                        mPlaylist.setCurrentPosition(mPlaylist.size() - 1)
                    }
                }
            }
            title = "${mFilm?.title} Сезон ${mTranslation?.selectedEpisode?.first} - Эпизод ${mTranslation?.selectedEpisode?.second}"
        } else {
            isSerial = false
            mFilm?.title?.let { title = it }
        }
    }

    private fun initializePlayer() {
        val renderFactory = DefaultRenderersFactory(requireContext())
        renderFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
        renderFactory.setEnableDecoderFallback(true)

        mTrackSelector = DefaultTrackSelector(requireContext())
        mPlayer = SimpleExoPlayer.Builder(requireContext(), renderFactory).setTrackSelector(mTrackSelector as DefaultTrackSelector).build()
        mPlayerAdapter = LeanbackPlayerAdapter(requireActivity(), mPlayer!!, UPDATE_DELAY)

        if (mPlaybackActionListener == null) {
            mPlaybackActionListener = PlaybackActionListener(this, mPlaylist)
        }
        mPlayerGlue = VideoPlayerGlue(activity, mPlayerAdapter, mPlaybackActionListener!!, isSerial)
        mPlayerGlue?.host = VideoSupportFragmentGlueHost(this)
        mPlayerGlue?.playWhenPrepared()

        mStream?.let { play(it) }
    }

    private fun releasePlayer() {
        if (mPlayer != null) {
            mPlayer!!.release()
            mPlayer = null
            mTrackSelector = null
            mPlayerGlue = null
            mPlayerAdapter = null
            mPlaybackActionListener = null
        }
    }

    private fun play(stream: Stream) {
        mPlayerGlue?.title = title
        mPlayerGlue?.subtitle = mFilm?.description
        prepareMediaForPlaying(Uri.parse(stream.url))
        mPlayerGlue?.play()
    }

    private fun play(playlistItem: Playlist.PlaylistItem) {
        mPlayerGlue?.title = "${mFilm?.title} Сезон ${playlistItem.season} - Эпизод ${playlistItem.episode}"
        mPlayerGlue?.subtitle = mFilm?.description

        GlobalScope.launch {
            mTranslation?.streams = FilmModel.getStreamsByEpisodeId(mTranslation!!, mFilm?.filmId!!, playlistItem.season, playlistItem.episode)
            val streams: ArrayList<Stream> = FilmModel.parseStreams(mTranslation!!, mFilm?.filmId!!)

            withContext(Dispatchers.Main) {
                for (stream in streams) {
                    if (stream.quality == mStream?.quality) {
                        mStream = stream
                        prepareMediaForPlaying(Uri.parse(stream.url))
                        mPlayerGlue?.play()
                        break
                    }
                }
            }
        }
    }

    private fun prepareMediaForPlaying(mediaSourceUri: Uri) {
        val userAgent = Util.getUserAgent(requireActivity(), "VideoPlayerGlue")
        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(DefaultDataSourceFactory(requireActivity(), userAgent))
            .setExtractorsFactory(DefaultExtractorsFactory())
            .createMediaSource(mediaSourceUri)

        mPlayer?.prepare(mediaSource)
    }

    fun skipToNext() {
        mPlaylist.next()?.let { play(it) }
    }

    fun skipToPrevious() {
        mPlaylist.previous()?.let { play(it) }
    }

    fun rewind() {
        mPlayerGlue?.rewind()
    }

    fun fastForward() {
        mPlayerGlue?.fastForward()
    }

    fun tickle(autohide: Boolean, showActions: Boolean) {
        mPlayerGlue?.setActions(showActions)
        isControlsOverlayAutoHideEnabled = false
        showControlsOverlay(true)
        if (autohide) {
            isControlsOverlayAutoHideEnabled = true
        }
    }

    // Overridden because the default tickle disables the fade timer.
    override fun tickle() {
        tickle(false, true)
    }

    fun actionSelected(action: Action?) {
        val view = view
        val text = view?.findViewById<TextView>(R.id.button_selected) ?: return

        if (action != null) {
            mCurrentAction = action
            text.text = action.label1
            mFocusView = view.findFocus()
        } else {
            // Called with null when a key is pressed. Will clear help message
            // if we are no longer on the control.
            val newView = view.findFocus()
            if (newView !== mFocusView) {
                text.text = ""
                mFocusView = newView
                mCurrentAction = null
            } else if (mCurrentAction != null) {
                text.text = mCurrentAction?.label1
            }
        }
    }

    fun onControlsUp(): Boolean {
        val primaryActionsAdapter = mPlayerGlue!!.controlsRow.primaryActionsAdapter as ArrayObjectAdapter
        if (primaryActionsAdapter.indexOf(mCurrentAction) >= 0) {
            hideControlsOverlay(true)
            return true
        }
        return false
    }

    fun setActions(showActions: Boolean) {
        mPlayerGlue?.setActions(showActions)
    }

    /** Jumps backwards 5 min.  */
    fun jumpBack() {
        moveBackward(mJump)
    }

    /** Jumps forward 5 min.  */
    fun jumpForward() {
        moveForward(mJump)
    }

    private fun moveBackward(millis: Int) {
        var newPosition = mPlayerGlue!!.currentPosition - millis
        newPosition = if (newPosition < 0) 0 else newPosition
        seekTo(newPosition, false)
    }

    private fun moveForward(millis: Int) {
        var doReset = false
        var resetDone = false

        if (!mIsBounded) {
            seekTo(-1, true)
            resetDone = true
        }

        val duration: Long? = mPlayerGlue?.myGetDuration()
        if (duration != null) {
            if (duration > -1) {
                var newPosition = mPlayerGlue!!.currentPosition + millis
                if (newPosition > duration - 1000) {
                    newPosition = duration - 1000
                    doReset = true
                }
                seekTo(newPosition, doReset && !resetDone)
            }
        }
    }

    // set position to -1 for a reset with no seek.
    // set doReset true to refresh file size information.
    // If it is in unbounded state will reset to bounded state
    // regardless of parameters.
    private fun seekTo(position: Long, doReset: Boolean) {
        val newPosition: Long = if (position == -1L) mPlayerGlue!!.currentPosition else position
        if (mIsBounded && !doReset) {
            if (position != -1L) {
                mPlayerAdapter?.seekTo(newPosition)
            }
        } else {
            mIsBounded = true
            mBookmark = newPosition
            mOffsetBytes = 0
            mPlayerGlue?.setOffsetMillis(0)
            mPlayer?.stop(true)

            mStream?.let { play(it) }
        }
    }

    fun hideNavigation() {
        if (requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)) {
            val view = view
            view!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    companion object {
        const val UPDATE_DELAY = 16
        const val SPEED_START_VALUE = 1.0f
    }
}
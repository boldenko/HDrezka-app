package com.falcofemoralis.hdrezkaapp.views.tv.player

import android.annotation.TargetApi
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
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
    private var mPlayer: SimpleExoPlayer? = null
    private var mTrackSelector: TrackSelector? = null
    private var mPlaylistActionListener: PlaylistActionListener? = null

    /* Data elements */
    private var mFilm: Film? = null
    private var mStream: Stream? = null
    private var mTranslation: Voice? = null
    private var mPlaylist: Playlist = Playlist()
    private var mPlaylistItem: Playlist.PlaylistItem? = null

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
                        mPlaylistItem = item
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
        mPlaylistActionListener = PlaylistActionListener(mPlaylist)
        mPlayerGlue = VideoPlayerGlue(activity, mPlayerAdapter, mPlaylistActionListener!!, isSerial)
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
            mPlaylistActionListener = null
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
        mPlayerGlue?.next()
    }

    fun skipToPrevious() {
        mPlayerGlue?.previous()
    }

    fun rewind() {
        mPlayerGlue?.rewind()
    }

    fun fastForward() {
        mPlayerGlue?.fastForward()
    }

    internal inner class PlaylistActionListener(private val mPlaylist: Playlist) : VideoPlayerGlue.OnActionClickedListener {
        override fun onPrevious() {
            mPlaylist.previous()?.let { play(it) }
        }

        override fun onNext() {
            mPlaylist.next()?.let { play(it) }
        }
    }

    companion object {
        const val UPDATE_DELAY = 16
    }
}
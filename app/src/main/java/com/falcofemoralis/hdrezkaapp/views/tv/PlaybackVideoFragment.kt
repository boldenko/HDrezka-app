package com.falcofemoralis.hdrezkaapp.views.tv

import android.net.Uri
import android.os.Bundle
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.*
import com.falcofemoralis.hdrezkaapp.models.FilmModel
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.Playlist
import com.falcofemoralis.hdrezkaapp.objects.Stream
import com.falcofemoralis.hdrezkaapp.objects.Voice
import kotlinx.coroutines.*

open class PlaybackVideoFragment : VideoSupportFragment() {
    private lateinit var mTransportControlGlue: PlaybackTransportControlGlue<MediaPlayerAdapter>
    private lateinit var playerAdapter: MediaPlayerAdapter
    private lateinit var mPlaylistActionListener: PlaylistActionListener
    private lateinit var mPlaylist: Playlist
    private lateinit var film: Film
    private lateinit var translation: Voice
    private lateinit var mStream: Stream

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        film = activity?.intent?.getSerializableExtra(PlaybackActivity.FILM) as Film
        mStream = activity?.intent?.getSerializableExtra(PlaybackActivity.STREAM) as Stream
        translation = activity?.intent?.getSerializableExtra(PlaybackActivity.TRANSLATION) as Voice

        mPlaylist = Playlist()
        val isSerial: Boolean
        var title = ""
        if (translation.seasons != null && translation.seasons!!.size > 0) {
            isSerial = true
            val seasons: HashMap<String, ArrayList<String>> = translation.seasons!!
            for ((season, episodes) in seasons) {
                for (episode in episodes) {
                    mPlaylist.add(Playlist.PlaylistItem(season, episode))
                }
            }
            title = "${film.title} Сезон ${translation.selectedEpisode?.first} - Эпизод ${translation.selectedEpisode?.second}"
        } else {
            isSerial = false
            film.title?.let { title = it }
        }

        playerAdapter = MediaPlayerAdapter(context)
        playerAdapter.setRepeatAction(PlaybackControlsRow.RepeatAction.INDEX_NONE)
        mPlaylistActionListener = PlaylistActionListener(mPlaylist)
        mTransportControlGlue = VideoPlayerGlue(requireActivity(), playerAdapter, mPlaylistActionListener, isSerial)
        mTransportControlGlue.host = VideoSupportFragmentGlueHost(this@PlaybackVideoFragment)
        mTransportControlGlue.title = title
        mTransportControlGlue.subtitle = film.description
        playerAdapter.setDataSource(Uri.parse(mStream.url))
        mTransportControlGlue.playWhenPrepared()
    }

    override fun onPause() {
        super.onPause()
        mTransportControlGlue.pause()
    }

    fun play(playlistItem: Playlist.PlaylistItem) {
        mTransportControlGlue.title = "${film.title} Сезон ${playlistItem.season} - Эпизод ${playlistItem.episode}"
        mTransportControlGlue.subtitle = film.description
        GlobalScope.launch {
            translation.streams = FilmModel.getStreamsByEpisodeId(translation, film.filmId!!, playlistItem.season, playlistItem.episode)
            val streams: ArrayList<Stream> = FilmModel.parseStreams(translation, film.filmId!!)

            withContext(Dispatchers.Main) {
                for (stream in streams) {
                    if (stream.quality == mStream.quality) {
                        playerAdapter.setDataSource(Uri.parse(stream.url))

                        GlobalScope.launch {
                            delay(1000)
                            withContext(Dispatchers.Main){
                                mTransportControlGlue.play()
                            }
                        }
                        break
                    }
                }
            }
        }
    }


    internal inner class PlaylistActionListener(playlist: Playlist) : VideoPlayerGlue.OnActionClickedListener {
        private val mPlaylist: Playlist = playlist

        override fun onPrevious() {
            mPlaylist.previous()?.let { play(it) }
        }

        override fun onNext() {
            mPlaylist.next()?.let { play(it) }
        }

    }
}
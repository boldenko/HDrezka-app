package com.falcofemoralis.hdrezkaapp.views.tv

import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.PlaybackControlsRow
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.Stream

class PlaybackVideoFragment : VideoSupportFragment() {
    private lateinit var mTransportControlGlue: PlaybackTransportControlGlue<MediaPlayerAdapter>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val film = activity?.intent?.getSerializableExtra(PlaybackActivity.FILM) as Film
        val stream = activity?.intent?.getSerializableExtra(PlaybackActivity.STREAM) as Stream

        val glueHost = VideoSupportFragmentGlueHost(this@PlaybackVideoFragment)
        val playerAdapter = MediaPlayerAdapter(context)
        playerAdapter.setRepeatAction(PlaybackControlsRow.RepeatAction.INDEX_NONE)

        mTransportControlGlue = PlaybackTransportControlGlue(requireActivity(), playerAdapter)
        mTransportControlGlue.host = glueHost
        mTransportControlGlue.title = film.title
        mTransportControlGlue.subtitle = film.description
        mTransportControlGlue.playWhenPrepared()
        playerAdapter.setDataSource(Uri.parse(stream.url))
    }

    override fun onPause() {
        super.onPause()
        mTransportControlGlue.pause()
    }
}
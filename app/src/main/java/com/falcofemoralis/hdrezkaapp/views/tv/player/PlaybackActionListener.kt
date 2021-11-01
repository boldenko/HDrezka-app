package com.falcofemoralis.hdrezkaapp.views.tv.player

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.leanback.widget.Action
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.objects.Playlist
import com.google.android.exoplayer2.PlaybackParameters

class PlaybackActionListener(private val playerFragment: PlayerFragment, private val mPlaylist: Playlist) : VideoPlayerGlue.OnActionClickedListener {
    var mDialog: AlertDialog? = null

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
        showSpeedSelector()
    }

    private fun showSpeedSelector(): Boolean {
        playerFragment.hideControlsOverlay(true)
        val builder = playerFragment.getContext()?.let { AlertDialog.Builder(it, R.style.Theme_AppCompat_Dialog_Alert) }
        builder?.setTitle(R.string.title_select_speed)?.setView(R.layout.leanback_preference_widget_seekbar)
        mDialog = builder?.create()
        mDialog?.show()
        val lp: WindowManager.LayoutParams? = mDialog?.window?.getAttributes()
        lp?.dimAmount = 0.0f // Dim level. 0.0 - no dim, 1.0 - completely opaque
        mDialog?.window?.attributes = lp
        mDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.argb(100, 0, 0, 0)))
        val seekBar: SeekBar? = mDialog?.findViewById<SeekBar>(R.id.seekbar)
        seekBar?.max = 800
        seekBar?.progress = Math.round(playerFragment.mSpeed * 100.0f).toInt()
        val seekValue: TextView? = mDialog?.findViewById<TextView>(R.id.seekbar_value)
        val value = (playerFragment.mSpeed * 100.0f)
        seekValue?.setText("$value%")

        mDialog?.setOnKeyListener { dlg: DialogInterface, keyCode: Int, event: KeyEvent ->
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                    dlg.dismiss()
                    return@setOnKeyListener true
                }
            }
            if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
            var value = seekBar?.progress
            if (value != null) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_DOWN -> value -= if (value > 10) 10 else return@setOnKeyListener true
                    KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_UP -> value += if (value <= 790) 10 else return@setOnKeyListener true
                    KeyEvent.KEYCODE_BACK -> return@setOnKeyListener false
                    else -> {
                        dlg.dismiss()
                        playerFragment.activity?.onKeyDown(event.keyCode, event)
                        return@setOnKeyListener true
                    }
                }
            }
            if (value != null) {
                seekBar?.progress = value
            }
            true
        }
        seekBar?.setOnSeekBarChangeListener(
            object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, value: Int, fromUser: Boolean) {
                    var value = value
                    value = value / 10 * 10
                    if (value < 10) value = 10
                    seekValue?.text = "$value%"
                    playerFragment.mSpeed = value.toFloat() * 0.01f
                    val parms = PlaybackParameters(playerFragment.mSpeed)
                    playerFragment.mPlayer?.playbackParameters = parms
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            }
        )
        mDialog?.setOnDismissListener(
            DialogInterface.OnDismissListener { dialog: DialogInterface? ->
                mDialog = null
                playerFragment.hideNavigation()
            }
        )

        return true
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
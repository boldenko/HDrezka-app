package com.falcofemoralis.hdrezkaapp.views.tv.player

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.PlaybackFragment
import com.falcofemoralis.hdrezkaapp.R

class PlayerActivity : FragmentActivity() {
    private var mPlaybackFragment: PlayerFragment? = null
    private var gamepadTriggerPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val fragment = supportFragmentManager.findFragmentByTag(getString(R.string.playback_tag))
        if (fragment is PlayerFragment) {
            mPlaybackFragment = fragment
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BUTTON_R1 -> {
                mPlaybackFragment?.skipToNext()
                return true
            }
            KeyEvent.KEYCODE_BUTTON_L1 -> {
                mPlaybackFragment?.skipToPrevious()
                return true
            }
            KeyEvent.KEYCODE_BUTTON_L2 -> {
                mPlaybackFragment?.rewind()
            }
            KeyEvent.KEYCODE_BUTTON_R2 -> {
                mPlaybackFragment?.fastForward()
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        // This method will handle gamepad events.
        if (event.getAxisValue(MotionEvent.AXIS_LTRIGGER) > GAMEPAD_TRIGGER_INTENSITY_ON
            && !gamepadTriggerPressed
        ) {
            mPlaybackFragment?.rewind()
            gamepadTriggerPressed = true
        } else if (event.getAxisValue(MotionEvent.AXIS_RTRIGGER) > GAMEPAD_TRIGGER_INTENSITY_ON
            && !gamepadTriggerPressed
        ) {
            mPlaybackFragment?.fastForward()
            gamepadTriggerPressed = true
        } else if (event.getAxisValue(MotionEvent.AXIS_LTRIGGER) < GAMEPAD_TRIGGER_INTENSITY_OFF
            && event.getAxisValue(MotionEvent.AXIS_RTRIGGER) < GAMEPAD_TRIGGER_INTENSITY_OFF
        ) {
            gamepadTriggerPressed = false
        }
        return super.onGenericMotionEvent(event)
    }

    companion object {
        const val FILM = "film"
        const val STREAM = "stream"
        const val TRANSLATION = "translation"
        const val GAMEPAD_TRIGGER_INTENSITY_ON = 0.5f
        const val GAMEPAD_TRIGGER_INTENSITY_OFF = 0.45f
    }

}
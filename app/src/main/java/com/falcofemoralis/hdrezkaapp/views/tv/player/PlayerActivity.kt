package com.falcofemoralis.hdrezkaapp.views.tv.player

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.SeekBar
import com.falcofemoralis.hdrezkaapp.R

class PlayerActivity : FragmentActivity() {
    private var mPlaybackFragment: PlayerFragment? = null
    private var gamepadTriggerPressed = false

    private var isLongKeyPress = false
    private var mArrowSkipJump = false

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

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val keycode = event.keyCode

        if (event.action == KeyEvent.ACTION_DOWN) {
            val view = currentFocus
            var isSeekBar = false

            if (view is SeekBar) {
                isSeekBar = true
            }

            if ((keycode == KeyEvent.KEYCODE_DPAD_CENTER || keycode == KeyEvent.KEYCODE_ENTER) && !mPlaybackFragment!!.isControlsOverlayVisible) {
                return if (event.isLongPress) {
                    isLongKeyPress = true
                    val retu = mPlaybackFragment?.mPlaybackActionListener?.onMenu()
                    return if (retu != null) {
                        retu
                    } else {
                        false
                    }
                } else {
                    true
                }
            }

            if (keycode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                mPlaybackFragment?.tickle()
                mPlaybackFragment?.fastForward()
                return true
            }

            if (keycode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (!mPlaybackFragment!!.isControlsOverlayVisible) {
                    mArrowSkipJump = true
                }
                mPlaybackFragment?.tickle(mArrowSkipJump, !mArrowSkipJump)

                if (isSeekBar) {
                    mPlaybackFragment?.fastForward()
                    return true
                }
            }

            if (keycode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                mPlaybackFragment?.tickle()
                mPlaybackFragment?.rewind()
                return true
            }

            if (keycode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (!mPlaybackFragment!!.isControlsOverlayVisible) {
                    mArrowSkipJump = true
                }

                mPlaybackFragment?.tickle(mArrowSkipJump, !mArrowSkipJump)

                if (mArrowSkipJump || isSeekBar) {
                    mPlaybackFragment?.rewind()
                    return true
                }
            }
            if (keycode == KeyEvent.KEYCODE_DPAD_UP) {
                if (!mArrowSkipJump && mPlaybackFragment!!.isControlsOverlayVisible) {
                    if (mPlaybackFragment!!.onControlsUp()) {
                        return true
                    }
                } else {
                    mPlaybackFragment?.tickle(false, true)
                    return true
                }
            }
            if (keycode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (!mPlaybackFragment!!.isControlsOverlayVisible) {
                    mArrowSkipJump = true
                }

                mPlaybackFragment?.tickle(mArrowSkipJump, !mArrowSkipJump)

                if (mArrowSkipJump) {
                    mPlaybackFragment?.jumpForward()
                    return true
                }
            }
            mArrowSkipJump = false
            mPlaybackFragment?.setActions(true)
        } else if (event.action == KeyEvent.ACTION_UP) {
            if ((keycode == KeyEvent.KEYCODE_DPAD_CENTER || keycode == KeyEvent.KEYCODE_ENTER) && !isLongKeyPress) {
                var wasVisible = mPlaybackFragment?.isControlsOverlayVisible
                if (wasVisible == true && mArrowSkipJump) {
                    wasVisible = false
                }
                mArrowSkipJump = false
                mPlaybackFragment?.tickle(false, !mArrowSkipJump)
                if (wasVisible == false) {
                    return true
                }
            }
            isLongKeyPress = false
        }

        val ret = super.dispatchKeyEvent(event)
        mPlaybackFragment?.actionSelected(null)
        return ret
    }

    companion object {
        const val FILM = "film"
        const val STREAM = "stream"
        const val TRANSLATION = "translation"
        const val GAMEPAD_TRIGGER_INTENSITY_ON = 0.5f
        const val GAMEPAD_TRIGGER_INTENSITY_OFF = 0.45f
    }

}
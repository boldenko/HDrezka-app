package com.falcofemoralis.hdrezkaapp.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.falcofemoralis.hdrezkaapp.interfaces.OnFragmentInteractionListener
import com.falcofemoralis.hdrezkaapp.views.fragments.FilmFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Serializable

object FragmentOpener {
    private var isCommitInProgress = false

    fun <T> openWithData(source: Fragment, fragmentListener: OnFragmentInteractionListener, data: T, dataTag: String) where T : Serializable {
        if (isCommitInProgress) {
            return
        }

        isCommitInProgress = true
        val dataBundle = Bundle()
        dataBundle.putSerializable(dataTag, data)
        fragmentListener.onFragmentInteraction(source, FilmFragment(), OnFragmentInteractionListener.Action.NEXT_FRAGMENT_HIDE, true, null, dataBundle, ::callback)
    }

    fun callback() {
        GlobalScope.launch {
            delay(1000)
            isCommitInProgress = false
        }
    }
}
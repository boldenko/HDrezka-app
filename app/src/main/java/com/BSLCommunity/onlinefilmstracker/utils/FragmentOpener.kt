package com.BSLCommunity.onlinefilmstracker.utils

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import com.BSLCommunity.onlinefilmstracker.interfaces.OnFragmentInteractionListener
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.views.fragments.FilmFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object FragmentOpener {
    private var isCommitInProgress = false

    fun openFilm(film: Film, source: Fragment, fragmentListener: OnFragmentInteractionListener) {
        if(isCommitInProgress){
            return
        }

        isCommitInProgress = true
        val data = Bundle()
        data.putSerializable("film", film)
        fragmentListener.onFragmentInteraction(source, FilmFragment(), OnFragmentInteractionListener.Action.NEXT_FRAGMENT_HIDE, true, "film", data, ::callback)
    }

    fun callback(){
        GlobalScope.launch {
            delay(1000)
            isCommitInProgress = false
        }
    }
}
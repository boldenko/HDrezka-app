package com.falcofemoralis.hdrezkaapp.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.falcofemoralis.hdrezkaapp.interfaces.OnFragmentInteractionListener
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.views.fragments.FilmFragment
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
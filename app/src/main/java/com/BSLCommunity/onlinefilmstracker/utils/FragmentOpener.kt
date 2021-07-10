package com.BSLCommunity.onlinefilmstracker.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.views.OnFragmentInteractionListener
import com.BSLCommunity.onlinefilmstracker.views.fragments.FilmFragment

object FragmentOpener {
    fun openFilm(film: Film, source: Fragment, fragmentListener: OnFragmentInteractionListener) {
        val data = Bundle()
        data.putSerializable("film", film)
        fragmentListener.onFragmentInteraction(source, FilmFragment(), OnFragmentInteractionListener.Action.NEXT_FRAGMENT_HIDE, true, null, data)
    }
}
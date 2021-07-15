package com.falcofemoralis.hdrezkaapp.interfaces

import android.os.Bundle
import androidx.fragment.app.Fragment

interface OnFragmentInteractionListener {
    enum class Action {
        NEXT_FRAGMENT_HIDE,
        NEXT_FRAGMENT_REPLACE,
        RETURN_FRAGMENT_BY_TAG,
        POP_BACK_STACK
    }

    fun onFragmentInteraction(fragmentSource: Fragment?, fragmentReceiver: Fragment, action: Action, isBackStack: Boolean, backStackTag: String?, data: Bundle?, callback: (() -> Unit)?)
}
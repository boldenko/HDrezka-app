package com.falcofemoralis.hdrezkaapp.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.falcofemoralis.hdrezkaapp.R

class ActorFragment : Fragment() {
    private lateinit var currentView: View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        currentView = inflater.inflate(R.layout.fragment_actor, container, false)
        return currentView
    }
}
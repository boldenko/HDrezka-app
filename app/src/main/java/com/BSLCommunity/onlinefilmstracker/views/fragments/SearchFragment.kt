package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.BSLCommunity.onlinefilmstracker.R

class SearchFragment : Fragment() {
    private lateinit var currentView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_search, container, false)
        return currentView
    }
}
package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.BSLCommunity.onlinefilmstracker.R

class CategoriesFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d("FRAGMENT_TEST", "cat init")

        return inflater.inflate(R.layout.fragment_categories, container, false)
    }
}
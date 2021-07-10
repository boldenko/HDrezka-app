package com.BSLCommunity.onlinefilmstracker.views.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager.widget.PagerAdapter.POSITION_NONE
import androidx.viewpager2.adapter.FragmentStateAdapter


class ViewPagerAdapter(private val list: ArrayList<Fragment>, fm: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fm, lifecycle) {
    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        return list[position]
    }
}
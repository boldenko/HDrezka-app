package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.views.adapters.ViewPagerAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView


class ViewPagerFragment : Fragment() {
    private lateinit var currentView: View
    private lateinit var viewPager2: ViewPager2

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_view_pager, container, false)

        setBottomBar()
        setAdapter()

        return currentView
    }

    private fun setBottomBar() {
        val bottomNavigationView = currentView.findViewById<BottomNavigationView>(R.id.fragment_pager_nv_bottomBar)
        viewPager2 = currentView.findViewById(R.id.fragment_pager_viewPager2)

        viewPager2.offscreenPageLimit = 5
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> bottomNavigationView.menu.findItem(R.id.nav_newest).isChecked = true
                    1 -> bottomNavigationView.menu.findItem(R.id.nav_categories).isChecked = true
                    2 -> bottomNavigationView.menu.findItem(R.id.nav_search).isChecked = true
                    3 -> bottomNavigationView.menu.findItem(R.id.nav_bookmarks).isChecked = true
                    4 -> bottomNavigationView.menu.findItem(R.id.nav_watch).isChecked = true
                }
            }
        })

        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            val pagerItem = when (item.itemId) {
                R.id.nav_newest -> 0
                R.id.nav_categories -> 1
                R.id.nav_search -> 2
                R.id.nav_bookmarks -> 3
                R.id.nav_watch -> 4
                else -> 0
            }
            viewPager2.currentItem = pagerItem
            true
        }
        bottomNavigationView.selectedItemId = R.id.nav_newest // default select
    }


    fun setAdapter() {
        val fragmentList = arrayListOf(
            NewestFilmsFragment(),
            CategoriesFragment(),
            SearchFragment(),
            BookmarksFragment(),
            WatchLaterFragment()

        )
        viewPager2.adapter = ViewPagerAdapter(fragmentList, requireActivity().supportFragmentManager, lifecycle)
    }
}
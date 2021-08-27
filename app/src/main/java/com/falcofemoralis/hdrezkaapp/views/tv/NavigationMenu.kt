package com.falcofemoralis.hdrezkaapp.views.tv

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.constants.NavigationMenuTabs
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import com.falcofemoralis.hdrezkaapp.views.tv.interfaces.FragmentChangeListener
import com.falcofemoralis.hdrezkaapp.views.tv.interfaces.NavigationStateListener

class NavigationMenu : Fragment() {
    private lateinit var fragmentChangeListener: FragmentChangeListener
    private lateinit var navigationStateListener: NavigationStateListener
    private lateinit var currentView: ConstraintLayout

    private lateinit var newest_IB: ImageButton
    private lateinit var categories_IB: ImageButton
    private lateinit var search_IB: ImageButton
    private lateinit var bookmarks_IB: ImageButton
    private lateinit var later_IB: ImageButton
    private lateinit var settings_IB: ImageButton

    private lateinit var newest_TV: TextView
    private lateinit var categories_TV: TextView
    private lateinit var search_TV: TextView
    private lateinit var bookmarks_TV: TextView
    private lateinit var later_TV: TextView
    private lateinit var settings_TV: TextView

    private val newestFilms = NavigationMenuTabs.nav_menu_newest
    private val categories = NavigationMenuTabs.nav_menu_categories
    private val search = NavigationMenuTabs.nav_menu_search
    private val bookmarks = NavigationMenuTabs.nav_menu_bookmarks
    private val later = NavigationMenuTabs.nav_menu_later
    private var settings = NavigationMenuTabs.nav_menu_settings

    private var lastSelectedMenu: String? = newestFilms
    private var menuTextAnimationDelay = 0 //200

    companion object {
        var isFree = true
        var isFocusOut = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        currentView = inflater.inflate(R.layout.fragment_nav_menu, container, false) as ConstraintLayout

        newest_IB = currentView.findViewById(R.id.newest_IB)
        categories_IB = currentView.findViewById(R.id.categories_IB)
        search_IB = currentView.findViewById(R.id.search_IB)
        bookmarks_IB = currentView.findViewById(R.id.bookmarks_IB)
        later_IB = currentView.findViewById(R.id.later_IB)
        settings_IB = currentView.findViewById(R.id.settings_IB)

        newest_TV = currentView.findViewById(R.id.newest_TV)
        categories_TV = currentView.findViewById(R.id.categories_TV)
        search_TV = currentView.findViewById(R.id.search_TV)
        bookmarks_TV = currentView.findViewById(R.id.bookmarks_TV)
        later_TV = currentView.findViewById(R.id.later_TV)
        settings_TV = currentView.findViewById(R.id.settings_TV)

        return currentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //by default selection
        SettingsData.mainScreen?.let {
            when (it) {
                0 -> setMenuIconFocusView(R.drawable.ic_baseline_movie_24_sel, newest_IB)
                1 -> setMenuIconFocusView(R.drawable.ic_baseline_categories_24_sel, categories_IB)
                2 -> setMenuIconFocusView(R.drawable.ic_baseline_search_24_sel, search_IB)
                3 -> setMenuIconFocusView(R.drawable.ic_baseline_bookmarks_24_sel, bookmarks_IB)
                4 -> setMenuIconFocusView(R.drawable.ic_baseline_watch_later_24_sel, later_IB)
                else -> setMenuIconFocusView(R.drawable.ic_baseline_movie_24_sel, newest_IB)
            }
        }

        //Navigation Menu Options Focus, Key Listeners
        setListener(newest_IB, newest_TV, newestFilms, R.drawable.ic_baseline_movie_24_sel, R.drawable.ic_baseline_movie_24)

        setListener(categories_IB, categories_TV, categories, R.drawable.ic_baseline_categories_24_sel, R.drawable.ic_baseline_categories_24)

        setListener(search_IB, search_TV, search, R.drawable.ic_baseline_search_24_sel, R.drawable.ic_baseline_search_24)

        setListener(bookmarks_IB, bookmarks_TV, bookmarks, R.drawable.ic_baseline_bookmarks_24_sel, R.drawable.ic_baseline_bookmarks_24)

        setListener(later_IB, later_TV, later, R.drawable.ic_baseline_watch_later_24_sel, R.drawable.ic_baseline_watch_later_24)

        setListener(settings_IB, settings_TV, settings, R.drawable.ic_baseline_settings_24_sel, R.drawable.ic_baseline_settings_24)
    }

    private fun setListener(ib: ImageButton, tv: TextView, lastMenu: String, selectedImage: Int, unselectedImage: Int) {
        ib.setOnFocusChangeListener { v, hasFocus ->
            if (isFree) {
                if (hasFocus) {
                    if (isNavigationOpen()) {
                        setFocusedView(ib, selectedImage)
                        setMenuNameFocusView(tv, true)
                        focusIn(ib)
                    } else {
                        openNav()
                    }
                } else {
                    if (isNavigationOpen()) {
                        // false by default,
                        if (isFocusOut) {
                            isFocusOut = false
                        } else {
                            setOutOfFocusedView(ib, unselectedImage)
                        }
                        setMenuNameFocusView(tv, false)
                        focusOut(ib)
                    }
                }
            }
        }

        ib.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {//only when key is pressed down
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        isFocusOut = true
                        closeNav()
                        navigationStateListener.onStateChanged(false, lastSelectedMenu)
                    }
                    KeyEvent.KEYCODE_ENTER -> {
                        lastSelectedMenu = lastMenu
                        fragmentChangeListener.switchFragment(lastMenu)
                        // closeNav()
                    }
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        if (!ib.isFocusable)
                            ib.isFocusable = true
                    }
                    KeyEvent.KEYCODE_DPAD_CENTER -> {
                        lastSelectedMenu = lastMenu
                        fragmentChangeListener.switchFragment(lastMenu)
                        closeNav()
                    }
                }
            }
            false
        }
    }

    private fun setOutOfFocusedView(view: ImageButton, resource: Int) {
        setMenuIconFocusView(resource, view)
    }

    private fun setFocusedView(view: ImageButton, resource: Int) {
        setMenuIconFocusView(resource, view)
    }

    /**
     * Setting animation when focus is lost
     */
    private fun focusOut(v: View) {
        val scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1.2f, 1f)
        val set = AnimatorSet()
        set.play(scaleX).with(scaleY)
        set.start()
    }

    /**
     * Setting the animation when getting focus
     */
    private fun focusIn(v: View) {
        val scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1.2f)
        val scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1.2f)
        val set = AnimatorSet()
        set.play(scaleX).with(scaleY)
        set.start()
    }

    private fun setMenuIconFocusView(resource: Int, view: ImageButton) {
        view.setImageResource(resource)
    }

    private fun setMenuNameFocusView(view: TextView, inFocus: Boolean) {
        if (inFocus) {
            view.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.main_color_3
                )
            )
        } else
            view.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
    }

    private fun openNav() {
        enableNavMenuViews(View.VISIBLE)
        val lp = FrameLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        currentView.layoutParams = lp
        navigationStateListener.onStateChanged(true, lastSelectedMenu)

        when (lastSelectedMenu) {
            categories -> {
                categories_IB.requestFocus()
                setMenuNameFocusView(categories_TV, true)
            }
            newestFilms -> {
                newest_IB.requestFocus()
                setMenuNameFocusView(newest_TV, true)
            }
            search -> {
                search_IB.requestFocus()
                setMenuNameFocusView(search_TV, true)
            }
            bookmarks -> {
                bookmarks_IB.requestFocus()
                setMenuNameFocusView(bookmarks_TV, true)
            }
            later -> {
                later_IB.requestFocus()
                setMenuNameFocusView(later_TV, true)
            }
            settings -> {
                settings_IB.requestFocus()
                setMenuNameFocusView(settings_TV, true)
            }
        }
    }

    private fun closeNav() {
        enableNavMenuViews(View.GONE)
        val lp = FrameLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        currentView.layoutParams = lp

        //highlighting last selected menu icon
        highlightMenuSelection(lastSelectedMenu)

        //Setting out of focus views for menu icons, names
        unHighlightMenuSelections(lastSelectedMenu)
    }

    private fun unHighlightMenuSelections(lastSelectedMenu: String?) {
        if (!lastSelectedMenu.equals(newestFilms, true)) {
            setOutOfFocusedView(newest_IB, R.drawable.ic_baseline_movie_24)
            setMenuNameFocusView(newest_TV, false)
        }
        if (!lastSelectedMenu.equals(categories, true)) {
            setOutOfFocusedView(categories_IB, R.drawable.ic_baseline_categories_24)
            setMenuNameFocusView(categories_TV, false)
        }
        if (!lastSelectedMenu.equals(search, true)) {
            setOutOfFocusedView(search_IB, R.drawable.ic_baseline_search_24)
            setMenuNameFocusView(search_TV, false)
        }
        if (!lastSelectedMenu.equals(bookmarks, true)) {
            setOutOfFocusedView(bookmarks_IB, R.drawable.ic_baseline_bookmarks_24)
            setMenuNameFocusView(bookmarks_TV, false)
        }
        if (!lastSelectedMenu.equals(later, true)) {
            setOutOfFocusedView(later_IB, R.drawable.ic_baseline_watch_later_24)
            setMenuNameFocusView(later_TV, false)
        }
        if (!lastSelectedMenu.equals(settings, true)) {
            setOutOfFocusedView(settings_IB, R.drawable.ic_baseline_settings_24)
            setMenuNameFocusView(settings_TV, false)
        }
    }

    private fun highlightMenuSelection(lastSelectedMenu: String?) {
        when (lastSelectedMenu) {
            newestFilms -> {
                setFocusedView(newest_IB, R.drawable.ic_baseline_movie_24_sel)
            }
            categories -> {
                setFocusedView(categories_IB, R.drawable.ic_baseline_categories_24_sel)
            }
            search -> {
                setFocusedView(search_IB, R.drawable.ic_baseline_search_24_sel)
            }
            bookmarks -> {
                setFocusedView(bookmarks_IB, R.drawable.ic_baseline_bookmarks_24_sel)
            }
            later -> {
                setFocusedView(later_IB, R.drawable.ic_baseline_watch_later_24_sel)
            }
            settings -> {
                setFocusedView(settings_IB, R.drawable.ic_baseline_settings_24_sel)
            }
        }
    }

    private fun enableNavMenuViews(visibility: Int) {
        if (visibility == View.GONE) {
            animateMenuNamesEntry(newest_TV, visibility, 1, R.anim.slide_in_right_menu_name)
            /* menuTextAnimationDelay = 0//200 //reset
             newest_TV.visibility = visibility
             categories_TV.visibility = visibility
             search_TV.visibility = visibility
             bookmarks_TV.visibility = visibility
             later_TV.visibility = visibility
             settings_TV.visibility = visibility*/
        } else {
            animateMenuNamesEntry(newest_TV, visibility, 1, R.anim.slide_in_left_menu_name)
        }
    }

    private fun animateMenuNamesEntry(view: View, visibility: Int, viewCode: Int, anim: Int) {
        view.postDelayed({
            val animate = AnimationUtils.loadAnimation(context, anim)
            if (visibility == View.GONE) {
                val duration = context?.resources?.getInteger(R.integer.animation_duration)?.toLong()
                duration?.let {
                    animate.duration = it
                }
                animate.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        view.visibility = visibility
                    }

                    override fun onAnimationRepeat(animation: Animation?) {
                    }
                })
            } else {
                view.visibility = visibility
            }
            view.startAnimation(animate)

            menuTextAnimationDelay = 0 // 100

            // step by step animation
            when (viewCode) {
                1 -> {
                    animateMenuNamesEntry(categories_TV, visibility, viewCode + 1, anim)
                }
                2 -> {
                    animateMenuNamesEntry(search_TV, visibility, viewCode + 1, anim)
                }
                3 -> {
                    animateMenuNamesEntry(bookmarks_TV, visibility, viewCode + 1, anim)
                }
                4 -> {
                    animateMenuNamesEntry(later_TV, visibility, viewCode + 1, anim)
                }
                5 -> {
                    animateMenuNamesEntry(settings_TV, visibility, viewCode + 1, anim)
                }
            }
        }, menuTextAnimationDelay.toLong())
    }

    private fun isNavigationOpen() = newest_TV.visibility == View.VISIBLE

    fun setFragmentChangeListener(callback: FragmentChangeListener) {
        this.fragmentChangeListener = callback
    }

    fun setNavigationStateListener(callback: NavigationStateListener) {
        this.navigationStateListener = callback
    }
}

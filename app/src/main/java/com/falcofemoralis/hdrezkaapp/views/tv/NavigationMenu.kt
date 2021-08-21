package com.falcofemoralis.hdrezkaapp.views.tv

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.views.tv.interfaces.FragmentChangeListener
import com.falcofemoralis.hdrezkaapp.views.tv.interfaces.NavigationStateListener
import com.falcofemoralis.hdrezkaapp.constants.NavigationMenuTabs

class NavigationMenu : Fragment() {
    private lateinit var fragmentChangeListener: FragmentChangeListener
    private lateinit var navigationStateListener: NavigationStateListener
    private lateinit var currentView: ConstraintLayout
    private lateinit var newest_IB: ImageButton
    private lateinit var categories_IB: ImageButton
    private lateinit var newest_TV: TextView
    private lateinit var categories_TV: TextView

    private val newestFilms = NavigationMenuTabs.nav_menu_newest
    private val categories = NavigationMenuTabs.nav_menu_categories
    private var lastSelectedMenu: String? = newestFilms
    private var newestAllowedToGainFocus = false
    private var categoriesAllowedToGainFocus = true

    /*    private var settingsAllowedToGainFocus = false
        private var musicAllowedToGainFocus = false
        private var podcastsAllowedToGainFocus = false
        private var newsAllowedToGainFocus = false*/

    private var switchUserAllowedToGainFocus = false
    private var menuTextAnimationDelay = 0 //200


    companion object {
        var isFree = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        currentView = inflater.inflate(R.layout.tv_fragment_nav_menu, container, false) as ConstraintLayout
        newest_IB = currentView.findViewById(R.id.newest_IB)
        categories_IB = currentView.findViewById(R.id.categories_IB)

        newest_TV = currentView.findViewById(R.id.newest_TV)
        categories_TV = currentView.findViewById(R.id.categories_TV)

        return currentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //by default selection
        setMenuIconFocusView(R.drawable.ic_baseline_movie_24, newest_IB, false)

        //Navigation Menu Options Focus, Key Listeners
        setListener(newest_IB, newest_TV, newestFilms, R.drawable.ic_baseline_movie_24_sel, R.drawable.ic_baseline_movie_24)

        setListener(categories_IB, categories_TV, categories, R.drawable.ic_baseline_categories_24_sel, R.drawable.ic_baseline_categories_24)

    }

    private fun setListener(ib: ImageButton, tv: TextView, lastMenu: String, selectedImage: Int, unselectedImage: Int) {
        ib.setOnFocusChangeListener { v, hasFocus ->
            if (isFree) {
                if (hasFocus) {
                    if (isNavigationOpen()) {
                        setFocusedView(ib, selectedImage)
                        setMenuNameFocusView(tv, true)
                        focusIn(ib, 0)
                        Log.d("NAV_TEST", "0 $lastMenu opened and has focus")
                    } else {
                        Log.d("NAV_TEST", "1 $lastMenu close and has focus")
                        Log.d("NAV_TEST", "1 openNav()")
                        openNav()
                    }
                } else {
                    if (isNavigationOpen()) {
                        setOutOfFocusedView(ib, unselectedImage)
                        setMenuNameFocusView(tv, false)
                        focusOut(ib, 0)
                        Log.d("NAV_TEST", "2 $lastMenu opened and hasn't focus")
                        Log.d("NAV_TEST", "2 !!!!!!!")
                    } else {
                        Log.d("NAV_TEST", "3 $lastMenu close and hasn't focus")
                    }
                }
            }
        }

        ib.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {//only when key is pressed down
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        Log.d("NAV_TEST", "closeNav() on right")
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
                        switchUserAllowedToGainFocus = true
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
        setMenuIconFocusView(resource, view, false)
    }

    private fun setFocusedView(view: ImageButton, resource: Int) {
        setMenuIconFocusView(resource, view, true)
    }


    /**
     * Setting animation when focus is lost
     */
    fun focusOut(v: View, position: Int) {
        val scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1.2f, 1f)
        val set = AnimatorSet()
        set.play(scaleX).with(scaleY)
        set.start()
    }

    /**
     * Setting the animation when getting focus
     */
    fun focusIn(v: View, position: Int) {
        val scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1.2f)
        val scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1.2f)
        val set = AnimatorSet()
        set.play(scaleX).with(scaleY)
        set.start()
    }

    private fun setMenuIconFocusView(resource: Int, view: ImageButton, inFocus: Boolean) {
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

    fun openNav() {
        enableNavMenuViews(View.VISIBLE)
        val lp = FrameLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        currentView.layoutParams = lp
        navigationStateListener.onStateChanged(true, lastSelectedMenu)

        when (lastSelectedMenu) {
            categories -> {
                categories_IB.requestFocus()
                categoriesAllowedToGainFocus = true
                setMenuNameFocusView(categories_TV, true)
            }
            newestFilms -> {
                newest_IB.requestFocus()
                newestAllowedToGainFocus = true
                setMenuNameFocusView(newest_TV, true)
            }
            /*    podcasts -> {
                    podcasts_IB.requestFocus()
                    podcastsAllowedToGainFocus = true
                    setMenuNameFocusView(podcasts_TV, true)
                }
                music -> {
                    music_IB.requestFocus()
                    musicAllowedToGainFocus = true
                    setMenuNameFocusView(music_TV, true)
                }
                news -> {
                    news_IB.requestFocus()
                    newsAllowedToGainFocus = true
                    setMenuNameFocusView(news_TV, true)
                }
                settings -> {
                    settings_IB.requestFocus()
                    settingsAllowedToGainFocus = true
                    setMenuNameFocusView(settings_TV, true)
                }*/
        }

    }

    fun closeNav() {
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
        /*  if (!lastSelectedMenu.equals(podcasts, true)) {
              setOutOfFocusedView(podcasts_IB, R.drawable.ic_podcast_unselected)
              setMenuNameFocusView(podcasts_TV, false)
          }
          if (!lastSelectedMenu.equals(music, true)) {
              setOutOfFocusedView(music_IB, R.drawable.ic_music_unselected)
              setMenuNameFocusView(music_TV, false)
          }
          if (!lastSelectedMenu.equals(news, true)) {
              setOutOfFocusedView(news_IB, R.drawable.ic_news_unselected)
              setMenuNameFocusView(news_TV, false)
          }
          if (!lastSelectedMenu.equals(settings, true)) {
              setOutOfFocusedView(settings_IB, R.drawable.ic_settings_unselected)
              setMenuNameFocusView(settings_TV, false)
          }*/
    }

    private fun highlightMenuSelection(lastSelectedMenu: String?) {
        when (lastSelectedMenu) {
            newestFilms -> {
                setFocusedView(newest_IB, R.drawable.ic_baseline_movie_24_sel)
            }
            categories -> {
                setFocusedView(categories_IB, R.drawable.ic_baseline_categories_24_sel)
            }
            /*  podcasts -> {
                  setFocusedView(podcasts_IB, R.drawable.ic_podcast_selected)
              }
              music -> {
                  setFocusedView(music_IB, R.drawable.ic_music_selected)
              }
              news -> {
                  setFocusedView(news_IB, R.drawable.ic_news_selected)
              }
              settings -> {
                  setFocusedView(settings_IB, R.drawable.ic_settings_selected)
              }*/
        }
    }

    private fun enableNavMenuViews(visibility: Int) {
        if (visibility == View.GONE) {
            menuTextAnimationDelay = 0//200 //reset
            newest_TV.visibility = visibility
            categories_TV.visibility = visibility
            /*  news_TV.visibility = visibility
              music_TV.visibility = visibility
              podcasts_TV.visibility = visibility
              settings_TV.visibility = visibility*/
        } else {
            animateMenuNamesEntry(newest_TV, visibility, 1)
        }
    }

    private fun animateMenuNamesEntry(view: View, visibility: Int, viewCode: Int) {
        view.postDelayed({
            view.visibility = visibility
            val animate = AnimationUtils.loadAnimation(context, R.anim.slide_in_left_menu_name)
            view.startAnimation(animate)
            menuTextAnimationDelay = 100
            when (viewCode) {
                1 -> {
                    animateMenuNamesEntry(categories_TV, visibility, viewCode + 1)
                }
                /*         2 -> {
                             animateMenuNamesEntry(podcasts_TV, visibility, viewCode + 1)
                         }
                         3 -> {
                             animateMenuNamesEntry(music_TV, visibility, viewCode + 1)
                         }
                         4 -> {
                             animateMenuNamesEntry(news_TV, visibility, viewCode + 1)
                         }
                         5 -> {
                             animateMenuNamesEntry(settings_TV, visibility, viewCode + 1)
                         }*/
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

    fun setSelectedMenu(navMenuName: String) {
        when (navMenuName) {
            categories -> {
                lastSelectedMenu = categories
            }
            newestFilms -> {
                lastSelectedMenu = newestFilms
            }
        }

        highlightMenuSelection(lastSelectedMenu)
        unHighlightMenuSelections(lastSelectedMenu)

    }
}

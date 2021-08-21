package com.falcofemoralis.hdrezkaapp.views

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.constants.DeviceType
import com.falcofemoralis.hdrezkaapp.constants.NavigationMenuTabs
import com.falcofemoralis.hdrezkaapp.constants.UpdateItem
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.interfaces.IPagerView
import com.falcofemoralis.hdrezkaapp.interfaces.NavigationMenuCallback
import com.falcofemoralis.hdrezkaapp.interfaces.OnFragmentInteractionListener
import com.falcofemoralis.hdrezkaapp.interfaces.OnFragmentInteractionListener.Action
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import com.falcofemoralis.hdrezkaapp.objects.UserData
import com.falcofemoralis.hdrezkaapp.utils.ConnectionManager.isInternetAvailable
import com.falcofemoralis.hdrezkaapp.utils.ConnectionManager.showConnectionErrorDialog
import com.falcofemoralis.hdrezkaapp.utils.FragmentOpener
import com.falcofemoralis.hdrezkaapp.views.fragments.*
import com.falcofemoralis.hdrezkaapp.views.tv.NavigationMenu
import com.falcofemoralis.hdrezkaapp.views.tv.interfaces.FragmentChangeListener
import com.falcofemoralis.hdrezkaapp.views.tv.interfaces.NavigationStateListener
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity(), OnFragmentInteractionListener, IConnection, IPagerView, NavigationStateListener, FragmentChangeListener,
    NavigationMenuCallback {
    private var isSettingsOpened: Boolean = false
    private lateinit var mainFragment: Fragment
    private lateinit var currentFragment: Fragment
    private var savedInstanceState: Bundle? = null
    private lateinit var interfaceMode: Number

    /* TV */
    private lateinit var navMenuFragment: NavigationMenu
    private lateinit var newestFilmsFragment: NewestFilmsFragment
    private lateinit var categoriesFragment: CategoriesFragment
    private lateinit var searchFragment: SearchFragment
    private lateinit var bookmarksFragment: BookmarksFragment
    private lateinit var watchLaterFragment: WatchLaterFragment
    private lateinit var navFragmentLayout: FrameLayout
    private lateinit var settingsFragment: UserFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        interfaceMode = (getSystemService(UI_MODE_SERVICE) as UiModeManager).currentModeType
        when (interfaceMode) {
            Configuration.UI_MODE_TYPE_TELEVISION -> setContentView(R.layout.tv_activity_main)
            else -> setContentView(R.layout.activity_main)
        }

        this.savedInstanceState = savedInstanceState
        initApp()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun initApp() {
        if (isInternetAvailable(applicationContext)) {
            if (savedInstanceState == null) {
                UserData.init(applicationContext)

                when (interfaceMode) {
                    Configuration.UI_MODE_TYPE_TELEVISION -> {
                        SettingsData.init(applicationContext, DeviceType.TV)
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                        navFragmentLayout = findViewById(R.id.nav_fragment)

                        navMenuFragment = NavigationMenu()
                        supportFragmentManager.beginTransaction().replace(R.id.nav_fragment, navMenuFragment).commit()

                        newestFilmsFragment = NewestFilmsFragment()
                        mainFragment = newestFilmsFragment
                        onFragmentInteraction(null, newestFilmsFragment, Action.NEXT_FRAGMENT_REPLACE, false, null, null, null)
                    }
                    else -> {
                        SettingsData.init(applicationContext, DeviceType.MOBILE)
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        mainFragment = ViewPagerFragment()
                        onFragmentInteraction(null, mainFragment, Action.NEXT_FRAGMENT_REPLACE, false, null, null, null)

                        createUserMenu()
                        setUserAvatar()
                    }
                }

                if (intent.data != null) {
                    val link = SettingsData.provider + intent.data.toString().replace("${intent.data!!.scheme}://", "").replace(intent.data!!.host ?: "", "")
                    FragmentOpener.openWithData(mainFragment, this, Film(link), "film")
                }
            }
        } else {
            showConnectionError(IConnection.ErrorType.NO_INTERNET, "")
        }
    }

    private fun createUserMenu() {
        findViewById<ImageView>(R.id.activity_main_iv_user).setOnClickListener {
            openUserMenu()
        }
    }

    fun openUserMenu() {
        if (!isSettingsOpened) {
            val f = if (mainFragment.isVisible) mainFragment
            else currentFragment
            onFragmentInteraction(f, UserFragment(), Action.NEXT_FRAGMENT_HIDE, true, null, null, null)
            isSettingsOpened = true
        }
    }

    fun setUserAvatar() {
        val imageView: ImageView = findViewById(R.id.activity_main_iv_user)
        if (UserData.avatarLink != null && UserData.avatarLink!!.isNotEmpty()) {
            Picasso.get().load(UserData.avatarLink).into(imageView)
        } else {
            imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.no_avatar))
        }
    }

    override fun updatePager() {
        (mainFragment as ViewPagerFragment).setAdapter()
    }

    override fun redrawPage(item: UpdateItem) {
        (mainFragment as ViewPagerFragment).updatePage(item)
    }

    override fun onFragmentInteraction(fragmentSource: Fragment?, fragmentReceiver: Fragment, action: Action, isBackStack: Boolean, backStackTag: String?, data: Bundle?, callback: (() -> Unit)?) {
        val fTrans: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentReceiver.arguments = data

        val animIn = R.anim.fade_in
        val animOut = R.anim.fade_out
        fTrans.setCustomAnimations(animIn, animOut, animIn, animOut)

        when (action) {
            Action.NEXT_FRAGMENT_HIDE -> {
                if (mainFragment.isVisible) fTrans.hide(mainFragment)
                else fragmentSource?.let { fTrans.hide(it) }

                val frags: List<Fragment> = supportFragmentManager.fragments
                var f: Fragment? = null
                for (fragment in frags) {
                    if (fragment == fragmentReceiver) {
                        f = fragment
                        break
                    }
                }
                currentFragment = fragmentReceiver

                if (f == null) {
                    fTrans.add(R.id.activity_main_fcv_container, fragmentReceiver)
                } else {
                    fTrans.show(fragmentReceiver)
                }

                if (isBackStack) {
                    fTrans.addToBackStack(backStackTag)
                }
                fTrans.commit()
            }
            Action.NEXT_FRAGMENT_REPLACE -> {
                fTrans.replace(R.id.activity_main_fcv_container, fragmentReceiver)
                if (isBackStack) {
                    fTrans.addToBackStack(backStackTag)
                }
                fTrans.commit()
            }
            Action.RETURN_FRAGMENT_BY_TAG -> supportFragmentManager.popBackStack(backStackTag, 0)
            Action.POP_BACK_STACK -> supportFragmentManager.popBackStack()
        }

        supportFragmentManager.addOnBackStackChangedListener {
            if (callback != null) {
                callback()
            }
        }
    }

    override fun showConnectionError(type: IConnection.ErrorType, errorText: String) {
        showConnectionErrorDialog(this, type, ::initApp)
    }

    override fun onBackPressed() {
        if (isSettingsOpened) {
            isSettingsOpened = false
        }
        super.onBackPressed()
    }

    /* TV */
    override fun switchFragment(fragmentName: String?) {
        var fragmentTo: Fragment? = null

        when (fragmentName) {
            NavigationMenuTabs.nav_menu_newest -> {
                newestFilmsFragment = NewestFilmsFragment()
                fragmentTo = newestFilmsFragment
            }
            NavigationMenuTabs.nav_menu_categories -> {
                categoriesFragment = CategoriesFragment()
                fragmentTo = categoriesFragment
            }
            NavigationMenuTabs.nav_menu_search -> {
                searchFragment = SearchFragment()
                fragmentTo = searchFragment
            }
            NavigationMenuTabs.nav_menu_bookmarks -> {
                bookmarksFragment = BookmarksFragment()
                fragmentTo = bookmarksFragment
            }
            NavigationMenuTabs.nav_menu_later -> {
                watchLaterFragment = WatchLaterFragment()
                fragmentTo = watchLaterFragment
            }
            NavigationMenuTabs.nav_menu_settings -> {
                settingsFragment = UserFragment()
                fragmentTo = settingsFragment
            }
        }

        if (fragmentTo != null) {
            mainFragment = fragmentTo
            onFragmentInteraction(null, fragmentTo, Action.NEXT_FRAGMENT_REPLACE, false, null, null, null)
        }
    }

    override fun onStateChanged(expanded: Boolean, lastSelected: String?) {
    }

    override fun navMenuToggle(toShow: Boolean) {
    }

    override fun onAttachFragment(fragment: Fragment) {
        when (fragment) {
            is NavigationMenu -> {
                fragment.setFragmentChangeListener(this)
                fragment.setNavigationStateListener(this)
            }
        }
    }
}
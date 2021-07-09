package com.BSLCommunity.onlinefilmstracker.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.models.UserModel
import com.BSLCommunity.onlinefilmstracker.views.OnFragmentInteractionListener.Action
import com.BSLCommunity.onlinefilmstracker.views.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), OnFragmentInteractionListener, INoConnection {
    private var isSettingsOpened: Boolean = false
    private var selectedFragment: Fragment? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isInternetAvailable(applicationContext)) {
            if (savedInstanceState == null) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                UserModel.loadLoggedIn(applicationContext)
                setBottomBar()
                createUserMenu()
            }
        } else {
            showErrorDialog()
        }
    }

    private fun createUserMenu() {
        findViewById<ImageView>(R.id.activity_main_ib_user).setOnClickListener {
            if (!isSettingsOpened) {
                onFragmentInteraction(UserFragment(), Action.NEXT_FRAGMENT_HIDE, null, true, null)
                isSettingsOpened = true
            }
        }
    }

    private fun setBottomBar() {
        val newestFragment = NewestFilmsFragment()
        val bookmarksFragment = BookmarksFragment()
        val searchFragment = SearchFragment()
        val watchLaterFragment = WatchLaterFragment()
        val categoriesFragment = CategoriesFragment()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.activity_main_nv_bottomBar)
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_newest -> newestFragment
                R.id.nav_categories -> categoriesFragment
                R.id.nav_search -> searchFragment
                R.id.nav_bookmarks -> bookmarksFragment
                R.id.nav_watch -> watchLaterFragment
                else -> Fragment()
            }
            onFragmentInteraction(fragment, Action.NEXT_FRAGMENT_HIDE, null, false, null)
            selectedFragment = fragment
            false
        }
        bottomNavigationView.selectedItemId = R.id.nav_newest // default select
    }

    override fun onBackPressed() {
        if (isSettingsOpened) {
            isSettingsOpened = false
        }

/*        val fragments = supportFragmentManager.fragments
        if (fragments.size > 1) {
            selectedFragment = fragments[fragments.size - 2]
            Log.d("SEL_FR", "set on backpressed" + selectedFragment.toString())
        }*/
        super.onBackPressed()
    }

    override fun onFragmentInteraction(fragmentReceiver: Fragment, action: Action, data: Bundle?, isBackStack: Boolean, backStackTag: String?) {
        val fTrans: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentReceiver.arguments = data

        val animIn = R.anim.fade_in
        val animOut = R.anim.fade_out
        fTrans.setCustomAnimations(animIn, animOut, animIn, animOut)

        when (action) {
            Action.NEXT_FRAGMENT_HIDE -> {
                selectedFragment?.let {
                    if (selectedFragment!!.isVisible) fTrans.hide(selectedFragment!!)
                    // else fragmentSource?.let { it1 -> fTrans.hide(it1) }
                }

                Log.d("SEL_FR", "set on hide" + selectedFragment.toString())

                val frags: List<Fragment> = supportFragmentManager.fragments
                var f: Fragment? = null
                for (fragment in frags) {
                    if (fragment == fragmentReceiver) {
                        f = fragment
                        break
                    }
                }
                if (f == null) {
                    fTrans.add(R.id.main_fragment_container, fragmentReceiver)
                } else {
                    fTrans.show(fragmentReceiver)
                }
                if (isBackStack) {
                    fTrans.addToBackStack(backStackTag)
                }
                fTrans.commit()
            }
            Action.NEXT_FRAGMENT_REPLACE -> {
                fTrans.replace(R.id.main_fragment_container, fragmentReceiver)
                if (isBackStack) {
                    fTrans.addToBackStack(backStackTag)
                }
                fTrans.commit()
            }
            Action.RETURN_FRAGMENT_BY_TAG -> supportFragmentManager.popBackStack(backStackTag, 0)
            Action.POP_BACK_STACK -> supportFragmentManager.popBackStack()
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        var connection = false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            connection = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    connection = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }

                }
            }
        }

        return connection
    }

    override fun showErrorDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
        dialog.setTitle("Ошибка. Отсутствует интернет соеденение")
        dialog.setPositiveButton("Выйти") { dialog, id ->
            exitProcess(0)
        }
        val d = dialog.create()
        d.show()
    }
}
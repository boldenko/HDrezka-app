package com.BSLCommunity.onlinefilmstracker.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.models.UserModel
import com.BSLCommunity.onlinefilmstracker.views.OnFragmentInteractionListener.Action
import com.BSLCommunity.onlinefilmstracker.views.fragments.BookmarksFragment
import com.BSLCommunity.onlinefilmstracker.views.fragments.NewestFilmsFragment
import com.BSLCommunity.onlinefilmstracker.views.fragments.UserFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), OnFragmentInteractionListener {
    private var isSettingsOpened: Boolean = false
    private lateinit var selectedFragment: Fragment

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var connected = false
        try {
            val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val nInfo = cm.activeNetworkInfo
            connected = nInfo != null && nInfo.isAvailable && nInfo.isConnected
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (connected) {
            if (savedInstanceState == null) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                UserModel.loadLoggedIn(applicationContext)
                selectedFragment = NewestFilmsFragment()
                setBottomBar()

                findViewById<ImageView>(R.id.activity_main_ib_user).setOnClickListener {
                    if (!isSettingsOpened) {
                        onFragmentInteraction(UserFragment(), Action.NEXT_FRAGMENT_HIDE, null, null)
                        isSettingsOpened = true
                    }
                }
            }
        } else {
            val dialog = MaterialAlertDialogBuilder(this)
            dialog.setTitle("Ошибка. Отсутствует интернет соеденение")
            dialog.setPositiveButton("Выйти") { dialog, id ->
                exitProcess(0)
            }
            val d = dialog.create()
            d.show()
        }
    }

    private fun setBottomBar() {
        val newestFragment = NewestFilmsFragment()
        val bookmarksFragment = BookmarksFragment()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.activity_main_nv_bottomBar)
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_newest -> onFragmentInteraction(newestFragment, Action.NEXT_FRAGMENT_HIDE_NO_BACK_STACK, null, null)
                R.id.nav_bookmarks -> onFragmentInteraction(bookmarksFragment, Action.NEXT_FRAGMENT_HIDE_NO_BACK_STACK, null, null)
            }
            false
        }
        bottomNavigationView.selectedItemId = R.id.nav_newest
    }

    override fun onBackPressed() {
        if (isSettingsOpened) {
            isSettingsOpened = false
        }
        super.onBackPressed()
    }

    override fun onFragmentInteraction(fragmentReceiver: Fragment, action: Action, data: Bundle?, backStackTag: String?) {
        val fTrans = supportFragmentManager.beginTransaction()

        fragmentReceiver.arguments = data

        val animIn: Int = R.anim.fade_in
        val animOut: Int = R.anim.fade_out
        fTrans.setCustomAnimations(animIn, animOut, animIn, animOut)

        if (selectedFragment.isVisible) fTrans.hide(selectedFragment)
        selectedFragment = fragmentReceiver

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

        when (action) {
            Action.NEXT_FRAGMENT_HIDE_NO_BACK_STACK -> {
                fTrans.commit()
            }
            Action.NEXT_FRAGMENT_HIDE -> {
                fTrans.addToBackStack(backStackTag) // Добавление изменнений в стек
                fTrans.commit()
            }
/*            Action.NEXT_FRAGMENT_REPLACE -> {
                fTrans.replace(R.id.main_fragment_container, fragmentReceiver)
                fTrans.addToBackStack(backStackTag) // Добавление изменнений в стек
                fTrans.commit()
            }
            Action.RETURN_FRAGMENT_BY_TAG -> supportFragmentManager.popBackStack(backStackTag, 0)
            Action.POP_BACK_STACK -> supportFragmentManager.popBackStack()*/
        }
    }
}
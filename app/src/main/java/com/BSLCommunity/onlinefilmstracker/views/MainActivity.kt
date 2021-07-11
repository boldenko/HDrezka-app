package com.BSLCommunity.onlinefilmstracker.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.models.UserModel
import com.BSLCommunity.onlinefilmstracker.views.interfaces.OnFragmentInteractionListener.Action
import com.BSLCommunity.onlinefilmstracker.views.fragments.UserFragment
import com.BSLCommunity.onlinefilmstracker.views.fragments.ViewPagerFragment
import com.BSLCommunity.onlinefilmstracker.views.interfaces.INoConnection
import com.BSLCommunity.onlinefilmstracker.views.interfaces.OnFragmentInteractionListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), OnFragmentInteractionListener, INoConnection {
    private var isSettingsOpened: Boolean = false
    private lateinit var mainFragment: ViewPagerFragment

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isInternetAvailable(applicationContext)) {
            if (savedInstanceState == null) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                UserModel.loadLoggedIn(applicationContext)
                mainFragment = ViewPagerFragment()
                onFragmentInteraction(null, mainFragment, Action.NEXT_FRAGMENT_REPLACE, false, null, null)
                createUserMenu()
            }
        } else {
            showErrorDialog()
        }
    }

    private fun createUserMenu() {
        findViewById<ImageView>(R.id.activity_main_ib_user).setOnClickListener {
            if (!isSettingsOpened) {
                onFragmentInteraction(mainFragment, UserFragment(), Action.NEXT_FRAGMENT_HIDE, true, null, null)
                isSettingsOpened = true
            }
        }
    }

    override fun onBackPressed() {
        if (isSettingsOpened) {
            isSettingsOpened = false
        }
        super.onBackPressed()
    }

    override fun onFragmentInteraction(fragmentSource: Fragment?, fragmentReceiver: Fragment, action: Action, isBackStack: Boolean, backStackTag: String?, data: Bundle?) {
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

    fun updatePager(){
        mainFragment.setAdapter()
    }
}
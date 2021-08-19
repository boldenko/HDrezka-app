package com.falcofemoralis.hdrezkaapp.views

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.constants.UpdateItem
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.interfaces.OnFragmentInteractionListener
import com.falcofemoralis.hdrezkaapp.interfaces.OnFragmentInteractionListener.Action
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import com.falcofemoralis.hdrezkaapp.objects.UserData
import com.falcofemoralis.hdrezkaapp.utils.FragmentOpener
import com.falcofemoralis.hdrezkaapp.views.atv.MainFragment
import com.falcofemoralis.hdrezkaapp.views.fragments.UserFragment
import com.falcofemoralis.hdrezkaapp.views.fragments.ViewPagerFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), OnFragmentInteractionListener, IConnection {
    private var isSettingsOpened: Boolean = false
    private lateinit var mainFragment: Fragment
    private lateinit var currentFragment: Fragment
    private var savedInstanceState: Bundle? = null
    private lateinit var interfaceMode: Number

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.savedInstanceState = savedInstanceState
        initInterface()
        initApp()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun initInterface() {
        interfaceMode = (getSystemService(UI_MODE_SERVICE) as UiModeManager).currentModeType

        if (interfaceMode == Configuration.UI_MODE_TYPE_TELEVISION) {
            setContentView(R.layout.tv_activity_main)
            setTheme(R.style.Theme_Leanback)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            setContentView(R.layout.activity_main)
            setTheme(R.style.AppTheme)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun initApp() {
        if (isInternetAvailable(applicationContext)) {
            if (savedInstanceState == null) {
                SettingsData.init(applicationContext)
                UserData.init(applicationContext)

                if (interfaceMode == Configuration.UI_MODE_TYPE_TELEVISION) {
                    mainFragment = MainFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.main_browse_fragment, mainFragment).commitNow()
                } else {
                    mainFragment = ViewPagerFragment()
                    onFragmentInteraction(null, mainFragment, Action.NEXT_FRAGMENT_REPLACE, false, null, null, null)
                    createUserMenu()
                    setUserAvatar()

                    if (intent.data != null) {
                        val link = SettingsData.provider + intent.data.toString().replace("${intent.data!!.scheme}://", "").replace(intent.data!!.host ?: "", "")
                        FragmentOpener.openWithData(mainFragment, this, Film(link), "film")
                    }
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

    override fun onBackPressed() {
        if (isSettingsOpened) {
            isSettingsOpened = false
        }
        super.onBackPressed()
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

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        var connection = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }

        if (!connection) {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
            connection = wm?.isWifiEnabled ?: false
        }

        return connection
    }

    fun updatePager() {
        (mainFragment as ViewPagerFragment).setAdapter()
    }

    fun setUserAvatar() {
        val imageView: ImageView = findViewById(R.id.activity_main_iv_user)
        if (UserData.avatarLink != null && UserData.avatarLink!!.isNotEmpty()) {
            Picasso.get().load(UserData.avatarLink).into(imageView)
        } else {
            imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.no_avatar))
        }
    }

    override fun showConnectionError(type: IConnection.ErrorType, errorText: String) {
        if (type == IConnection.ErrorType.NO_INTERNET) {
            val dialog = MaterialAlertDialogBuilder(this)
            dialog.setTitle(getString(R.string.no_connection))
            dialog.setPositiveButton(getString(R.string.exit)) { dialog, id ->
                exitProcess(0)
            }
            dialog.setNegativeButton(getString(R.string.retry)) { dialog, id ->
                initApp()
            }
            val d = dialog.create()
            d.show()
        }
    }

    fun showSimpleMsg(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun redrawPage(item: UpdateItem) {
        (mainFragment as ViewPagerFragment).updatePage(item)
    }
}
package com.falcofemoralis.hdrezkaapp.views

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.chivorn.smartmaterialspinner.SmartMaterialSpinner
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.clients.PlayerJsInterface
import com.falcofemoralis.hdrezkaapp.constants.DeviceType
import com.falcofemoralis.hdrezkaapp.constants.NavigationMenuTabs
import com.falcofemoralis.hdrezkaapp.constants.UpdateItem
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.interfaces.IPagerView
import com.falcofemoralis.hdrezkaapp.interfaces.NavigationMenuCallback
import com.falcofemoralis.hdrezkaapp.interfaces.OnFragmentInteractionListener
import com.falcofemoralis.hdrezkaapp.interfaces.OnFragmentInteractionListener.Action
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import com.falcofemoralis.hdrezkaapp.objects.UserData
import com.falcofemoralis.hdrezkaapp.utils.ConnectionManager.isInternetAvailable
import com.falcofemoralis.hdrezkaapp.utils.ConnectionManager.showConnectionErrorDialog
import com.falcofemoralis.hdrezkaapp.views.fragments.*
import com.falcofemoralis.hdrezkaapp.views.tv.NavigationMenu
import com.falcofemoralis.hdrezkaapp.views.tv.interfaces.FragmentChangeListener
import com.falcofemoralis.hdrezkaapp.views.tv.interfaces.NavigationStateListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), OnFragmentInteractionListener, IConnection, IPagerView, NavigationStateListener, FragmentChangeListener,
    NavigationMenuCallback {
    private var isSettingsOpened: Boolean = false
    private lateinit var mainFragment: Fragment
    private lateinit var currentFragment: Fragment
    private var savedInstanceState: Bundle? = null
    private lateinit var interfaceMode: Number

    /* TV */
    private lateinit var navMenuFragment: NavigationMenu
    private lateinit var navFragmentLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.savedInstanceState = savedInstanceState

        initApp()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun initApp() {
        if (isInternetAvailable(applicationContext)) {
            if (savedInstanceState == null) {
                SettingsData.initProvider(this)
                if (SettingsData.provider == "" || SettingsData.provider == null) {
                    showProviderEnter()
                } else {
                    interfaceMode = (getSystemService(UI_MODE_SERVICE) as UiModeManager).currentModeType
                    when (interfaceMode) {
                        Configuration.UI_MODE_TYPE_TELEVISION -> {
                            SettingsData.init(applicationContext, DeviceType.TV)
                            UserData.init(applicationContext)
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                            navFragmentLayout = findViewById(R.id.nav_fragment)

                            navMenuFragment = NavigationMenu()
                            supportFragmentManager.beginTransaction().replace(R.id.nav_fragment, navMenuFragment).commit()

                            mainFragment = NewestFilmsFragment()
                            onFragmentInteraction(null, mainFragment, Action.NEXT_FRAGMENT_REPLACE, false, null, null, null)
                        }
                        else -> {
                            SettingsData.init(applicationContext, DeviceType.MOBILE)
                            UserData.init(applicationContext)

                            requestedOrientation = if (SettingsData.isAutorotate == true) {
                                ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            }
                            mainFragment = ViewPagerFragment()
                            onFragmentInteraction(null, mainFragment, Action.NEXT_FRAGMENT_REPLACE, false, null, null, null)

                            createUserMenu()
                            setUserAvatar()
                        }
                    }
                }
            }
        } else {
            showConnectionError(IConnection.ErrorType.NO_INTERNET, "")
        }
    }

    private fun createUserMenu() {
        if (SettingsData.deviceType != DeviceType.TV) {
            findViewById<ImageView>(R.id.activity_main_iv_user).setOnClickListener {
                openUserMenu()
            }
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
        if (SettingsData.deviceType != DeviceType.TV) {
            val imageView: ImageView = findViewById(R.id.activity_main_iv_user)
            if (UserData.avatarLink != null && UserData.avatarLink!!.isNotEmpty()) {
                Picasso.get().load(UserData.avatarLink).into(imageView)
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.no_avatar))
            }
        }
    }

    override fun updatePager() {
        if (SettingsData.deviceType != DeviceType.TV) {
            (mainFragment as ViewPagerFragment).setAdapter()
        }
    }

    override fun redrawPage(item: UpdateItem) {
        if (SettingsData.deviceType != DeviceType.TV) {
            (mainFragment as ViewPagerFragment).updatePage(item)
        }
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
            Action.POP_BACK_STACK -> supportFragmentManager.popBackStack()
        }

        supportFragmentManager.addOnBackStackChangedListener {
            if (callback != null) {
                callback()
            }
        }
    }

    override fun findFragmentByTag(tag: String): Fragment? {
        return supportFragmentManager.findFragmentByTag(tag)
    }

    override fun showConnectionError(type: IConnection.ErrorType, errorText: String) {
        showConnectionErrorDialog(this, type, ::initApp)
    }

    fun showProviderEnter() {
        val dialog = MaterialAlertDialogBuilder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_provider_enter, null)
        val spinner = dialogView.findViewById<SmartMaterialSpinner<String>>(R.id.dialog_provider_protocol)
        val editText = dialogView.findViewById<EditText>(R.id.dialog_provider_enter)
        val adapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(this, R.array.providerProtocols, android.R.layout.simple_spinner_item)
        var selectedProtocol = ""
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, itemSelected: View?, selectedItemPosition: Int, selectedId: Long) {
                val arr = resources.getStringArray(R.array.providerProtocols)
                selectedProtocol = arr[selectedItemPosition]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinner.setSelection(0)

        dialog.setTitle(getString(R.string.provider_enter_title))
        dialog.setNegativeButton(getString(R.string.exit)) { dialog, id ->
            exitProcess(0)
        }
        dialog.setPositiveButton(getString(R.string.ok), null)
        dialog.setView(dialogView)
        dialog.setCancelable(false)
        val d = dialog.create()
        d.show()

        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val enteredText = editText.text.toString()

            if (enteredText.isNotEmpty()) {
                Toast.makeText(this, getString(R.string.new_provider, selectedProtocol + enteredText), Toast.LENGTH_LONG).show()
                SettingsData.setProvider(selectedProtocol + enteredText, this, true)
                d.cancel()
                initApp()
            } else {
                Toast.makeText(this, getString(R.string.empty_provider), Toast.LENGTH_SHORT).show()
            }
        }
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
                fragmentTo = NewestFilmsFragment()
            }
            NavigationMenuTabs.nav_menu_categories -> {
                fragmentTo = CategoriesFragment()
            }
            NavigationMenuTabs.nav_menu_search -> {
                fragmentTo = SearchFragment()
            }
            NavigationMenuTabs.nav_menu_bookmarks -> {
                fragmentTo = BookmarksFragment()
            }
            NavigationMenuTabs.nav_menu_later -> {
                fragmentTo = WatchLaterFragment()
            }
            NavigationMenuTabs.nav_menu_settings -> {
                fragmentTo = UserFragment()
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

    override fun onDestroy() {
        PlayerJsInterface.notifyanager?.cancel(0)
        super.onDestroy()
    }
}
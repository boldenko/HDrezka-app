package com.falcofemoralis.hdrezkaapp.views

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.algolia.instantsearch.voice.VoiceSpeechRecognizer
import com.algolia.instantsearch.voice.ui.Voice
import com.algolia.instantsearch.voice.ui.Voice.shouldExplainPermission
import com.algolia.instantsearch.voice.ui.Voice.showPermissionRationale
import com.algolia.instantsearch.voice.ui.VoicePermissionDialogFragment
import com.chivorn.smartmaterialspinner.SmartMaterialSpinner
import com.falcofemoralis.hdrezkaapp.BuildConfig
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.clients.PlayerJsInterface
import com.falcofemoralis.hdrezkaapp.constants.DeviceType
import com.falcofemoralis.hdrezkaapp.constants.NavigationMenuTabs
import com.falcofemoralis.hdrezkaapp.constants.UpdateItem
import com.falcofemoralis.hdrezkaapp.controllers.SocketFactory
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.interfaces.IPagerView
import com.falcofemoralis.hdrezkaapp.interfaces.NavigationMenuCallback
import com.falcofemoralis.hdrezkaapp.interfaces.OnFragmentInteractionListener
import com.falcofemoralis.hdrezkaapp.interfaces.OnFragmentInteractionListener.Action
import com.falcofemoralis.hdrezkaapp.models.NewestFilmsModel
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SeriesUpdateItem
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import com.falcofemoralis.hdrezkaapp.objects.UserData
import com.falcofemoralis.hdrezkaapp.utils.ConnectionManager.isInternetAvailable
import com.falcofemoralis.hdrezkaapp.utils.ConnectionManager.showConnectionErrorDialog
import com.falcofemoralis.hdrezkaapp.utils.DialogManager
import com.falcofemoralis.hdrezkaapp.utils.FragmentOpener
import com.falcofemoralis.hdrezkaapp.views.fragments.*
import com.falcofemoralis.hdrezkaapp.views.tv.NavigationMenu
import com.falcofemoralis.hdrezkaapp.views.tv.interfaces.FragmentChangeListener
import com.falcofemoralis.hdrezkaapp.views.tv.interfaces.NavigationStateListener
import com.google.firebase.FirebaseApp
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import ru.nikartm.support.ImageBadgeView
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URI
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), OnFragmentInteractionListener, IConnection, IPagerView, NavigationStateListener, FragmentChangeListener,
    NavigationMenuCallback, VoiceSpeechRecognizer.ResultsListener {
    private var isSettingsOpened: Boolean = false
    private var mainFragment: Fragment? = null
    private lateinit var currentFragment: Fragment
    private var savedInstanceState: Bundle? = null
    private lateinit var interfaceMode: Number

    /* TV */
    private lateinit var navMenuFragment: NavigationMenu
    private lateinit var navFragmentLayout: FrameLayout
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.savedInstanceState = savedInstanceState

        FirebaseApp.initializeApp(this);

        initApp()

        checkAppVersion()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun initApp() {
        if (isInternetAvailable(applicationContext)) {
            if (savedInstanceState == null) {
                SettingsData.initProvider(this)
                try {
                    setDefaultSSLSocketFactory(SocketFactory())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (SettingsData.provider == null || SettingsData.provider == "") {
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

                            SettingsData.mainScreen?.let {
                                mainFragment = when (it) {
                                    0 -> NewestFilmsFragment()
                                    1 -> CategoriesFragment()
                                    2 -> SearchFragment()
                                    3 -> BookmarksFragment()
                                    4 -> WatchLaterFragment()
                                    else -> NewestFilmsFragment()
                                }
                            }
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
                            createNotifyBtn()
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
            val f = if (mainFragment?.isVisible == true) mainFragment
            else currentFragment
            onFragmentInteraction(f, SettingsFragment(), Action.NEXT_FRAGMENT_HIDE, true, null, null, null)
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

    override fun onFragmentInteraction(fragmentSource: Fragment?, fragmentReceiver: Fragment?, action: Action, isBackStack: Boolean, backStackTag: String?, data: Bundle?, callback: (() -> Unit)?) {
        val fTrans: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentReceiver?.arguments = data

        val animIn = R.anim.fade_in
        val animOut = R.anim.fade_out
        fTrans.setCustomAnimations(animIn, animOut, animIn, animOut)

        when (action) {
            Action.NEXT_FRAGMENT_HIDE -> {
                if (mainFragment?.isVisible == true) fTrans.hide(mainFragment!!)
                else fragmentSource?.let { fTrans.hide(it) }

                val frags: List<Fragment> = supportFragmentManager.fragments
                var f: Fragment? = null
                for (fragment in frags) {
                    if (fragment == fragmentReceiver) {
                        f = fragment
                        break
                    }
                }
                if (fragmentReceiver != null) {
                    currentFragment = fragmentReceiver
                }

                if (f == null) {
                    if (fragmentReceiver != null) {
                        fTrans.add(R.id.activity_main_fcv_container, fragmentReceiver)
                    }
                } else {
                    if (fragmentReceiver != null) {
                        fTrans.show(fragmentReceiver)
                    }
                }

                if (isBackStack) {
                    fTrans.addToBackStack(backStackTag)
                }
                fTrans.commit()
            }
            Action.NEXT_FRAGMENT_REPLACE -> {
                if (fragmentReceiver != null) {
                    fTrans.replace(R.id.activity_main_fcv_container, fragmentReceiver)
                }
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
        try {
            showConnectionErrorDialog(this, type, ::initApp)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showProviderEnter() {
        val dialog = DialogManager.getDialog(this, R.string.provider_enter_title)
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

        dialog.setNegativeButton(getString(R.string.exit)) { dialog, id ->
            exitProcess(0)
        }
        dialog.setPositiveButton(getString(R.string.ok), null)
        dialog.setView(dialogView)
        dialog.setCancelable(false)
        val d = dialog.create()
        d.show()

        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val enteredText = editText.text.toString().replace(" ", "").replace("\n", "")

            if (enteredText.isNotEmpty()) {
                val link = selectedProtocol + enteredText
                Toast.makeText(this, getString(R.string.new_provider, link), Toast.LENGTH_LONG).show()
                SettingsData.setProvider(link, this, true)
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

        if (SettingsData.deviceType == DeviceType.TV) {
            val acceptableFragment = when (mainFragment) {
                is NewestFilmsFragment -> true
                is CategoriesFragment -> true
                is SearchFragment -> true
                is BookmarksFragment -> true
                is WatchLaterFragment -> true
                is SettingsFragment -> true
                else -> false
            }

            if (acceptableFragment && mainFragment?.isVisible == true) {
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed()
                    return
                }

                this.doubleBackToExitPressedOnce = true
                Toast.makeText(this, getString(R.string.double_click_hint), Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
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
                fragmentTo = SettingsFragment()
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

    // This callback is invoked when the Speech Recognizer returns.
    // This is where you process the intent and extract the speech text from the intent.
    override fun onResults(possibleTexts: Array<out String>) {
/*        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).let { results ->
                    results?.get(0)
                }

        }
        super.onActivityResult(requestCode, resultCode, data)*/

        val spokenText = possibleTexts.firstOrNull() //?.capitalize()
        if (mainFragment is SearchFragment) {
            (mainFragment as SearchFragment).showVoiceResult(spokenText)
        } else if (mainFragment is ViewPagerFragment) {
            (mainFragment as ViewPagerFragment).showVoiceCommand(spokenText)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (Voice.isRecordPermissionWithResults(requestCode, grantResults)) {
            when {
                Voice.isPermissionGranted(grantResults) -> {
                    if (mainFragment is SearchFragment) {
                        (mainFragment as SearchFragment).showVoiceDialog()
                    } else if (mainFragment is ViewPagerFragment) {
                        (mainFragment as ViewPagerFragment).setVoiceCommand()
                    }
                }
                shouldExplainPermission() -> showPermissionRationale(getPermissionView())
                else -> Voice.showPermissionManualInstructions(getPermissionView())
            }
        }
    }

    private fun getPermissionView(): View = getPermissionDialog()!!.requireView().findViewById(R.id.positive)
    private fun getPermissionDialog() = supportFragmentManager.findFragmentByTag(SearchFragment.Tag.getTag().name) as? VoicePermissionDialogFragment

    private fun checkAppVersion() {
        if (SettingsData.isCheckNewVersion == true) {
            val _context = this
            //val versionFilePath = filesDir.path + "/version"

            GlobalScope.launch {
                try {
                    val uri: URI = URI.create("https://dl.dropboxusercontent.com/s/yhvwhwdzmiiqu6x/version.json?dl=1")
                    uri.toURL().openStream().use { inputStream ->
                        // InputStream -> String
                        val versionString = convertInputStreamToString(inputStream)
                        try {
                            if (versionString != null) {
                                val versionObject = JSONObject(versionString)
                                val versionCode = versionObject.getInt("code")
                                val serverVersion = versionObject.getString("version")
                                val newFeatures = versionObject.getString("newFeatures")

                                val isNewVersion = compareAppVersion(versionCode)

                                withContext(Dispatchers.Main) {
                                    if (isNewVersion) {
                                        // show dialog
                                        val builder = DialogManager.getDialog(_context, R.string.new_version_hint)
                                        builder.setPositiveButton(R.string.ok_text) { d, i ->
                                            d.dismiss()
                                        }
                                        /*       builder.setNegativeButton(R.string.cancel) { d, i ->
                                         fun downloadApk() {
                                             val apkUrl = "https://androidwave.com/source/apk/app-pagination-recyclerview.apk"
                                             val downloadController = DownloadController(_context, apkUrl)
                                             downloadController.enqueueDownload()
                                         }

                                         val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                                             if (isGranted) {
                                                 // Permission is granted. Continue the action or workflow in your
                                                 downloadApk()
                                             } else {
                                                 Toast.makeText(_context, getString(R.string.perm_write_hint), Toast.LENGTH_LONG).show()
                                             }
                                         }

                                         when (PackageManager.PERMISSION_GRANTED) {
                                             ContextCompat.checkSelfPermission(_context, Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                                                 downloadApk()
                                             }
                                             else -> {
                                                 // You can directly ask for the permission.
                                                 // The registered ActivityResultCallback gets the result of this request.
                                                 requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                             }
                                         }
                                     }*/
                                        builder.setMessage("${BuildConfig.VERSION_NAME} -> $serverVersion\n\n $newFeatures")
                                        val d = builder.create()
                                        d.show()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun compareAppVersion(version: Int?): Boolean {
        if (version == null) {
            throw Exception("File not found")
        }

        return BuildConfig.VERSION_CODE < version
    }

    private fun convertInputStreamToString(inputStream: InputStream): String? {
        val result = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var length: Int
        while (inputStream.read(buffer).also { length = it } != -1) {
            result.write(buffer, 0, length)
        }

        // Java 1.1
        return result.toString(StandardCharsets.UTF_8.name())

        // Java 10
        // return result.toString(StandardCharsets.UTF_8);
    }

    fun createNotifyBtn() {
        val _context = this
        val notifyBtn = findViewById<ImageBadgeView>(R.id.activity_main_iv_notify_btn)
        var d: AlertDialog? = null
        val builder = DialogManager.getDialog(this, R.string.series_update_hot)
        val dialogView = layoutInflater.inflate(R.layout.dialog_series_updates, null) as LinearLayout
        var seriesUpdates: LinkedHashMap<String, ArrayList<SeriesUpdateItem>>? = null
        val userUpdates: LinkedHashMap<String, ArrayList<SeriesUpdateItem>> = LinkedHashMap()
        val hintView = dialogView.findViewById<TextView>(R.id.series_updates_hint)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.series_updates_progress)

        if (UserData.isLoggedIn == true) {
            hintView.visibility = View.GONE

            GlobalScope.launch {
                seriesUpdates = NewestFilmsModel.getSeriesUpdates()

                if (seriesUpdates != null && seriesUpdates!!.size > 0) {
                    for ((date, list) in seriesUpdates!!) {
                        val userList: ArrayList<SeriesUpdateItem> = ArrayList()

                        for (item in list) {
                            if (item.isUserWatch) {
                                userList.add(item)
                            }
                        }

                        if (userList.size > 0) {
                            userUpdates[date] = userList
                        }
                    }

                    // set dialog
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE

                        val container = dialogView.findViewById<LinearLayout>(R.id.series_updates_container)

                        if (userUpdates.size > 0) {
                            var badgeCount = 0

                            for ((date, list) in userUpdates) {
                                if (list.size > 0) {
                                    val dateView = layoutInflater.inflate(R.layout.inflate_series_updates_date, null) as LinearLayout
                                    dateView.findViewById<TextView>(R.id.date).text = date
                                    container.addView(dateView)
                                }

                                for (item in list) {
                                    badgeCount++

                                    val itemView = layoutInflater.inflate(R.layout.inflate_series_updates_item, null) as LinearLayout
                                    itemView.findViewById<TextView>(R.id.inflate_series_updates_item_title).text = item.title
                                    itemView.findViewById<TextView>(R.id.inflate_series_updates_item_season).text = item.season
                                    itemView.findViewById<TextView>(R.id.inflate_series_updates_item_episode).text = item.episode
                                    if (!item.voice.isNullOrEmpty()) {
                                        itemView.findViewById<TextView>(R.id.inflate_series_updates_item_voice).text = item.voice
                                    }

                                    val film = Film(SettingsData.provider + item.filmLink)
                                    itemView.setOnClickListener {
                                        if (!film.filmLink.isNullOrEmpty()) {
                                            d?.dismiss()

                                            val f = if (mainFragment?.isVisible == true) mainFragment
                                            else currentFragment
                                            if (f != null) {
                                                FragmentOpener.openWithData(f, _context, film, "film")
                                            }
                                        }
                                    }

                                    container.addView(itemView)
                                }
                            }

                            if (badgeCount > 0) {
                                notifyBtn.badgeValue = badgeCount
                                notifyBtn.isShowCounter = true
                                notifyBtn.badgeColor = getColor(R.color.main_color_3)
                            }
                        } else {
                            hintView.text = getString(R.string.no_updates)
                        }
                    }
                }
            }
        } else {
            progressBar.visibility = View.GONE
            notifyBtn.isShowCounter = false
            notifyBtn.badgeColor = getColor(R.color.transparent)
        }

        builder.setView(dialogView)
        builder.setPositiveButton(R.string.ok_text) { dialog, id ->
            dialog.dismiss()
        }

        builder.setNeutralButton(R.string.watch_all) { dialog, id ->
            showAllSeriesUpdates(seriesUpdates)
        }

        d = builder.create()
        notifyBtn.setOnClickListener {
            d.show()
        }
    }

    private fun showAllSeriesUpdates(tmp: LinkedHashMap<String, ArrayList<SeriesUpdateItem>>?) {
        var seriesUpdates = tmp
        val builder = DialogManager.getDialog(this, R.string.series_update_hot)
        var dialog: AlertDialog? = null
        val dialogView = layoutInflater.inflate(R.layout.dialog_series_updates, null) as LinearLayout
        val container = dialogView.findViewById<LinearLayout>(R.id.series_updates_container)
        dialogView.findViewById<TextView>(R.id.series_updates_hint).visibility = View.GONE

        fun updateDialog() {
            dialogView.findViewById<ProgressBar>(R.id.series_updates_progress).visibility = View.GONE

            if (seriesUpdates != null) {
                for ((date, updateItems) in seriesUpdates!!) {
                    // костыль т.к .collapsed() не срабатывает
                    val layout: LinearLayout = layoutInflater.inflate(R.layout.inflate_series_updates_layout, null) as LinearLayout
                    val expandedList: LinearLayout = layout.findViewById(R.id.inflate_series_updates_layout_list)

                    if (!date.contains("Сегодня")) {
                        expandedList.visibility = View.GONE
                    }

                    layout.findViewById<TextView>(R.id.inflate_series_updates_layout_header).text = date
                    layout.findViewById<LinearLayout>(R.id.inflate_series_updates_button).setOnClickListener {
                        if (expandedList.isVisible) {
                            expandedList.visibility = View.GONE
                        } else {
                            expandedList.visibility = View.VISIBLE
                        }
                    }

                    var lastColor = R.color.light_background
                    for (item in updateItems) {
                        if (item.title.isEmpty()) {
                            continue
                        }

                        val itemView = layoutInflater.inflate(R.layout.inflate_series_updates_item, null) as LinearLayout
                        itemView.findViewById<TextView>(R.id.inflate_series_updates_item_title).text = item.title
                        itemView.findViewById<TextView>(R.id.inflate_series_updates_item_season).text = item.season
                        itemView.findViewById<TextView>(R.id.inflate_series_updates_item_episode).text = item.episode
                        if (!item.voice.isNullOrEmpty()) {
                            itemView.findViewById<TextView>(R.id.inflate_series_updates_item_voice).text = item.voice
                        }

                        val film = Film(SettingsData.provider + item.filmLink)
                        itemView.setOnClickListener {
                            if (!film.filmLink.isNullOrEmpty()) {
                                dialog?.dismiss()

                                val f = if (mainFragment?.isVisible == true) mainFragment
                                else currentFragment
                                if (f != null) {
                                    FragmentOpener.openWithData(f, this, film, "film")
                                }
                            }
                        }

                        if (item.isUserWatch) {
                            itemView.setBackgroundColor(ContextCompat.getColor(this, R.color.main_color_3))
                        } else {
                            lastColor = if (lastColor == R.color.dark_background) R.color.light_background else R.color.dark_background
                            itemView.setBackgroundColor(ContextCompat.getColor(this, lastColor))
                        }
                        expandedList.addView(itemView)
                    }
                    container.addView(layout)
                }
            }
        }

        if (seriesUpdates == null) {
            GlobalScope.launch {
                seriesUpdates = NewestFilmsModel.getSeriesUpdates()

                withContext(Dispatchers.Main) {
                    updateDialog()
                }
            }
        } else {
            GlobalScope.launch {
                Thread.sleep(50)

                withContext(Dispatchers.Main) {
                    updateDialog()
                }
            }
        }

        builder.setView(dialogView)
        builder.setPositiveButton(getString(R.string.ok_text)) { d, id ->
            d.cancel()
        }

        dialog = builder.create()
        dialog.show()
    }
}
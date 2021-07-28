package com.falcofemoralis.hdrezkaapp.views.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import androidx.fragment.app.Fragment
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.clients.AuthWebViewClient
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.interfaces.OnFragmentInteractionListener
import com.falcofemoralis.hdrezkaapp.objects.UserData
import com.falcofemoralis.hdrezkaapp.presenters.UserPresenter
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper
import com.falcofemoralis.hdrezkaapp.views.MainActivity
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.UserView
import com.squareup.picasso.Picasso

class UserFragment : Fragment(), UserView {
    private lateinit var currentView: View
    private lateinit var userPresenter: UserPresenter
    private lateinit var popupWindowView: RelativeLayout
    private lateinit var popupWindow: PopupWindow
    private lateinit var webView: WebView
    private lateinit var popupWindowLoadingBar: ProgressBar
    private var popupWindowCloseBtn: Button? = null
    private var isLoaded = false
    private lateinit var authPanel: LinearLayout
    private lateinit var exitPanel: TextView
    private lateinit var fragmentListener: OnFragmentInteractionListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentListener = context as OnFragmentInteractionListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        currentView = inflater.inflate(R.layout.fragment_user, container, false)
        userPresenter = UserPresenter(this, requireContext())

        authPanel = currentView.findViewById(R.id.fragment_user_ll_auth_panel)
        exitPanel = currentView.findViewById(R.id.fragment_user_tv_exit)

        initExitButton()

        UserData.isLoggedIn?.let {
            setAuthPanel(it)
        }

        currentView.findViewById<TextView>(R.id.fragment_user_tv_settings).setOnClickListener {
            fragmentListener.onFragmentInteraction(this, SettingsFragment(), OnFragmentInteractionListener.Action.NEXT_FRAGMENT_HIDE, true, null, null, null)
        }

        return currentView
    }

    private fun setAuthPanel(isLogged: Boolean) {
        if (isLogged) {
            popupWindowCloseBtn?.visibility = View.GONE
            authPanel.visibility = View.GONE
            exitPanel.visibility = View.VISIBLE
        } else {
            authPanel.visibility = View.VISIBLE
            exitPanel.visibility = View.GONE
            createAuthDialog()
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun createAuthDialog() {
        activity?.let {
            popupWindowView = requireActivity().layoutInflater.inflate(R.layout.dialog_auth, null) as RelativeLayout

            popupWindow = PopupWindow(popupWindowView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true)
            popupWindowLoadingBar = popupWindowView.findViewById(R.id.dialog_auth_pb_loading)
            popupWindowCloseBtn = popupWindowView.findViewById(R.id.dialog_auth_bt_close)
            webView = popupWindowView.findViewById(R.id.dialog_auth_wv_auth)
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true

            popupWindowView.setOnTouchListener { _, _ -> //Close the window when clicked outside
                popupWindow.dismiss()
                true
            }
            popupWindowView.findViewById<Button>(R.id.dialog_auth_bt_close).setOnClickListener {
                popupWindow.dismiss()
            }

            currentView.findViewById<TextView>(R.id.fragment_user_tv_login).setOnClickListener {
                // login
                userPresenter.setAuthWindow(UserPresenter.WindowType.LOGIN)
            }

            currentView.findViewById<TextView>(R.id.fragment_user_tv_register).setOnClickListener {
                //register
                userPresenter.setAuthWindow(UserPresenter.WindowType.REGISTRATION)
            }
        }
    }

    override fun showAuthWindow(type: UserPresenter.WindowType, link: String) {
        webView.visibility = View.GONE
        popupWindowCloseBtn?.visibility = View.GONE
        popupWindowLoadingBar.visibility = View.VISIBLE

        webView.webViewClient = AuthWebViewClient(type, this, ::authCallback)
        popupWindow.showAtLocation(popupWindowView, Gravity.CENTER, 0, 0)
        if (!isLoaded) {
            webView.loadUrl(link)
            isLoaded = true
        }
    }

    private fun authCallback(isLogged: Boolean) {
        webView.stopLoading()
        isLoaded = false

        if (isLogged) {
            webView.visibility = View.GONE

            setAuthPanel(isLogged)
            userPresenter.enter()
            userPresenter.getUserAvatar()

            activity?.let {
                (it as MainActivity).updatePager()
            }
            popupWindow.dismiss()
        } else {
            popupWindowLoadingBar.visibility = View.GONE
            popupWindowCloseBtn?.visibility = View.VISIBLE
            webView.visibility = View.VISIBLE
        }
    }

    private fun initExitButton() {
        exitPanel.setOnClickListener {
            setAuthPanel(false)
            userPresenter.exit()

            activity?.let { it1 ->
                (it1 as MainActivity).updatePager()
            }

        }
    }

    override fun setUserAvatar(link: String?) {
        if(link != null){
            Picasso.get().load(link).into(requireActivity().findViewById<ImageView>(R.id.activity_main_iv_user))
        }
    }

    override fun showConnectionError(type: IConnection.ErrorType) {
        ExceptionHelper.showToastError(requireContext(), type)
    }
}
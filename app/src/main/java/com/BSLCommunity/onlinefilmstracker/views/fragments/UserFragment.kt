package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.clients.AuthWebViewClient
import com.BSLCommunity.onlinefilmstracker.models.UserModel

class UserFragment : Fragment() {

    private lateinit var currentView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        currentView = inflater.inflate(R.layout.fragment_user, container, false)

        currentView.findViewById<TextView>(R.id.fragment_user_tv_exit).setOnClickListener {
            activity?.let {
                UserModel.saveLoggedIn(false, it)
                setAuthPanel(false)
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
            }
        }

        UserModel.isLoggedIn?.let { setAuthPanel(it) }
        createAuthDialog()

        return currentView
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun createAuthDialog() {
        var isLoaded = false

        activity?.let {
            val view: LinearLayout = requireActivity().layoutInflater.inflate(R.layout.dialog_auth, null) as LinearLayout
            val popupWindow = PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true)
            view.setOnTouchListener { _, _ -> //Close the window when clicked
                popupWindow.dismiss()
                true
            }

            val webView: WebView = view.findViewById(R.id.dialog_auth_wv_auth)
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.webViewClient = AuthWebViewClient { isLogged ->
                if (isLogged) {
                    UserModel.saveLoggedIn(isLogged, it)
                    setAuthPanel(isLogged)
                    webView.visibility = View.GONE
                } else {
                    view.findViewById<ProgressBar>(R.id.dialog_auth_pb_loading).visibility = View.GONE
                    webView.visibility = View.VISIBLE
                }
            }

            currentView.findViewById<TextView>(R.id.fragment_user_tv_login).setOnClickListener {
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
                if (!isLoaded) {
                    webView.loadUrl("http://hdrezka.tv/favorites/")
                    isLoaded = true
                }
            }
        }
    }

    private fun setAuthPanel(isLogged: Boolean) {
        if (isLogged) {
            currentView.findViewById<LinearLayout>(R.id.fragment_user_ll_auth_panel).visibility = View.GONE
            currentView.findViewById<TextView>(R.id.fragment_user_tv_exit).visibility = View.VISIBLE
        } else {
            currentView.findViewById<LinearLayout>(R.id.fragment_user_ll_auth_panel).visibility = View.VISIBLE
            currentView.findViewById<TextView>(R.id.fragment_user_tv_exit).visibility = View.GONE
        }
    }
}
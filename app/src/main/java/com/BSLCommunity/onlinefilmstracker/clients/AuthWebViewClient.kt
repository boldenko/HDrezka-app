package com.BSLCommunity.onlinefilmstracker.clients

import android.annotation.TargetApi
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class AuthWebViewClient(val callback: (isLogged: Boolean) -> Unit) : WebViewClient() {

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        if (!checkUrl(request.url.toString())) {
            view.loadUrl(request.url.toString())
        }
        return true
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        if (!checkUrl(url)) {
            view.loadUrl(url)
        }
        return true
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        view?.evaluateJavascript(
            "javascript:" +
                    "document.querySelector('meta[name=\"viewport\"]').setAttribute('content', 'width=device-width,initial-scale=1.0');" +
                    "const loginPopup = document.getElementById('login-popup');" +
                    "const parent = document.getElementsByTagName('body')[0];" +
                    "parent.prepend(loginPopup);" +
                    "loginPopup.style.display = 'block';" +
                    "document.getElementById('wrapper').style.display='none';" +
                    "document.getElementById('footer').style.display = 'none';" +
                    "loginPopup.classList.remove('b-popup__fixed');" +
                    "const body = document.body;" +
                    "body.style.height = 'unset';" +
                    "body.style.minWidth = 'unset';" +
                    "loginPopup.style.width = '90%';", null
        )

        callback(false)

        super.onPageFinished(view, url)
    }

    private fun checkUrl(url: String): Boolean {
        return if (url == "http://hdrezka.tv/favorites/") {
            callback(true)
            false
        } else {
            true
        }
    }
}
package com.BSLCommunity.onlinefilmstracker.clients

import android.annotation.TargetApi
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.BSLCommunity.onlinefilmstracker.interfaces.IConnection
import com.BSLCommunity.onlinefilmstracker.objects.SettingsData
import com.BSLCommunity.onlinefilmstracker.presenters.UserPresenter

class AuthWebViewClient(val type: UserPresenter.WindowType, val mainView: IConnection, val callback: (isLogged: Boolean) -> Unit) : WebViewClient() {
    private val LOGIN_CLASS = "login-popup"
    private val REGISTER_CLASS = "register-popup"

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

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        if (error?.errorCode == ERROR_TIMEOUT) {
            mainView.showConnectionError(IConnection.ErrorType.TIMEOUT)
        }
        super.onReceivedError(view, request, error)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        var additionalScript = ""
        val window: String = when (type) {
            UserPresenter.WindowType.LOGIN -> LOGIN_CLASS
            UserPresenter.WindowType.REGISTRATION -> {
                additionalScript += "\$('.b-tophead__register').trigger('click');"
                REGISTER_CLASS
            }
        }

        view?.evaluateJavascript(
            "javascript: " +
                    additionalScript +
                    "document.querySelector('meta[name=\"viewport\"]').setAttribute('content', 'width=device-width,initial-scale=1.0');" +
                    "" + // move window at top
                    "var windowPopup = document.getElementById('$window');" +
                    "document.body.insertAdjacentElement('afterbegin', windowPopup);" +
                    "" + // fix window position
                    "windowPopup.style.display = 'block';" +
                    "windowPopup.style.marginTop = 'none';" +
                    "windowPopup.classList.remove('b-popup__fixed');" +
                    "windowPopup.style.width = '90%';" +
                    "windowPopup.style.marginLeft = 'unset';" +
                    "windowPopup.style.left = 'unset';" +
                    "" + // hide main body
                    "document.getElementById('wrapper').style.display = 'none';" +
                    "document.getElementById('footer').style.display = 'none';" +
                    "document.body.style.height = 'unset';" +
                    "document.body.style.minWidth = 'unset';" +
                    "" + // remove different elements
                    "document.getElementsByClassName('b-popup__close')[0].style.display = 'none';" +
                    "document.getElementsByClassName('b-login__popup_join')[0].style.display = 'none';" +
                    "" + // remove advertisements
                    "document.body.classList.remove('active-brand');" +
                    "var advertbg = document.getElementsByTagName('noindex')[0];" +
                    "if(advertbg) {advertbg.style.display = 'none';}" +
                    "function setPos(){" +
                    "   var isChanged = false;" +
                    "   for (var i = 0; i < document.body.childNodes.length; i++) {" +
                    "       var node = document.body.childNodes[i];" +
                    "       if(node.childNodes.length > 0 && node.style.position == 'fixed'){" +
                    "           node.style.display = 'none'; " +
                    "           document.getElementById('overlay').style.display = 'none';" +
                    // fix form inputs
                    "           var inputs = document.getElementsByClassName('b-field');" +
                    "           if('$window' == '$REGISTER_CLASS'){" +
                    "             for(var i = 0; i<inputs.length; i++) { inputs[i].style.width = '95%'; }" +
                    "             document.getElementsByClassName('btn-action')[0].style.width = '100%';}" +
                    "           isChanged = true; " +
                    "           return;}" +
                    "   } " +
                    "   if(!isChanged){" +
                    "       setTimeout(setPos, 1000);}" +
                    "} " +
                    "setTimeout(setPos, 1000);", null
        )

        callback(false)

        super.onPageFinished(view, url)
    }


    private fun checkUrl(url: String): Boolean {
        return if (url == SettingsData.provider + "/" || url == "https://rezka.ag/" || (url.contains("__q_hash")) && url.contains("oauth.vk.com")) {
            callback(true)
            true
        } else {
            false
        }
    }
}
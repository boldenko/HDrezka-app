package com.BSLCommunity.onlinefilmstracker.clients

import android.annotation.TargetApi
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.BSLCommunity.onlinefilmstracker.objects.SettingsData
import com.BSLCommunity.onlinefilmstracker.presenters.UserPresenter

class AuthWebViewClient(val type: UserPresenter.WindowType, val callback: (isLogged: Boolean) -> Unit) : WebViewClient() {

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
        var additionalScript = ""
        val window: String = when (type) {
            UserPresenter.WindowType.LOGIN -> "login-popup"
            UserPresenter.WindowType.REGISTRATION -> {
                additionalScript += "\$('.b-tophead__register').trigger('click');"
                "register-popup"
            }
        }

        view?.evaluateJavascript(
            "javascript:" +
                    additionalScript +
                    "document.querySelector('meta[name=\"viewport\"]').setAttribute('content', 'width=device-width,initial-scale=1.0');" +
                    "const windowPopup = document.getElementById('$window');" +
                    "const parent = document.getElementsByTagName('body')[0];" +
                    "parent.prepend(windowPopup);" +
                    "windowPopup.style.display = 'block';" +
                    "windowPopup.style.marginTop = 'none';" +
                    "document.getElementById('wrapper').style.display='none';" +
                    "document.getElementById('footer').style.display = 'none';" +
                    "windowPopup.classList.remove('b-popup__fixed');" +
                    "const body = document.body;" +
                    "body.style.height = 'unset';" +
                    "body.style.minWidth = 'unset';" +
                    "windowPopup.style.width = '90%';" +
                    "document.getElementsByClassName('b-popup__close')[0].style.display = 'none';" +
                    "document.getElementsByClassName('b-login__popup_join')[0].style.display = 'none';" +
                    "document.getElementById('overlay').style.display = 'none';" +
                    // "body.classList.remove('active-brand');" +
                    "function setPos(){" +
                    "let isChanged = false;" +
                    "for (let i = 0; i < body.childNodes.length; ++i) {" +
                    "const node = body.childNodes[i];" +
                    "if(node.childNodes.length > 0 && node.style.position == 'fixed'){node.style.display = 'none'; isChanged = true; return;}" +
                    "} " +
                    "if(!isChanged){setTimeout(setPos, 1000);}" +
                    "} " +
                    "setTimeout(setPos, 1000);", null
        )

        callback(false)

        super.onPageFinished(view, url)
    }


    private fun checkUrl(url: String): Boolean {
        return if (url == SettingsData.provider + "/" || url == "https://rezka.ag/") {
            callback(true)
            true
        } else {
            false
        }
    }
}
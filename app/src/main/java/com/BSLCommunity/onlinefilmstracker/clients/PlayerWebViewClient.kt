package com.BSLCommunity.onlinefilmstracker.clients

import android.annotation.TargetApi
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class PlayerWebViewClient(val callback: () -> Unit) : WebViewClient() {

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        view.loadUrl(request.url.toString())
        return true
    }

    // Для старых устройств
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        view.loadUrl(url)
        return true
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        // Move player at top
        view?.evaluateJavascript(
            "javascript:" +
                    "document.querySelector('meta[name=\"viewport\"]').setAttribute('content', 'width=device-width,initial-scale=1.0');" +
                    "const player = document.getElementById('player'); " +
                    "const translators = document.getElementsByClassName('b-translators__block')[0]; " +
                    "const parent = document.getElementsByTagName('body')[0]; " +
                    "parent.prepend(player);" +
                    "if(translators){" +
                    "parent.prepend(translators);" +
                    "}", null
        )

        // Hide different elements
        view?.evaluateJavascript(
            "javascript:(function() {" +
                    "parent.style.minWidth = 'unset';" +
                    "const translatorItems = document.getElementsByClassName('b-translator__item');" +
                    "Array.from(translatorItems).forEach(translatorItem => translatorItem.style.minWidth = '40%');" +
                    "document.getElementById('wrapper').style.display='none';" +
                    "document.getElementById('footer').style.display='none';" +
                    "document.getElementsByClassName('b-post__support_holder')[0].style.display='none';" +
                    "document.getElementById('cdnplayer-container').style.width='100%';" +
                    "document.getElementById('cdnplayer').style.width='100%';" +
                    "const playIcon = document.getElementById('oframecdnplayer').childNodes[18];" +
                    "playIcon.style.left = '50%';" +
                    "const mo = new MutationObserver(() => {" +
                    "playIcon.style.left = '50%';" +
                    "});" +
                    "mo.observe(playIcon, { attributes: true, attributeFilter: ['style'] });" +
                    "const mo2 = new MutationObserver((mutationsList, observer) => {" +
                    "for(let mutation of mutationsList) {" +
                    "if (mutation.type === 'childList') {" +
                    "const voices = document.getElementsByClassName('tooltipster-base')[0];" +
                    "if(voices){" +
                    "voices.style.left = 'unset';" +
                    "voices.style.minWidth = 'unset';}}" +
                    "}});" +
                    "mo2.observe(parent, {childList: true, subtree: true});" +
                    "})()", null
        )

        view?.evaluateJavascript("javascript: document.body.style.height = 'unset';", null)

        callback()

        super.onPageFinished(view, url)
    }
}
package com.falcofemoralis.hdrezkaapp.clients

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection

class PlayerWebViewClient(val context: Context, val mainView: IConnection, val callback: () -> Unit) : WebViewClient() {

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        if (!checkUrl(request.url.toString())) {
            view.loadUrl(request.url.toString())
        }
        return true
    }

    // Для старых устройств
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
        // Move player at top
        view?.evaluateJavascript(
            "javascript:" +
                    "document.querySelector('meta[name=\"viewport\"]').setAttribute('content', 'width=device-width,initial-scale=1.0');" +
                    "document.body.insertAdjacentElement('afterbegin', document.getElementById('player'));" +
                    "var translators = document.getElementsByClassName('b-translators__block')[0];" +
                    "if(translators){ document.body.insertAdjacentElement('afterbegin', translators);}", null
        )

        view?.evaluateJavascript(
            "javascript: (function() {" +
                    "document.body.style.minWidth = 'unset';" +
                    "" + // fix width translations
                    "const translatorArray = document.getElementsByClassName('b-translator__item');" +
                    "for (var i = 0; i < translatorArray.length; i++) {" +
                    "    translatorArray[i].style.minWidth = '40%';" +
                    "}" +
                    "" +  // hide main body
                    "document.getElementById('wrapper').style.display='none';" +
                    "document.getElementById('footer').style.display='none';" +
                    "" + // fix elements sizes
                    "document.getElementsByClassName('b-post__support_holder')[0].style.display='none';" +
                    "document.getElementById('cdnplayer-container').style.width='100%';" +
                    "document.getElementById('cdnplayer').style.width='100%';" +
                    "" + //  fix play button position
                    "var playIcon = document.getElementById('oframecdnplayer').childNodes[18];" +
                    "playIcon.style.left = '50%';" +
                    "var mo = new MutationObserver(function(){" +
                    "   playIcon.style.left = '50%';" +
                    "});" +
                    "mo.observe(playIcon, { attributes: true, attributeFilter: ['style'] });" +
                    "" + // fix voices
                    "var mo2 = new MutationObserver(function(mutationsList, observer) {" +
                    "   for(var i = 0; i<mutationsList.length; i++) {" +
                    "       if (mutation[i].type == 'childList') {" +
                    "           var voices = document.getElementsByClassName('tooltipster-base')[0];" +
                    "           if(voices){" +
                    "               voices.style.left = 'unset';" +
                    "               voices.style.minWidth = 'unset';" +
                    "       }}" +
                    "}});" +
                    "mo2.observe(document.body, {childList: true, subtree: true});" +
                    "" + // fix load spinner position
                    "var loadIcon = document.getElementById('oframecdnplayer').childNodes[13];" +
                    "loadIcon.style.left = '50%';" +
                    "var mo3 = new MutationObserver(function() {" +
                    "   loadIcon.style.left = '50%';" +
                    "});" +
                    "mo3.observe(loadIcon, { attributes: true, attributeFilter: ['style'] });" +
                    "" + // remove advertisements
                    "var bannerCont = document.getElementsByClassName('banner-container')[0];" +
                    "if(bannerCont) {bannerCont.style.display = 'none'; bannerCont.parentElement.parentElement.style.height = '0px';}" +
                    "document.body.classList.remove('active-brand');" +
                    "function setPos(){" +
                    "   var isChanged = false;" +
                    "   for (var i = 0; i < document.body.childNodes.length; i++) {" +
                    "       var node = document.body.childNodes[i];" +
                    "       if(node.childNodes.length > 0 && node.style.position == 'fixed'){" +
                    "           node.style.display = 'none'; " +
                    "           isChanged = true; " +
                    "           return;}" +
                    "   } " +
                    "   if(!isChanged){" +
                    "       setTimeout(setPos, 1000);}" +
                    "} " +
                    "setTimeout(setPos, 1000);" +
                    "" + // remove tracker
                    "document.body.style.height = 'unset';" +
                    "var trackerWrapper = document.getElementsByClassName('b-post__status_wrapper')[0];" +
                    "if(trackerWrapper) {trackerWrapper.style.width = '100%';}" +
                    "var trackerDownload = document.getElementsByClassName('b-post__status__tracker__download')[0];" +
                    "if(trackerDownload) {trackerDownload.style.display = 'none';}" +
                    "" +
                    "})()", null
        )

        callback()

        super.onPageFinished(view, url)
    }

    private fun checkUrl(url: String): Boolean {
        return if (url == "https://t.me/hdrezka") {
            val linkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(linkIntent)
            true
        } else {
            false
        }
    }
}
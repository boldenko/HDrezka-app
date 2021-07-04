package com.BSLCommunity.onlinefilmstracker.objects

import android.annotation.TargetApi
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

// Kotlin
class MyWebViewClient : WebViewClient() {
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
        view?.loadUrl("javascript:let child = document.getElementById('player'); let parent = document.getElementsByTagName('body')[0]; parent.prepend(child);") //let wrapper = document.getElementById('wrapper'); wrapper.style.display='none'
        view?.loadUrl("javascript:(function() {let main = document.getElementById('main'); main.style.display='none'})()");
        super.onPageFinished(view, url)
    }
}
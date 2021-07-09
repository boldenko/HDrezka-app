package com.BSLCommunity.onlinefilmstracker.presenters

import com.BSLCommunity.onlinefilmstracker.models.WatchLaterModel
import com.BSLCommunity.onlinefilmstracker.objects.WatchLater
import com.BSLCommunity.onlinefilmstracker.viewsInterface.WatchLaterView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchLaterPresenter(private val watchLaterView: WatchLaterView) {
    fun initList() {
        GlobalScope.launch {
            val watchLaterList: ArrayList<WatchLater> = WatchLaterModel.getWatchLaterList()

            withContext(Dispatchers.Main) {
                watchLaterView.setWatchLaterList(watchLaterList)
            }
        }
    }
}
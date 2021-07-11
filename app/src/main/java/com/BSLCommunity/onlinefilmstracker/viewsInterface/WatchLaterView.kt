package com.BSLCommunity.onlinefilmstracker.viewsInterface

import com.BSLCommunity.onlinefilmstracker.objects.WatchLater
import com.BSLCommunity.onlinefilmstracker.views.interfaces.IMsg

interface WatchLaterView : IMsg {
    fun setWatchLaterList(list: ArrayList<WatchLater>)

    fun redrawWatchLaterList()

    fun setProgressBarState(state: Boolean)
}
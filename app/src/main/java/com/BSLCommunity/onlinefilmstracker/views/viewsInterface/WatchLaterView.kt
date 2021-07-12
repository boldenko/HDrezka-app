package com.BSLCommunity.onlinefilmstracker.views.viewsInterface

import com.BSLCommunity.onlinefilmstracker.objects.WatchLater
import com.BSLCommunity.onlinefilmstracker.interfaces.IMsg

interface WatchLaterView : IMsg {
    fun setWatchLaterList(list: ArrayList<WatchLater>)

    fun redrawWatchLaterList()

    fun setProgressBarState(state: Boolean)
}
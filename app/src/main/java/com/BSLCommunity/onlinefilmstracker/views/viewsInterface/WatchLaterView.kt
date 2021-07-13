package com.BSLCommunity.onlinefilmstracker.views.viewsInterface

import com.BSLCommunity.onlinefilmstracker.objects.WatchLater
import com.BSLCommunity.onlinefilmstracker.interfaces.IMsg
import com.BSLCommunity.onlinefilmstracker.interfaces.IProgressState

interface WatchLaterView : IMsg, IProgressState {
    fun setWatchLaterList(list: ArrayList<WatchLater>)

    fun redrawWatchLaterList()
}
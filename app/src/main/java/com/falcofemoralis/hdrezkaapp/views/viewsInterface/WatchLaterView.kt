package com.falcofemoralis.hdrezkaapp.views.viewsInterface

import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.objects.WatchLater
import com.falcofemoralis.hdrezkaapp.interfaces.IMsg
import com.falcofemoralis.hdrezkaapp.interfaces.IProgressState

interface WatchLaterView : IMsg, IProgressState, IConnection {
    fun setWatchLaterList(list: ArrayList<WatchLater>)

    fun redrawWatchLaterList()
}
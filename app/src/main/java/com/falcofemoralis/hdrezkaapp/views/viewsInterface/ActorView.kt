package com.falcofemoralis.hdrezkaapp.views.viewsInterface

import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.objects.Actor

interface ActorView : IConnection {
    fun setBaseInfo(actor: Actor)
}
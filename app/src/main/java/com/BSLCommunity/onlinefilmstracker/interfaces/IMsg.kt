package com.BSLCommunity.onlinefilmstracker.interfaces

interface IMsg {
    enum class MsgType {
        NOT_AUTHORIZED,
        NOTHING_FOUND,
        NOTHING_ADDED
    }
    fun showMsg(msg: MsgType)
}
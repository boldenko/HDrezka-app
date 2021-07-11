package com.BSLCommunity.onlinefilmstracker.views

interface IMsg {
    enum class MsgType {
        NOT_AUTHORIZED,
        NOTHING_FOUND,
        NOTHING_ADDED
    }
    fun showMsg(msg: MsgType)
}
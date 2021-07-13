package com.BSLCommunity.onlinefilmstracker.interfaces

interface IProgressState {
    enum class StateType {
        LOADING,
        LOADED
    }

    fun setProgressBarState(type: StateType)
}
package com.BSLCommunity.onlinefilmstracker.interfaces

interface IConnection {
    enum class ErrorType {
        NO_INTERNET,
        PARSING_ERROR,
        TIMEOUT
    }

    fun showConnectionError(type: ErrorType)
}
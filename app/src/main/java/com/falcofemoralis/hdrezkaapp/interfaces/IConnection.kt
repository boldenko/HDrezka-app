package com.falcofemoralis.hdrezkaapp.interfaces

interface IConnection {
    enum class ErrorType {
        NO_INTERNET,
        PARSING_ERROR,
        EMPTY,
        TIMEOUT,
        ERROR
    }

    fun showConnectionError(type: ErrorType)
}
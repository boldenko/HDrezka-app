package com.BSLCommunity.onlinefilmstracker.utils

import android.content.Context
import android.widget.Toast
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.interfaces.IConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import org.jsoup.parser.ParseError
import java.net.SocketTimeoutException

object ExceptionHelper {
    fun showToastError(context: Context, type: IConnection.ErrorType) {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context, when (type) {
                        IConnection.ErrorType.EMPTY -> context.getString(R.string.error_empty)
                        IConnection.ErrorType.TIMEOUT -> context.getString(R.string.error_timeout)
                        IConnection.ErrorType.PARSING_ERROR -> context.getString(R.string.error_parsing)
                        IConnection.ErrorType.NO_INTERNET -> context.getString(R.string.no_connection)
                        IConnection.ErrorType.ERROR -> context.getString(R.string.error_occured)
                    }, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun catchException(e: Exception, view: IConnection) {
        val type: IConnection.ErrorType = when (e) {
            is SocketTimeoutException -> IConnection.ErrorType.TIMEOUT
            is HttpStatusException -> {
                if (e.statusCode == 404) {
                    IConnection.ErrorType.EMPTY
                } else {
                    IConnection.ErrorType.ERROR
                }
            }
            is ParseError -> IConnection.ErrorType.PARSING_ERROR
            else -> IConnection.ErrorType.ERROR
        }

        view.showConnectionError(type)
    }
}
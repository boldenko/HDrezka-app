package com.BSLCommunity.onlinefilmstracker.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.interfaces.IConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import java.lang.Exception
import java.net.SocketTimeoutException

object ExceptionHelper {
    fun showToastError(context: Context, type: IConnection.ErrorType) {
        GlobalScope.launch {
            withContext(Dispatchers.Main){
                Toast.makeText(
                    context, when (type) {
                        IConnection.ErrorType.EMPTY -> context.getString(R.string.error_empty)
                        IConnection.ErrorType.TIMEOUT -> context.getString(R.string.error_timeout)
                        IConnection.ErrorType.NO_INTERNET -> context.getString(R.string.error_parsing)
                        else -> context.getString(R.string.error_occured)
                    }, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun catchException(e: Exception, view: IConnection){
        when (e) {
            is SocketTimeoutException -> {
                view.showConnectionError(IConnection.ErrorType.TIMEOUT)
            }
            is HttpStatusException -> {
                if (e.statusCode == 404) {
                    view.showConnectionError(IConnection.ErrorType.EMPTY)
                }
            }
        }
    }
}
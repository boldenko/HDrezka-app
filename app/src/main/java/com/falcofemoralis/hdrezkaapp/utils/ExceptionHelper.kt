package com.falcofemoralis.hdrezkaapp.utils

import android.content.Context
import android.widget.Toast
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection.ErrorType
import com.falcofemoralis.hdrezkaapp.views.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import org.jsoup.parser.ParseError
import java.net.SocketTimeoutException

object ExceptionHelper {
    fun showToastError(context: Context, type: ErrorType, errorText: String) {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                val textId: Int = when (type) {
                    ErrorType.EMPTY -> R.string.error_empty
                    ErrorType.TIMEOUT -> R.string.error_timeout
                    ErrorType.PARSING_ERROR -> R.string.error_parsing
                    ErrorType.NO_INTERNET -> R.string.no_connection
                    ErrorType.BLOCKED_SITE -> R.string.no_access
                    ErrorType.MALFORMED_URL -> R.string.malformed_url
                    ErrorType.MODERATE_BY_ADMIN -> R.string.comment_need_apply
                    ErrorType.ERROR -> R.string.error_occured
                }

                if (type == ErrorType.BLOCKED_SITE) {
                    createDialog(textId, context)
                } else {
                    Toast.makeText(context, context.getString(textId) + ": " + errorText, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createDialog(textId: Int, context: Context) {
        val dialog = MaterialAlertDialogBuilder(context)
        dialog.setTitle(context.getString(textId))
        dialog.setPositiveButton(context.getString(R.string.to_settings)) { dialog, id ->
            (context as MainActivity).openUserMenu()
        }
        dialog.setCancelable(false)
        val d = dialog.create()
        d.show()
    }

    fun catchException(e: Exception, view: IConnection) {
        e.printStackTrace()

        val type: ErrorType = when (e) {
            is SocketTimeoutException -> ErrorType.TIMEOUT
            is HttpStatusException -> {
                when (e.statusCode) {
                    404 -> ErrorType.EMPTY
                    403 -> ErrorType.MODERATE_BY_ADMIN
                    else -> ErrorType.ERROR
                }
            }
            is ParseError -> ErrorType.PARSING_ERROR
            is IndexOutOfBoundsException -> ErrorType.BLOCKED_SITE
            is IllegalArgumentException -> ErrorType.MALFORMED_URL
            else -> ErrorType.ERROR
        }

        view.showConnectionError(type, e.toString())
    }
}
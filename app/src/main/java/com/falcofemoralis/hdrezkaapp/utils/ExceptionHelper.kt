package com.falcofemoralis.hdrezkaapp.utils

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import java.io.IOException
import java.lang.Error
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

object ExceptionHelper {
    var activeDialog: AlertDialog? = null

    fun showToastError(context: Context, type: ErrorType, errorText: String) {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                val textId: Int = when (type) {
                    ErrorType.EMPTY -> R.string.error_empty
                    ErrorType.TIMEOUT -> R.string.error_timeout
                    ErrorType.PARSING_ERROR -> R.string.error_parsing
                    ErrorType.NO_INTERNET -> R.string.no_connection
                    ErrorType.BLOCKED_SITE -> R.string.no_access
                    ErrorType.PROVIDER_TIMEOUT -> R.string.provider_timeout
                    ErrorType.MALFORMED_URL -> R.string.malformed_url
                    ErrorType.MODERATE_BY_ADMIN -> R.string.comment_need_apply
                    ErrorType.ERROR -> R.string.error_occured
                    ErrorType.EMPTY_SEARCH -> R.string.search_empty
                }

                if (type == ErrorType.BLOCKED_SITE) {
                    createDialog(textId, context)
                } else if (type != ErrorType.PROVIDER_TIMEOUT) {
                    Toast.makeText(context, context.getString(textId) + ": " + errorText, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createDialog(textId: Int, context: Context) {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle(context.getString(textId))
        builder.setPositiveButton(context.getString(R.string.provider_change)) { dialog, id ->
            dialog.cancel()
        }
        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, id ->
            dialog.dismiss()
        }
        builder.setOnCancelListener {
            activeDialog = null
            (context as MainActivity).showProviderEnter()
        }
        builder.setCancelable(false)
        if (activeDialog == null) {
            activeDialog = builder.create()
        }
        activeDialog!!.show()
    }

    fun catchException(e: Exception, view: IConnection) {
        e.printStackTrace()

        val type: ErrorType = when (e) {
            is SocketTimeoutException -> ErrorType.TIMEOUT
            is HttpStatusException -> {
                when (e.statusCode) {
                    401 -> ErrorType.EMPTY_SEARCH
                    404 -> ErrorType.EMPTY
                    403 -> ErrorType.MODERATE_BY_ADMIN
                    503 -> ErrorType.PROVIDER_TIMEOUT
                    else -> ErrorType.ERROR
                }
            }
            is ParseError -> ErrorType.PARSING_ERROR
            is IllegalArgumentException -> ErrorType.MALFORMED_URL
            is IndexOutOfBoundsException -> ErrorType.BLOCKED_SITE
            is SSLHandshakeException -> ErrorType.BLOCKED_SITE
            is UnknownHostException -> ErrorType.BLOCKED_SITE
            //is IOException -> ErrorType.BLOCKED_SITE
            else -> ErrorType.ERROR
        }

        view.showConnectionError(type, e.toString())
    }
}
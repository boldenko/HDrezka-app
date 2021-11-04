package com.falcofemoralis.hdrezkaapp.utils

import android.content.Context
import com.falcofemoralis.hdrezkaapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object DialogManager {
    fun getDialog(context: Context, isCancelable: Boolean, titleResId: Int?): MaterialAlertDialogBuilder {
        val builder = MaterialAlertDialogBuilder(context)
        if(titleResId != null){
            builder.setTitle(context.getString(titleResId))

        }
        builder.setCancelable(isCancelable)

        return builder
    }
}
package com.BSLCommunity.onlinefilmstracker.objects

import java.io.Serializable

data class Bookmark(
    val catId: String,
    val link: String,
    val name: String,
    val amount: Number,
) : Serializable {
    var isChecked: Boolean? = null
}
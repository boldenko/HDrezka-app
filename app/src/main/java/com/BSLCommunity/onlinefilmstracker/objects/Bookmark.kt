package com.BSLCommunity.onlinefilmstracker.objects

data class Bookmark(
    val catId: String,
    val link: String,
    val name: String,
    val amount: Number,
) {
    var isChecked: Boolean? = null
}
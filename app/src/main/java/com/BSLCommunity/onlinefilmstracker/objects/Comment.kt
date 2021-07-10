package com.BSLCommunity.onlinefilmstracker.objects

data class Comment(
    val avatarPath: String,
    val nickname: String,
    val text: String,
    val date: String,
    val indent: Int
)

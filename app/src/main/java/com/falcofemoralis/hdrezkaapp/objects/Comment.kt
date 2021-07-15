package com.falcofemoralis.hdrezkaapp.objects

data class Comment(
    val avatarPath: String,
    val nickname: String,
    val text: ArrayList<Pair<TextType, String>>,
    val date: String,
    val indent: Int
) {
    enum class TextType {
        REGULAR,
        SPOILER,
        BOLD,
    }
}

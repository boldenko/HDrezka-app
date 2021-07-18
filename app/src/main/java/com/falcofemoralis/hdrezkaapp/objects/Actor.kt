package com.falcofemoralis.hdrezkaapp.objects

import java.io.Serializable

data class Actor(
    val link: String,
    val name: String
) : Serializable {
    var photoLink: String? = null
    var fullSizePhotoLink: String? = null
    var career: String? = null
    var born: String? = null
    var city: String? = null
    var height: String? = null
}
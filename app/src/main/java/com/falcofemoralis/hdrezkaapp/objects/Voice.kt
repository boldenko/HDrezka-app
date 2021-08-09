package com.falcofemoralis.hdrezkaapp.objects

open class Voice {
    constructor(name: String, id: String) {
        this.name = name
        this.id = id
    }

    constructor(streams: String) {
        this.streams = streams
    }

    var name: String? = null
    var id: String? = null
    var streams: String? = null
}


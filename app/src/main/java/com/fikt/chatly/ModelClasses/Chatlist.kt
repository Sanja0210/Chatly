package com.fikt.chatly.ModelClasses


class Chatlist {
    private var id: String = ""
    private var timestamp: Long = 0

    constructor()


    constructor(id: String, timestamp: Long) {
        this.id = id
        this.timestamp = timestamp
    }


    fun getId(): String? {
        return id
    }

    fun setId(id: String?) {
        this.id = id!!
    }

    fun getTimestamp(): Long? {
        return timestamp
    }

    fun setTimestamp(timestamp: Long?) {
        this.timestamp = timestamp!!
    }
}

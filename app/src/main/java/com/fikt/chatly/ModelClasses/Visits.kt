package com.fikt.chatly.ModelClasses


class Visits {
    private var otvorenoOd: String = ""
    private var otvorenoNa: String = ""

    constructor()


    constructor(otvorenoOd: String, otvorenoNa: String) {
        this.otvorenoOd = otvorenoOd
        this.otvorenoNa = otvorenoNa
    }


    fun getOtvorenoOd(): String? {
        return otvorenoOd
    }

    fun setOtvorenoOd(otvorenoOd: String?) {
        this.otvorenoOd = otvorenoOd!!
    }

    fun getOtvorenoNa(): String? {
        return otvorenoNa
    }

    fun setOtvorenoNa(otvorenoNa: String?) {
        this.otvorenoNa = otvorenoNa!!
    }
}

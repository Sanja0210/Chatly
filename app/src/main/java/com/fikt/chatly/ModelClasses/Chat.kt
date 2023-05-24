package com.fikt.chatly.ModelClasses

import android.util.Log
import androidx.annotation.Keep
import com.fikt.chatly.Enkripcija

@Keep
public class Chat {
    private var isprakjac: String? = ""
    private var poraka: String? = ""
    private var primac: String? = ""
    private var seen : Boolean = false
    private var url: String? = ""
    private var porakaId: String? = ""
    private var prateno : Boolean = true

    constructor()


    constructor(
        isprakjac: String,
        poraka: String,
        primac: String,
        seen: Boolean,
        url: String,
        porakaId: String,
        prateno: Boolean = true
    ) {

        this.isprakjac = isprakjac
        this.poraka = poraka
        this.primac = primac
        this.seen = seen
        this.url = url
        this.porakaId = porakaId
        this.prateno = prateno
    }



    fun getIsprakjac(): String? {
        return isprakjac
    }

    fun setIsprakjac(isprakjac: String?) {
        this.isprakjac = isprakjac!!
    }

    fun getPrateno(): Boolean {
        return prateno
    }

    fun setPrateno(prateno: Boolean?) {
        this.prateno = prateno!!
    }

    fun getPoraka(): String? {
        val enkripcijaDek = Enkripcija()
        return poraka?.let { enkripcijaDek.decrypt(it) }
    }

    fun setPoraka(poraka: String?) {
        this.poraka = poraka!!
    }

    fun getPrimac(): String? {
        return primac
    }

    fun setPrimac(primac: String?) {
        this.primac = primac!!
    }

    fun getSeen(): Boolean {
        return seen
    }

    fun setSeen(seen: Boolean?) {
        this.seen = seen!!
    }

    fun getUrl(): String? {
        return url
    }

    fun setUrl(url: String?) {
        this.url = url!!
    }

    fun getPorakaId(): String? {
        return porakaId
    }

    fun setPorakaId(porakaId: String?) {
        this.porakaId = porakaId!!
    }
}
package com.fikt.chatly.ModelClasses

class Users {
    private var uid : String? = ""
    private var username : String? = ""
    private var fullname : String? = ""
    private var profile : String? = ""
    private var cover : String? = ""
    private var status : String? = ""
    private var search : String? = ""
    private var facebook : String? = ""
    private var instagram : String? = ""
    private var website : String? = ""
    private var gender : String? = ""
    private var timestamp : Long? = 0
    private var dostapnost : Int? = 0
    private var gostin : Int? = 0
    private var kodPotvrda : String? = ""

    constructor() {

    }

    constructor(
        uid: String?,
        username: String?,
        fullname: String?,
        profile: String?,
        cover: String?,
        status: String?,
        search: String?,
        facebook: String?,
        instagram: String?,
        website: String?,
        gender: String?,
        dostapnost: Int?,
        gostin: Int?,
        kodPotvrda: String? = "",
        timestamp : Long? = 0
    ) {
        this.uid = uid
        this.username = username
        this.fullname = fullname
        this.profile = profile
        this.cover = cover
        this.status = status
        this.search = search
        this.facebook = facebook
        this.instagram = instagram
        this.website = website
        this.gender = gender
        this.dostapnost = dostapnost
        this.gostin = gostin
        this.kodPotvrda = kodPotvrda
        this.timestamp = timestamp
    }

    fun getUID() : String? {
        return uid
    }

    fun setUID(uid : String) {
        this.uid = uid
    }

    fun getUsername() : String? {
        return username
    }

    fun setUsername(username : String) {
        this.username = username
    }

    fun getKodPotvrda() : String? {
        return kodPotvrda
    }

    fun setKodPotvrda(kodPotvrda : String) {
        this.kodPotvrda = kodPotvrda
    }

    fun getFullname() : String? {
        return fullname
    }

    fun setFullname(fullname: String) {
        this.fullname = fullname
    }

    fun getProfile() : String? {
        return profile
    }

    fun setProfile(profile : String) {
        this.profile = profile
    }

    fun getCover() : String? {
        return cover
    }

    fun setCover(cover : String) {
        this.cover = cover
    }

    fun getStatus() : String? {
        return status
    }

    fun setStatus(status : String) {
        this.status = status
    }

    fun getSearch() : String? {
        return search
    }

    fun setSearch(search : String) {
        this.search = search
    }

    fun getFacebook() : String? {
        return facebook
    }

    fun setFacebook(facebook: String) {
        this.facebook = facebook
    }

    fun getInstagram() : String? {
        return instagram
    }

    fun setInstagram(instagram: String) {
        this.instagram = instagram
    }

    fun getWebsite() : String? {
        return website
    }

    fun setWebsite(website: String) {
        this.website = website
    }



    fun getGender() : String? {
        return gender
    }

    fun setGender(gender: String) {
        this.gender = gender
    }

    fun getDostapnost() : Int? {
        return dostapnost
    }

    fun setDostapnost(dostapnost: Int) {
        this.dostapnost = dostapnost
    }

    fun getGostin() : Int? {
        return gostin
    }

    fun setGostin(gostin: Int) {
        this.gostin = gostin
    }

    fun getTimestamp() : Long? {
        return timestamp
    }

    fun setTimestamp(timestamp: Long?) {
        this.timestamp = timestamp
    }



}
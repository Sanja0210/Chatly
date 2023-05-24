package com.fikt.chatly

import com.fikt.chatly.ModelClasses.Users

interface IListener {
    fun onUserClickListener(user: Users?)
}
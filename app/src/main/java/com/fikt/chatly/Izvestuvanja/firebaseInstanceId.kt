package com.fikt.chatly.Izvestuvanja

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService

class firebaseInstanceId : FirebaseMessagingService() {
    override fun onNewToken(p0: String)
    {
        super.onNewToken(p0)

        val firebaseKorisnik = FirebaseAuth.getInstance().currentUser
        val refreshToken = FirebaseInstanceId.getInstance().token

        if (firebaseKorisnik!= null)
        {
            updateToken(refreshToken)
        }
    }



    private fun updateToken(refreshToken: String?)
    {
        val firebaseKorisnik = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").getReference().child("tokens")
        val token = Token(refreshToken!!)
        ref.child(firebaseKorisnik!!.uid).setValue(token)
    }
}
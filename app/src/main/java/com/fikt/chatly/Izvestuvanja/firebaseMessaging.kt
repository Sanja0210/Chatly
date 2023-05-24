package com.fikt.chatly.Izvestuvanja

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.fikt.chatly.MessageChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class firebaseMessaging : FirebaseMessagingService() {
    override fun onMessageReceived(mRemoteMessage: RemoteMessage) {
        super.onMessageReceived(mRemoteMessage)

        val isprateno = mRemoteMessage.data["sented"]
        val korisnik = mRemoteMessage.data["user"]
        val sharedPref = getSharedPreferences("PREFS", Context.MODE_PRIVATE)

        val momentalnoAktivenKorisnik = sharedPref.getString("currentUser", "none")
        val firebaseKorisnik = FirebaseAuth.getInstance().currentUser

        if (firebaseKorisnik != null && isprateno == firebaseKorisnik.uid) {
            if (momentalnoAktivenKorisnik != korisnik) {
                //proveri dali e oreo
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    //oreo+
                    ispratiNotifikacijaOreo(mRemoteMessage)
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ispratiNotifikacijaM(mRemoteMessage)
                    } else {

                    }
                    ispratiNotifikacija(mRemoteMessage)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun ispratiNotifikacijaM(mRemoteMessage: RemoteMessage) {
        val korisnik = mRemoteMessage.data["user"]
        val ikona = mRemoteMessage.data["icon"]
        val naslov = mRemoteMessage.data["title"]
        val sodrzina = mRemoteMessage.data["body"]

        val notification = mRemoteMessage.notification
        val kBr = korisnik!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, MessageChatActivity::class.java)

        val bundle = Bundle()
        bundle.putString("idNaDrugiot", korisnik)
//        bundle.putString("idNa", korisnik)
        intent.putExtras(bundle)

        var pendingIntent : PendingIntent = PendingIntent.getActivity(this, kBr, intent, PendingIntent.FLAG_IMMUTABLE)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val oreoNotification = OreoNotification(this)
        val builder : Notification.Builder = oreoNotification.getOreoNotification(naslov, sodrzina, pendingIntent, defaultSound, ikona)

        var i = 0
        if (kBr > 0) {
            i = kBr
        }

        oreoNotification.getManager!!.notify(i, builder.build())
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun ispratiNotifikacijaOreo(mRemoteMessage: RemoteMessage) {
        val korisnik = mRemoteMessage.data["user"]
        val ikona = mRemoteMessage.data["icon"]
        val naslov = mRemoteMessage.data["title"]
        val sodrzina = mRemoteMessage.data["body"]

        val notification = mRemoteMessage.notification
        val kBr = korisnik!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, MessageChatActivity::class.java)

        val bundle = Bundle()
        bundle.putString("idNaDrugiot", korisnik)
        intent.putExtras(bundle)

        var pendingIntent : PendingIntent = PendingIntent.getActivity(this, kBr, intent, PendingIntent.FLAG_IMMUTABLE)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val oreoNotification = OreoNotification(this)
        val builder : Notification.Builder = oreoNotification.getOreoNotification(naslov, sodrzina, pendingIntent, defaultSound, ikona)

        var i = 0
        if (kBr > 0) {
            i = kBr
        }

        oreoNotification.getManager!!.notify(i, builder.build())
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun ispratiNotifikacija(mRemoteMessage: RemoteMessage) {
        val korisnik = mRemoteMessage.data["user"]
        val ikona = mRemoteMessage.data["icon"]
        val naslov = mRemoteMessage.data["title"]
        val sodrzina = mRemoteMessage.data["body"]

        val notification = mRemoteMessage.notification
        val kBr = korisnik!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, MessageChatActivity::class.java)

        val bundle = Bundle()
        bundle.putString("idNaDrugiot", korisnik)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, kBr, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this)
            .setSmallIcon(ikona!!.toInt())
            .setContentTitle(naslov)
            .setContentText(sodrzina)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentIntent(pendingIntent)

        val notiManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var i = 0
        if (kBr > 0) {
            i = kBr
        }

        notiManager.notify(i, builder.build())
    }
}
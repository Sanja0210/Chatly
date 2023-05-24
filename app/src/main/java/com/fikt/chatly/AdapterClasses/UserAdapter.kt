package com.fikt.chatly.AdapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.opengl.Visibility
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.fikt.chatly.Fragments.ChatsFragment
import com.fikt.chatly.Fragments.MessageChatFragment
import com.fikt.chatly.Fragments.SearchFragment
import com.fikt.chatly.IListener
import com.fikt.chatly.MessageChatActivity
import com.fikt.chatly.ModelClasses.Chat
import com.fikt.chatly.ModelClasses.Users
import com.fikt.chatly.R
import com.fikt.chatly.VisitUserActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.sql.Date
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime

class UserAdapter(
    mContext: Context,
    mUsers: List<Users>,
    isChatCheck: Boolean,
    listerKlik: IListener
) : RecyclerView.Adapter<UserAdapter.ViewHolder?>() {
    private val mContext : Context = mContext
    private val mUsers : List<Users> = mUsers.sortedBy { it.getTimestamp() }.reversed()
    private val isChatCheck : Boolean = isChatCheck
    private val listerKlik : IListener = listerKlik
    var poslednaPorakaVar : String = ""
    var procitanoPosledna : Boolean = true
    private var firestoreDb : FirebaseFirestore? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        //implementacija i mestenje na viewholder za adapterot
        val view : View = LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout, viewGroup, false)

        return UserAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firestoreDb = Firebase.firestore
        // zemi go potrebniot korisnik i-tiot korisnik i napraj mu element so slika i tekst
        val user : Users = mUsers[position]
        holder.usernameText.text = user.getUsername()
        if (user.getProfile() != "") {
            Picasso.get().load(user.getProfile()).placeholder(R.drawable.profile).into(holder.profileImageView)
        }

        val vreme = Timestamp(user.getTimestamp()!!)
        var vremeSat = ""

        var formatDatum = SimpleDateFormat("dd/MM/yyyy k:m:s")
        var vrednostDatum = Date(user.getTimestamp()!!)
        var datumPratenaPoraka = formatDatum.format(vrednostDatum)

        val momTimestamp = java.sql.Timestamp(System.currentTimeMillis()).time
        var momentVremeTemp = Date(momTimestamp)
        var momentVreme = formatDatum.format(momentVremeTemp)

        val razlika = momTimestamp - user.getTimestamp()!!
        Log.i("RAZLIKA", razlika.toString())
        val sekundiRazlika = (razlika / 1000).toInt()
        val minutiRazlika = (sekundiRazlika / 60).toInt()
        val satiRazlika = (minutiRazlika / 60).toInt()
        val denoviRazlika = (satiRazlika / 24).toInt()
        val nedeliRazlika = (denoviRazlika / 7).toInt()
        val meseciRazlika = (denoviRazlika / 30).toInt()
        val godiniRazlika = (denoviRazlika / 365).toInt()

        var tekstZaPoslednaPoraka = ""
        if (godiniRazlika > 0) tekstZaPoslednaPoraka = godiniRazlika.toString() + "y"
        else if (meseciRazlika > 0) tekstZaPoslednaPoraka = meseciRazlika.toString() + "mo"
        else if (nedeliRazlika > 0) tekstZaPoslednaPoraka = nedeliRazlika.toString() + "w"
        else if (denoviRazlika > 0) tekstZaPoslednaPoraka = denoviRazlika.toString() + "d"
        else if (satiRazlika > 0) tekstZaPoslednaPoraka = satiRazlika.toString() + "h"
        else if (minutiRazlika > 0) tekstZaPoslednaPoraka = minutiRazlika.toString() + "m"
        else if (sekundiRazlika > 0) tekstZaPoslednaPoraka = sekundiRazlika.toString() + "s"

        //online i offline status
        if (isChatCheck)
        {
            if (razlika != momTimestamp) {
                holder.poslednaPorakaVreme.text = tekstZaPoslednaPoraka
            }
            zemiPoslednaPoraka(user.getUID(), holder.poslednaPorakaTekst, holder.procitanoPosledna)
        }
        else
        {
            holder.usernameText.text = user.getFullname()
            holder.poslednaPorakaTekst.text = "@" + user.getUsername().toString()
//            holder.poslednaPorakaTekst.visibility = View.GONE
        }

        if (isChatCheck)
        {
            if (user.getStatus() == "online")
            {
                holder.onlineImageView.visibility = View.VISIBLE
                holder.offlineImageView.visibility = View.GONE
            }
            else
            {
                holder.onlineImageView.visibility = View.GONE
                holder.offlineImageView.visibility = View.VISIBLE
            }
        }
        else
        {
            holder.onlineImageView.visibility = View.GONE
            holder.offlineImageView.visibility = View.GONE
        }

        holder.itemView.setOnClickListener{

            if (isChatCheck) {
                listerKlik.onUserClickListener(user)
//                val intent = Intent(mContext, MessageChatActivity::class.java)
//                intent.putExtra("idNaDrugiot", user.getUID())
//                intent.putExtra("username", user.getUsername())
//                intent.putExtra("profile", user.getProfile())
//                mContext.startActivity(intent)
            } else {

                val intent = Intent(mContext, VisitUserActivity::class.java)
                intent.putExtra("profilZaOtvoranje", user.getUID())
                mContext.startActivity(intent)
            }
        }
    }

    private fun zemiPoslednaPoraka(uid: String?, poslednaPorakaTekst: TextView, procitanoPoslednoImage : CircleImageView) {
        poslednaPorakaVar = mContext.getString(R.string.nemaPoraka)
        procitanoPosledna = true
        var ispratenoOdMene = false

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val refrence = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference.child("chats")

        refrence.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot)
            {
                for (dataSnapshot in p0.children)
                {
                    ispratenoOdMene = false
                    val chat: Chat? = dataSnapshot.getValue(Chat::class.java)

                    if (firebaseUser!=null && chat!=null)
                    {
                        if (chat.getPrimac() == firebaseUser.uid  &&
                            chat.getIsprakjac() == uid  ||
                            chat.getPrimac() == uid  &&
                            chat.getIsprakjac() == firebaseUser.uid)
                        {
                            poslednaPorakaVar = chat.getPoraka()!!

                            if (chat.getIsprakjac() == firebaseUser.uid) {
                                ispratenoOdMene = true
                            }

                            if (!chat.getSeen() &&  chat.getPrimac() == firebaseUser.uid) {
                                procitanoPosledna = false
                            }
                        }
                    }
                }

                Log.i("ispratenoodkoj", ispratenoOdMene.toString())
                when(poslednaPorakaVar)
                {
                    "sent you an image." -> poslednaPorakaTekst.text = "image sent."
                    else ->  {
                        if (ispratenoOdMene) {
                            poslednaPorakaTekst.text = "Me: " + poslednaPorakaVar
                        } else {
                            poslednaPorakaTekst.text = poslednaPorakaVar
                        }
                    }
                }
                if (procitanoPosledna == false) {
                    procitanoPoslednoImage.visibility = View.VISIBLE
                } else {
                    procitanoPoslednoImage.visibility = View.GONE
                }
                poslednaPorakaVar = mContext.getString(R.string.nemaPoraka)
                ispratenoOdMene = false
                procitanoPosledna = true
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var usernameText : TextView = itemView.findViewById(R.id.username_text_search)
        var profileImageView : CircleImageView = itemView.findViewById(R.id.profile_image_search)
        var onlineImageView : CircleImageView = itemView.findViewById(R.id.image_online)
        var offlineImageView : CircleImageView = itemView.findViewById(R.id.image_offline)
        var poslednaPorakaTekst : TextView = itemView.findViewById(R.id.poslednaPorakaTekst)
        var poslednaPorakaVreme : TextView = itemView.findViewById(R.id.poslednaPorakaVreme)
        var procitanoPosledna : CircleImageView = itemView.findViewById(R.id.slika_neprocitano)
    }
}
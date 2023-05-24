package com.fikt.chatly.Fragments

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fikt.chatly.AdapterClasses.UserAdapter
import com.fikt.chatly.IListener
import com.fikt.chatly.Izvestuvanja.Token
import com.fikt.chatly.MessageChatActivity
import com.fikt.chatly.ModelClasses.Chatlist
import com.fikt.chatly.ModelClasses.Users
import com.fikt.chatly.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId

class ChatsFragment : Fragment(), IListener {
    private var userAdapter : UserAdapter? = null
    private var korisnici : List<Users>? = null
    private var korisniciChatList : List<Chatlist>? = null
    lateinit var chatListRecycler : RecyclerView
    private var firebaseKorisnik : FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

       chatListRecycler = view.findViewById(R.id.chatLista_recycler)
        chatListRecycler.setHasFixedSize(true)
        chatListRecycler.layoutManager = LinearLayoutManager(context)

        firebaseKorisnik = FirebaseAuth.getInstance().currentUser
//        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        korisniciChatList = ArrayList()


        //firebase db
        val ref = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference.child("chatList").child(firebaseKorisnik!!.uid)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (korisniciChatList as ArrayList).clear()

                for (dataSnapshot in p0.children)
                {
                    val korisnik = dataSnapshot.getValue(Chatlist::class.java)

                    (korisniciChatList as ArrayList).add(korisnik!!)
                    popolniChatlist()
                }

                popolniChatlist()
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })

        updateToken(FirebaseInstanceId.getInstance().token)

        return view
    }

    private fun updateToken(token: String?) {
        if (token != null) {
            val ref = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference.child("tokens")
            val token1 = Token(token!!)
            ref.child(firebaseKorisnik!!.uid).setValue(token1)
        }
    }

    private fun popolniChatlist() {
        korisnici = ArrayList()

        val ref = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference.child("users")

        ref!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (korisnici as ArrayList).clear()

                for (dataSnapshot in p0.children) {
                    val korisnik = dataSnapshot.getValue(Users::class.java)

                    for (sekojChatList in korisniciChatList!!) {
                        if (korisnik!!.getUID().equals(sekojChatList.getId())) {
                            korisnik.setTimestamp(sekojChatList.getTimestamp())
                            (korisnici as ArrayList).add(korisnik!!)
                        }
                    }
                }

                if (context != null) {
                    userAdapter = UserAdapter(context!!, (korisnici as ArrayList<Users>), true, this@ChatsFragment)

                    chatListRecycler.adapter = userAdapter
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })

    }

    fun isTablet(ctx: Context): Boolean {
        return ctx.getResources()
            .getConfiguration().screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    override fun onUserClickListener(user: Users?) {
        if (context?.let { isTablet(it) } == false) {
            val intent = Intent(context, MessageChatActivity::class.java)
                intent.putExtra("idNaDrugiot", user!!.getUID())
                intent.putExtra("user!!name", user!!.getUsername())
                intent.putExtra("profile", user!!.getProfile())
                requireContext().startActivity(intent)
        } else {
            val fragmentPoraka = parentFragmentManager.findFragmentById(R.id.detail_container) as MessageChatFragment?
            if (user != null) {
                user.getUID()?.let {
                    if (fragmentPoraka != null) {
                        fragmentPoraka.kreirajViewPorakiChat(it)
                    }
                }
            }
        }
    }
}
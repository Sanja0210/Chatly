package com.fikt.chatly.AdapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import com.fikt.chatly.ModelClasses.Chat
import com.fikt.chatly.R
import com.fikt.chatly.VidiCelosnaSlikaActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
class ChatsAdapter(
    mContext: Context,
    mChatList: List<Chat>,
    imageUrl: String,
    isOnline: Boolean
) : RecyclerView.Adapter<ChatsAdapter.ViewHolder?>()
{
    private val mContext: Context
    private val mChatList: List<Chat>
    private val imageUrl: String
    private val isOnline: Boolean

    var firebaseKorisnik: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    init {
        this.mChatList = mChatList
        this.mContext = mContext
        this.imageUrl = imageUrl
        this.isOnline = isOnline
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == 1)
        {
            val view: View = LayoutInflater.from(mContext).inflate(com.fikt.chatly.R.layout.sent_message, parent, false)
            ViewHolder(view)
        }
        else if (viewType == 0)
        {
            val view: View = LayoutInflater.from(mContext).inflate(com.fikt.chatly.R.layout.received_message, parent, false)
            ViewHolder(view)
        } else if (viewType == 2) {
            val view: View = LayoutInflater.from(mContext).inflate(com.fikt.chatly.R.layout.received_image, parent, false)
            ViewHolder(view)
        } else {
            val view: View = LayoutInflater.from(mContext).inflate(com.fikt.chatly.R.layout.sent_image, parent, false)
            ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val poraka = mChatList[position]

        val porakaId = poraka.getPorakaId()
        val porakaIme = poraka.getPoraka()

        Log.i("PORAKA_LOCAL", "$porakaId -,p $porakaIme")

        if (this.imageUrl.toString() != "") {
            Picasso.get().load(this.imageUrl).into(holder.profilnaSlika)
        }

        //slika vo poraka
        if (poraka.getPoraka().equals("sent you an image.") && !poraka.getUrl().equals(""))
        {
            //pratena slika
            if (poraka.getIsprakjac().equals(firebaseKorisnik!!.uid))
            {
//                holder.tekstPoraka!!.visibility = View.GONE
                Log.i("PORAKA_PRATENA", poraka.getUrl().toString())
//                holder.pratenaSlika!!.visibility = View.VISIBLE
                Picasso.get().load(poraka.getUrl()).into(holder.pratenaSlika)

                holder.pratenaSlika!!.setOnClickListener {
                    val intent = Intent(mContext, VidiCelosnaSlikaActivity::class.java)
                    intent.putExtra("url", poraka.getUrl())
                    mContext.startActivity(intent)
                }
            }
            //primena slika
            else if (!poraka.getIsprakjac().equals(firebaseKorisnik!!.uid))
            {
//                holder.tekstPoraka!!.visibility = View.GONE
//                holder.primenaSlika!!.visibility = View.VISIBLE
                Log.i("PORAKA_PRIMENA", poraka.getUrl().toString())
                Picasso.get().load(poraka.getUrl()).into(holder.primenaSlika)

                holder.primenaSlika!!.setOnClickListener {
                    val intent = Intent(mContext, VidiCelosnaSlikaActivity::class.java)
                    intent.putExtra("url", poraka.getUrl())
                    mContext.startActivity(intent)
                }
            }
        } else {
            //tekst vo poraka‘
                holder.tekstPoraka!!.visibility = View.VISIBLE


            holder.tekstPoraka!!.text = poraka.getPoraka()

                if (firebaseKorisnik!!.uid == poraka.getIsprakjac())
                {
//                    holder.pratenaSlika!!.visibility = View.GONE

                    holder.tekstPoraka!!.setOnClickListener {
                        val options = arrayOf<CharSequence>(
                            mContext.getString(R.string.izbrisiPoraka),
                            mContext.getString(R.string.otkazi)
                        )

                        var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                        builder.setTitle(mContext.getString(R.string.shosakash))

                        builder.setItems(options, DialogInterface.OnClickListener{
                                dialog, which ->
                            if (which == 0)
                            {
                                izbrisiPratenaPoraka(position, holder)
                            }
                        })
                        builder.show()
                    }
                } else {
//                    holder.primenaSlika!!.visibility = View.GONE

                }
        }

        if (poraka.getPrateno() == false) {
            if (poraka.getUrl() == "") {
            holder.tekstPoraka!!.background.alpha = 128

            }
        } else {
            if (poraka.getUrl() == "") {
                holder.tekstPoraka!!.background.alpha = 255
            }
        }

        if (position == mChatList.size-1)
        {
            if (poraka.getPrateno() == false) {
                holder.porakaSeen!!.text = ""
            } else
            if (poraka.getSeen())
            {
                holder.porakaSeen!!.text = mContext.getString(R.string.procitano)

                if (poraka.getPoraka().equals("sent you an image.") && !poraka.getUrl().equals(""))
                {
                    val porakaSeenElement: RelativeLayout.LayoutParams? = holder.porakaSeen!!.layoutParams as RelativeLayout.LayoutParams?
                    porakaSeenElement!!.setMargins(0, 3, 10, 0)
                    holder.porakaSeen!!.layoutParams = porakaSeenElement
                }
            }
            else
            {
                holder.porakaSeen!!.text = mContext.getString(R.string.isprateno)

                if (poraka.getPoraka().equals("sent you an image.") && !poraka.getUrl().equals(""))
                {
                    val porakaSeenElement: RelativeLayout.LayoutParams? = holder.porakaSeen!!.layoutParams as RelativeLayout.LayoutParams?
                    porakaSeenElement!!.setMargins(0, 3, 10, 0)
                    holder.porakaSeen!!.layoutParams = porakaSeenElement
                }
            }
        }
        else
        {
            holder.porakaSeen!!.visibility = View.GONE
        }
    }

    private fun izbrisiPratenaPoraka(position: Int, holder: ChatsAdapter.ViewHolder) {
        val ref = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference.child("chats")
            .child(mChatList.get(position).getPorakaId()!!)
            .removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    Toast.makeText(holder.itemView.context, mContext.getString(R.string.izbrisano), Toast.LENGTH_SHORT).show()
                }
                else
                {
                    Toast.makeText(holder.itemView.context, mContext.getString(R.string.greskaProbaj), Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun getItemCount(): Int {
        return mChatList.size
    }


    override fun getItemViewType(position: Int): Int
    {
        var tipNaPoraka = 0
        // 0 - primen tekst
        //1 - ispraten tekst
        //2 - primena slika
        //3 - ispratena slika
        if (mChatList[position].getIsprakjac().equals(firebaseKorisnik!!.uid))
        {
            if (mChatList[position].getUrl() == "") {
                //tekst praten
                tipNaPoraka = 1
            } else {
                //slika pratena
            tipNaPoraka = 3
            }
        }
        else
        {
            if (mChatList[position].getUrl() == "") {
                tipNaPoraka = 0
            } else {
                tipNaPoraka = 2
            }

            }

        return tipNaPoraka
    }




    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var profilnaSlika: CircleImageView? = null
        var tekstPoraka: TextView? = null
        var primenaSlika: ImageView? = null
        var porakaSeen: TextView? = null
        var pratenaSlika: ImageView? = null
        var porakaRelativeLayout: RelativeLayout? = null

        init {
            profilnaSlika = itemView.findViewById(R.id.profilnaKorisnik)
            tekstPoraka = itemView.findViewById(R.id.tekstPoraka)
            primenaSlika = itemView.findViewById(R.id.primenaSlika)
            porakaSeen = itemView.findViewById(R.id.seen)
            pratenaSlika = itemView.findViewById(R.id.pratenaSlika)
            porakaRelativeLayout = itemView.findViewById(R.id.porakaRelative)
        }
    }
}
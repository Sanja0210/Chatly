package com.fikt.chatly

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.fikt.chatly.ModelClasses.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthSettings
import com.google.firebase.database.*

class VerificationActivity : AppCompatActivity() {
    private lateinit var potvrdiProfilBtn : Button
    private lateinit var kodVerifikacijaEdit : EditText
    private lateinit var vratiNazad : ImageView

    //
    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers : DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        potvrdiProfilBtn = findViewById(R.id.potvrdiProfilBtn)
        kodVerifikacijaEdit = findViewById(R.id.kod_email)
        vratiNazad = findViewById(R.id.vratiNazad)

        mAuth = FirebaseAuth.getInstance()

        //toolbar
        vratiNazad.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, WelcomeActivity::class.java)

            startActivity(intent)
            finish()
        }


        var intentMain = Intent(this, MainActivity::class.java)

        potvrdiProfilBtn.setOnClickListener {
            Log.i("VERIFY", mAuth.currentUser!!.uid)
            refUsers = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference
                .child("users")
                .child(mAuth.currentUser!!.uid)

            refUsers.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val user : Users? = p0.getValue(Users::class.java)
                        Log.i("VERIFY", user!!.getKodPotvrda().toString())
                        Log.i("VERIFY", kodVerifikacijaEdit.text.toString())
                        if (user!!.getKodPotvrda() == kodVerifikacijaEdit.text.toString())                        {
                            val verifikacijaHash = HashMap<String, Any>()
                            verifikacijaHash["kodPotvrda"] = ""

                            refUsers.updateChildren(verifikacijaHash).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
//                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intentMain)
                                    finish()
                                }

                            }
                        } else {
                             Toast.makeText(this@VerificationActivity, getString(R.string.failVerification), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@VerificationActivity, getString(R.string.failVerification), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }
}
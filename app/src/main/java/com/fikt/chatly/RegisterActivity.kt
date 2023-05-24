package com.fikt.chatly

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.sql.Timestamp
import java.util.*
import kotlin.collections.set


class RegisterActivity : AppCompatActivity() {
    //varijabli firebase
    private lateinit var mAuth : FirebaseAuth
    private lateinit var refUsers : DatabaseReference
    private var firebaseUserId : String = ""
    private lateinit var firebaseAnalytics: FirebaseAnalytics


    //varijabli ui komponenti
    private lateinit var registerBtn : Button
    private lateinit var registerEmailEdit : EditText
    private lateinit var registerUsernameEdit : EditText
    private lateinit var registerPasswordEdit : EditText
    private lateinit var registerFullname : EditText
    private lateinit var vratiNazad : ImageView
    private lateinit var polOdberi : Spinner


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        firebaseAnalytics = Firebase.analytics


        //deklariraj elementi del
        registerBtn = findViewById<Button>(R.id.register_btn)
        registerEmailEdit = findViewById<EditText>(R.id.email_register)
        registerUsernameEdit = findViewById<EditText>(R.id.username_register)
        registerPasswordEdit = findViewById<EditText>(R.id.password_register)
        registerFullname = findViewById<EditText>(R.id.fullname_register)
        vratiNazad = findViewById(R.id.vratiNazad)
        polOdberi = findViewById(R.id.pol_spinner)

//        val toolbar : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_register)
//        setSupportActionBar(toolbar)
//
//        //title na top menu
//        supportActionBar!!.title = "Register"
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//
//        //back da vrakja na welcome
//        toolbar.setNavigationOnClickListener {
//            val intent = Intent(this, WelcomeActivity::class.java)
//
//            startActivity(intent)
//            finish()
//        }

        // racno back kopce
        vratiNazad.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)

            startActivity(intent)
            finish()
        }

        // init na pol select
        ArrayAdapter.createFromResource(
            this,
            R.array.pol_array,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            polOdberi.adapter = adapter
        }

        //vrzvanje so firebase
        mAuth = FirebaseAuth.getInstance()

        registerBtn.setOnClickListener {
            registerUser()
        }
    }

    private fun ispratiPorakaMail(email : String, subject : String, message : String) {
        val mEmail: String = email.toString()
        val mSubject: String = subject.toString()
        val mMessage: String = message.toString()
        val javaMailAPI = MailApi(this, mEmail, mSubject, mMessage)
        javaMailAPI.execute()
    }

    private fun registerUser() {
        val username : String = registerUsernameEdit.text.toString()
        val password : String = registerPasswordEdit.text.toString()
        val email : String = registerEmailEdit.text.toString()
        val fullname : String = registerFullname.text.toString()
        val odbranPol : String = polOdberi.selectedItem.toString()
        Log.i("POL", odbranPol)


        val daliEValidno : Boolean = proveriValidnost(username, password, email, fullname)

        if (daliEValidno) {
            // validen korisnik i dodaj vo baza i vo firebase authentication

            //proveri username
            val ref = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference
                .child("users").orderByChild("username").equalTo(username)


            val intentActivity = Intent(this, VerificationActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if (!p0.exists()) {

                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                                task ->
                            if (task.isSuccessful) {
                                firebaseUserId = mAuth.currentUser!!.uid
                                refUsers = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference.child("users").child(firebaseUserId)

                                val userHashMap = HashMap<String, Any>()
                                userHashMap["uid"] = firebaseUserId

                                userHashMap["status"] = "offline"
                                userHashMap["username"] = username
                                userHashMap["search"] = username.lowercase(Locale.getDefault())
                                userHashMap["facebook"] = "https://facebook.com"
                                userHashMap["instagram"] = "https://instagram.com"
                                userHashMap["website"] = "https://google.com"
                                userHashMap["fullname"] = fullname
                                userHashMap["gender"] = odbranPol
                                userHashMap["dostapnost"] = 0
                                userHashMap["gostin"] = 0

                                if (odbranPol == "Male" || odbranPol == "Машко") {
                                    userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/chatly-f63cf.appspot.com/o/m1.jpg?alt=media&token=14b1ea15-8f83-46ff-8edb-9e25b3060a38"
                                } else {
                                    userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/chatly-f63cf.appspot.com/o/f1.jpg?alt=media&token=5ef51edc-66fa-4a99-9cd7-5c443ab48e6e"
                                }
                                userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/chatly-f63cf.appspot.com/o/cover.jpg?alt=media&token=590ea2bd-1f43-40af-9f85-db1d44f3623f"


                                val momTimestamp = Timestamp(System.currentTimeMillis()).time.toString()
                                val kodPotvrda = momTimestamp.substring(momTimestamp.length - 6, momTimestamp.length)

                                userHashMap["kodPotvrda"] = kodPotvrda

                                refUsers.updateChildren(userHashMap)
                                    .addOnCompleteListener {task1 ->
                                        if (task1.isSuccessful) {
                                            firebaseAnalytics.logEvent("registracija") {
                                                param("registriran", userHashMap["uid"].toString())
                                            }

                                            ispratiPorakaMail(email, getString(R.string.potvrdiAkaunt), kodPotvrda)
                                            startActivity(intentActivity)
                                            finish()
                                        }
                                    }
                            } else {
                                Toast.makeText(this@RegisterActivity, "Error: " + task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this@RegisterActivity, getString(R.string.vekjepostoiUser), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })

        } else {
            //prikazi poraka deka se zadolzitelni polinjata
                if (!isValidEmail(email)) {
                    Toast.makeText(this, getString(R.string.emailNevaliden), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, getString(R.string.siteZadolzitelni), Toast.LENGTH_LONG).show()
                }
        }
    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target.toString()).matches()
        }
    }

    private fun proveriValidnost(username: String, password: String, email: String, fullname : String): Boolean {
        //proverka dali site polinja se popolnati
        return !(username == "" || password == "" || !isValidEmail(email) || fullname == "")
    }
}
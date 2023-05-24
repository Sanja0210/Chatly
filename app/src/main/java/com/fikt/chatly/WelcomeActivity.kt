package com.fikt.chatly

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fikt.chatly.ModelClasses.Users
import com.facebook.*
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.util.*


class WelcomeActivity : AppCompatActivity() {
    var firebaseUser : FirebaseUser? = null

    private lateinit var registerWelcomeBtn : Button
    private lateinit var loginWelcomeBtn : Button
    private lateinit var googleSignInBtn : Button
    private lateinit var loginX : Button
    private lateinit var facebookBtn : Button
    private lateinit var facebookLoginBtn : com.facebook.login.widget.LoginButton
    private lateinit var firebaseAnalytics : FirebaseAnalytics

    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers : DatabaseReference
    private lateinit var callbackManager: CallbackManager
    private var loginSo : String = ""
    lateinit var locale: Locale
    private var currentLanguage = "en"
    companion object {
        const val RC_SIGN_IN = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        FacebookSdk.sdkInitialize(getApplicationContext());
//        AppEventsLogger.activateApp(this@WelcomeActivity);

        mAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = Firebase.analytics

        intent = intent

        if (intent.getStringExtra("jazik") != null) {
            currentLanguage = intent.getStringExtra("jazik").toString()
        }

        val font = Typeface.createFromAsset(assets, "fontawesome-webfont.ttf")



        val sharedPreference =  getSharedPreferences("CHATX",Context.MODE_PRIVATE)
        val momJazik = sharedPreference.getString("jazik", "en").toString()

        setLocale(momJazik)


        Log.i("WELCOME_JAZIK", momJazik)


        //definiraj ui komponenti
        registerWelcomeBtn = findViewById<Button>(R.id.register_welcome_btn)
        loginWelcomeBtn = findViewById<Button>(R.id.login_welcome_btn)
        googleSignInBtn = findViewById(R.id.googleSignIn)
        loginX = findViewById(R.id.login_x)
        facebookBtn = findViewById(R.id.facebookLogin)
        facebookLoginBtn = findViewById(R.id.facebookLoginBtn)
        loginX.setTypeface(font)
        facebookBtn.setTypeface(font)
        googleSignInBtn.setTypeface(font)


        //facebook login
        facebookBtn.setOnClickListener {
            loginSo = "facebook"
            facebookLoginBtn.performClick()
        }
        callbackManager = CallbackManager.Factory.create()
        facebookLoginBtn.setReadPermissions("email", "public_profile")
        facebookLoginBtn.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d("FACEBOOK_LOGIN", "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d("FACEBOOK_LOGIN", "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d("FACEBOOK_LOGIN", "facebook:onError", error)
            }
        })

        //guest login
        loginX.setOnClickListener {
            mAuth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = mAuth.currentUser
                        firebaseAnalytics.logEvent("logiran_guest") {
                            param("user", user!!.uid)
                        }
                        updateUIGuest(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("SIGNIN_X", "signInAnonymously:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_client_id))
            .requestEmail()
            .requestProfile()
            .requestScopes(Scope(Scopes.PLUS_ME))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInBtn.setOnClickListener {
            loginSo = ""
            najaviSeSoGoogle()
        }


        registerWelcomeBtn.setOnClickListener{
            //smeni view vo registracija
            val intent = Intent(this, RegisterActivity::class.java)

            startActivity(intent)
            finish()
        }
        loginWelcomeBtn.setOnClickListener{
            //smeni view vo registracija
            val intent = Intent(this, LoginActivity::class.java)

            startActivity(intent)
            finish()
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("FACEBOOK_SIGNIN", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("FACEBOOK_SIGNIN", "signInWithCredential:success")
                    val user = mAuth.currentUser

                    firebaseAnalytics.logEvent("logiran_facebook") {
                        param("user", user!!.uid)
                    }
                    updateUIGoogle(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("FACEBOOK_SIGNIN", "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUIGuest(user: FirebaseUser?) {
        if (user != null) {
            val momentalenKorisnik = mAuth.currentUser!!.uid

            refUsers = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference
                .child("users")
                .child(momentalenKorisnik)

            val userHashMap = HashMap<String, Any>()
            userHashMap["uid"] = momentalenKorisnik
            val momTimestamp : Long = java.sql.Timestamp(System.currentTimeMillis()).time

            userHashMap["status"] = "online"
            userHashMap["username"] = "u" + momTimestamp
            userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/chatly-f63cf.appspot.com/o/cover.jpg?alt=media&token=590ea2bd-1f43-40af-9f85-db1d44f3623f"
            userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/chatly-f63cf.appspot.com/o/m1.jpg?alt=media&token=14b1ea15-8f83-46ff-8edb-9e25b3060a38"
            userHashMap["search"] = "u" + momTimestamp
            userHashMap["facebook"] = "https://facebook.com"
            userHashMap["instagram"] = "https://instagram.com"
            userHashMap["website"] = "https://google.com"
            userHashMap["fullname"] = "u" + momTimestamp
            userHashMap["gender"] = "Male"
            userHashMap["dostapnost"] = 2
            userHashMap["gostin"] = 1
            userHashMap["kodPotvrda"] = ""

            refUsers.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    refUsers.updateChildren(userHashMap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                vleziVoMainActivity(this@WelcomeActivity)
                            }
                        }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
    }

    private fun najaviSeSoGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (loginSo == "facebook") {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        } else {
            //google e nema sho drugo
            if (requestCode == RC_SIGN_IN) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w("GOOGLE_SIGNIN", "Google sign in failed", e)
                }
            }

        }
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("GOOGLE_SIGNIN", "signInWithCredential:success")
                    val user = mAuth.currentUser
                    firebaseAnalytics.logEvent("logiran_google") {
                        param("user", user!!.uid)
                    }
                    updateUIGoogle(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("GOOGLE_SIGNIN", "signInWithCredential:failure", task.exception)
                    updateUIGoogle(null)
                }
            }
    }

    private fun updateUIGoogle(user: FirebaseUser?) {
        if (user != null) {
            val momentalenKorisnik = mAuth.currentUser!!.uid

            refUsers = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference
                .child("users")
                .child(momentalenKorisnik)

            val userHashMap = HashMap<String, Any>()
            userHashMap["uid"] = momentalenKorisnik

            userHashMap["status"] = "online"
            userHashMap["username"] = user.email.toString()
            userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/chatly-f63cf.appspot.com/o/cover.jpg?alt=media&token=590ea2bd-1f43-40af-9f85-db1d44f3623f"
            userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/chatly-f63cf.appspot.com/o/m1.jpg?alt=media&token=14b1ea15-8f83-46ff-8edb-9e25b3060a38"
            userHashMap["search"] = user.email.toString().lowercase(Locale.getDefault())
            userHashMap["facebook"] = "https://facebook.com"
            userHashMap["instagram"] = "https://instagram.com"
            userHashMap["website"] = "https://google.com"
            userHashMap["fullname"] = user.displayName.toString()
            userHashMap["gender"] = "Male"
            userHashMap["dostapnost"] = 0
            userHashMap["gostin"] = 0
            userHashMap["kodPotvrda"] = ""

            //proveri prvo dali postoi
            refUsers.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    if (!p0.exists())
                    {
                        Log.i("FACEBOOK_SIGNIN", "ne postoi")
                        Log.i("FACEBOOK_SIGNIN", userHashMap["fullname"].toString())
                        Log.i("FACEBOOK_SIGNIN", userHashMap["email"].toString())
                        Log.i("FACEBOOK_SIGNIN", userHashMap["uid"].toString())
                        refUsers.updateChildren(userHashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    vleziVoMainActivity(this@WelcomeActivity)
                                }
                        }
                    } else {
                        Log.i("FACEBOOK_SIGNIN", "postoi")

                        vleziVoMainActivity(this@WelcomeActivity)
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })

        } else {
            Log.i("FACEBOOK_SIGNIN", "null e")
        }
    }

    private fun vleziVoMainActivity(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }


    private fun setLocale(localeName: String) {
        Log.i("PROMENIV_JAZIK33", localeName)
        Log.i("PROMENIV_JAZIK44", currentLanguage)
        if (localeName != currentLanguage) {
            Log.i("PROMENIV_JAZIK1", localeName)
            locale = Locale(localeName)
            val sharedPreference =  getSharedPreferences("CHATX",Context.MODE_PRIVATE)
            var editor = sharedPreference.edit()
            editor.putString("jazik",localeName)
            editor.commit()

            val res = resources
            val dm = res.displayMetrics
            val conf = res.configuration
            conf.locale = locale
            res.updateConfiguration(conf, dm)
            val refresh = Intent(
                this,
                WelcomeActivity::class.java
            )
            refresh.putExtra("jazik", localeName)
            currentLanguage = localeName
            startActivity(refresh)
        } else {
//            Toast.makeText(
//                this@MainActivity, "Language, , already, , selected)!", Toast.LENGTH_SHORT).show();
        }
    }

    override fun onStart() {
        super.onStart()

        firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null) {
            val intentMain = Intent(this, MainActivity::class.java)
            val intentVerify = Intent(this, VerificationActivity::class.java)

            refUsers = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/")
                .reference
                .child("users")
                .child(firebaseUser!!.uid)

            refUsers.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val najavenKorisnik : Users? = p0.getValue(Users::class.java)

                        if (najavenKorisnik!!.getKodPotvrda() == "") {
                            Log.i("MAINACTIVITY", "kje vlezam")
                            startActivity(intentMain)
                            finish()
                        } else {
                            Log.i("MAINACTIVITY", "vo verify")

                            startActivity(intentVerify)
                            finish()
                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
            // ako vekje e najaven odi vo MainActivity direktno

        }
    }
}
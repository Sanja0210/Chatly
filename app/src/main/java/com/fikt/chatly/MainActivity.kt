package com.fikt.chatly

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.navigation.ui.AppBarConfiguration
import androidx.viewpager.widget.ViewPager
import com.fikt.chatly.Fragments.ChatsFragment
import com.fikt.chatly.Fragments.MessageChatFragment
import com.fikt.chatly.Fragments.SearchFragment
import com.fikt.chatly.Fragments.SettingsFragment
import com.fikt.chatly.ModelClasses.Chat
import com.fikt.chatly.ModelClasses.Users
import com.fikt.chatly.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), IListener {
    //varijabli firebase
    var refUsers : DatabaseReference? = null
    var firebaseUser : FirebaseUser? = null

    //varijabli ui komponenti
    private lateinit var usernameText : TextView
    private lateinit var profileImage : de.hdodenhof.circleimageview.CircleImageView
    lateinit var locale: Locale
    private var currentLanguage = "en"
    private var promeniHome : ImageView? = null
    private var promeniSearch : ImageView? = null
    private var promeniProfil : ImageView? = null
    private var qrCodeSlika : ImageView? = null
    private var promeniHomeAktivno : ImageView? = null
    private var promeniSearchAktivno : ImageView? = null
    private var promeniProfilAktivno : ImageView? = null
    private var fragmentHome : FragmentContainerView? = null
    private var fragmentSearch : FragmentContainerView? = null
    private var fragmentProfil : FragmentContainerView? = null

    //varijabli funkcionalnosti
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //deklariraj ui komponenti
        usernameText = findViewById(R.id.username_text)
        profileImage = findViewById(R.id.profile_image)
        qrCodeSlika = findViewById(R.id.qrCodeSlika)

//        if (qrCodeSlika != null) {
//            qrCodeSlika!!.setImageBitmap(getQrCodeBitmap())
//        }

        intent = intent

        if (intent.getStringExtra("jazik") != null) {
            currentLanguage = intent.getStringExtra("jazik").toString()
        }

        val sharedPreference =  getSharedPreferences("CHATX",Context.MODE_PRIVATE)

        val momJazik = sharedPreference.getString("jazik", "en").toString()
//        currentLanguage = momJazik
        setLocale(momJazik)


        Log.i("JAZIK", currentLanguage)

        //zemi korisnik firebase
        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUsers = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference.child("users").child(firebaseUser!!.uid)


        val toolbar : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        supportActionBar!!.title = ""
        var tabLayout : TabLayout? = null
        var viewPager : ViewPager? = null

        if (isTablet(applicationContext) == false) {
            tabLayout = findViewById(R.id.tabLayout)
            viewPager = findViewById(R.id.viewPager)
        }

        if (isTablet(applicationContext)) {
            promeniHome = findViewById(R.id.promeniHome)
            promeniSearch = findViewById(R.id.promeniSearch)
            promeniProfil = findViewById(R.id.promeniProfil)

            promeniHomeAktivno = findViewById(R.id.promeniHomeAktivno)
            promeniSearchAktivno = findViewById(R.id.promeniSearchAktivno)
            promeniProfilAktivno = findViewById(R.id.promeniProfilAktivno)

            fragmentHome = findViewById(R.id.chats_fragment_tablet)
            fragmentSearch = findViewById(R.id.search_fragment_tablet)
            fragmentProfil = findViewById(R.id.settings_fragment_taablet)

            promeniHome!!.setOnClickListener {
                fragmentHome!!.visibility = View.VISIBLE
                fragmentSearch!!.visibility = View.GONE
                fragmentProfil!!.visibility = View.GONE

                promeniHomeAktivno!!.visibility = View.VISIBLE
                promeniHome!!.visibility = View.GONE

                promeniProfilAktivno!!.visibility = View.GONE
                promeniProfil!!.visibility = View.VISIBLE

                promeniSearchAktivno!!.visibility = View.GONE
                promeniSearch!!.visibility = View.VISIBLE
            }

            promeniSearch!!.setOnClickListener {
                fragmentHome!!.visibility = View.GONE
                fragmentSearch!!.visibility = View.VISIBLE
                fragmentProfil!!.visibility = View.GONE


                promeniHomeAktivno!!.visibility = View.GONE
                promeniHome!!.visibility = View.VISIBLE

                promeniProfilAktivno!!.visibility = View.GONE
                promeniProfil!!.visibility = View.VISIBLE

                promeniSearchAktivno!!.visibility = View.VISIBLE
                promeniSearch!!.visibility = View.GONE
            }

            promeniProfil!!.setOnClickListener {
                fragmentHome!!.visibility = View.GONE
                fragmentSearch!!.visibility = View.GONE
                fragmentProfil!!.visibility = View.VISIBLE

                promeniHomeAktivno!!.visibility = View.GONE
                promeniHome!!.visibility = View.VISIBLE

                promeniProfilAktivno!!.visibility = View.VISIBLE
                promeniProfil!!.visibility = View.GONE

                promeniSearchAktivno!!.visibility = View.GONE
                promeniSearch!!.visibility = View.VISIBLE
            }
        }

        val ref = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference.child("chats")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
                var brojNeProcitaniPoraki = 0

                for (dataSnapshot in  p0.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)

                    Log.i("poraka", dataSnapshot.toString())

                    if (chat!!.getPrimac().equals(firebaseUser!!.uid) && !chat.getSeen()) {
                        Log.i("poraka", "vlegov brat");
                        brojNeProcitaniPoraki += 1
                    }
                }

                if (brojNeProcitaniPoraki == 0) {
                    viewPagerAdapter.addFragment(ChatsFragment(), getString(R.string.chats))
                } else {
                    viewPagerAdapter.addFragment(ChatsFragment(), "${getString(R.string.chats)} ($brojNeProcitaniPoraki)")
                }

                viewPagerAdapter.addFragment(SearchFragment(), getString(R.string.search))
                viewPagerAdapter.addFragment(SettingsFragment(), getString(R.string.account_settings))
                if (isTablet(applicationContext) == false) {
                    if (viewPager != null) {
                        viewPager.adapter = viewPagerAdapter
                    }
                    if (tabLayout != null) {
                        tabLayout.setupWithViewPager(viewPager)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        //prikazi informacii od najaven korisnik
        refUsers!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user : Users? = p0.getValue(Users::class.java)
                    usernameText.text = user!!.getFullname()
                    Log.i("PROFIL", user.getProfile().toString())
                    if (isTablet(applicationContext)) {
                        profileImage.setOnClickListener {
//                            setContentView()
                        }
                    }
                    if (user.getProfile() != "") {
                        Picasso.get().load(user.getProfile()).placeholder(R.drawable.ic_profile).into(profileImage)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

//        setLocale(momJazik)

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val angliskoPromena = menu!!.findItem(R.id.promeniJazikEn)
        val makedonskoPromena = menu!!.findItem(R.id.promeniJazikMk)

        if (currentLanguage == "mk") {
            angliskoPromena.setVisible(true)
            makedonskoPromena.setVisible(false)
        } else {
            makedonskoPromena.setVisible(true)
            angliskoPromena.setVisible(false)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                Firebase.auth.signOut()

                val intent = Intent(this, WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()

                return true
            }

            R.id.promeniJazikMk -> {
                setLocale("mk")
            }

            R.id.promeniJazikEn -> {
                setLocale("en")
            }
        }

        return false
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
                MainActivity::class.java
            )
            refresh.putExtra("jazik", localeName)
            currentLanguage = localeName
            startActivity(refresh)
        } else {
//            Toast.makeText(
//                this@MainActivity, "Language, , already, , selected)!", Toast.LENGTH_SHORT).show();
        }
    }
    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
        exitProcess(0)
    }

    fun odberiChatTablet() {
        if (intent.getStringExtra("idChat") != null) {
            val fragmentPoraka = supportFragmentManager.findFragmentById(R.id.detail_container) as MessageChatFragment?
            if (fragmentPoraka != null) {
                fragmentPoraka.kreirajViewPorakiChat(intent.getStringExtra("idChat").toString())
            }
        }
    }

    fun isTablet(ctx: Context): Boolean {
        return ctx.getResources()
            .getConfiguration().screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        return navController.navigateUp(appBarConfiguration)
//                || super.onSupportNavigateUp()
//    }

    internal class ViewPagerAdapter(fragmentManager: FragmentManager) : androidx.fragment.app.FragmentPagerAdapter(fragmentManager) {
        private val fragments : ArrayList<androidx.fragment.app.Fragment>
        private val titles : ArrayList<String>

        init {
            fragments = ArrayList<androidx.fragment.app.Fragment>()
            titles = ArrayList<String>()
        }

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragment(fragment: androidx.fragment.app.Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }

    private fun namestiStatus(status: String)
    {
        val ref = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference.child("users").child(firebaseUser!!.uid)

        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    ref!!.updateChildren(hashMap)
                }
             }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onResume() {
        super.onResume()

        namestiStatus("online")
    }

    override fun onPause() {
        super.onPause()

        namestiStatus("offline")
    }

    override fun onUserClickListener(user : Users?) {
        Log.i("BASHDAVIDAM", "vlegva")
        val fragmentPoraka = supportFragmentManager.findFragmentById(R.id.detail_container) as MessageChatFragment?
        if (user != null) {
            user.getUID()?.let {
                if (fragmentPoraka != null) {
                    fragmentPoraka.kreirajViewPorakiChat(it)
                }
            }
        }
    }

    fun getQrCodeBitmap(): Bitmap {
        val size = 512 //pixels
        val qrCodeContent = "https://google.com"
        val hints = hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 } // Make the QR code buffer border narrower
        val bits = QRCodeWriter().encode(qrCodeContent, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
}
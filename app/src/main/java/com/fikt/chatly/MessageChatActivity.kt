package com.fikt.chatly

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.*
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.fikt.chatly.AdapterClasses.ChatsAdapter
import com.fikt.chatly.Fragments.APIService
import com.fikt.chatly.Izvestuvanja.*
import com.fikt.chatly.ModelClasses.Chat
import com.fikt.chatly.ModelClasses.Chatlist
import com.fikt.chatly.ModelClasses.Users
import com.fikt.chatly.database.AppDatabase
import com.fikt.chatly.database.PorakaDb
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*


class MessageChatActivity : AppCompatActivity() {
    var idNaDrugiot : String = ""
    var firebaseKorisnik : FirebaseUser? = null
    private var slikaUri : Uri? = null
    private var slikaPath : String? = null
    var chatsAdapter: ChatsAdapter? = null
    var porakiLista: List<Chat>? = null
    private var dbReference : DatabaseReference? = null
    private var storageReference : StorageReference? = null
    var notify = false
    var apiService : APIService? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var slikaNaDrugiot = ""

    //deklariraj ui komponenti
    private lateinit var barLayout : AppBarLayout
    private lateinit var toolbarConv : Toolbar
    private lateinit var profilnaConv : CircleImageView
    private lateinit var korisnickoConv : TextView
    private lateinit var porakiListaRecycler : RecyclerView
    private lateinit var novaPorakaCelina : RelativeLayout
    private lateinit var prikaciFajlBtn : ImageView
    private lateinit var slikajBtn : ImageView
    private lateinit var ispratiPorakaBtn : CircleImageView
    private lateinit var novaPorakaEdit : EditText
    private lateinit var progressBar : ProgressBar
    private lateinit var recyclerPoraki : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

        firebaseAnalytics = Firebase.analytics

        val toolbar : Toolbar = findViewById(R.id.toolbar_chatlist)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_arrow)

        //custom event firebase
        toolbar.setNavigationOnClickListener {
            if (dbReference != null) {
                dbReference!!.removeEventListener(seenListener!!)
            }

            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)

        intent = intent
        idNaDrugiot = intent.getStringExtra("idNaDrugiot").toString()
        firebaseKorisnik = FirebaseAuth.getInstance().currentUser

        firebaseAnalytics.logEvent("otvoril_chat") {
            param("otvoril", firebaseKorisnik!!.uid)
            param("otvoreno_so", idNaDrugiot)
        }

        recyclerPoraki = findViewById(R.id.poraki_lista_recycler)
        recyclerPoraki.setHasFixedSize(true)
        recyclerPoraki.recycledViewPool.setMaxRecycledViews(0,0)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recyclerPoraki.layoutManager = linearLayoutManager

        val ref = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference
            .child("users").child(idNaDrugiot)
        //
        storageReference = FirebaseStorage.getInstance().reference.child("chat_images")
        //definiraj ui komponenti
        ispratiPorakaBtn = findViewById(R.id.isprati_poraka_button)
        novaPorakaEdit = findViewById(R.id.novaporaka_edit)
        korisnickoConv = findViewById(R.id.korisnicko_conv)
        profilnaConv = findViewById(R.id.profilna_conv)
        prikaciFajlBtn = findViewById(R.id.prikaci_fajl_conv)
        slikajBtn = findViewById(R.id.slikaj_conv)
        progressBar = findViewById(R.id.progressbar)

        progressBar.visibility = View.INVISIBLE

        slikajBtn.setOnClickListener {
            //intent to open camera app

            if (allPermissionsGranted()) {
                startCamera()
            } else {
                ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            }

        }

        //listeners
        //        --listener baza vlecenje na informacii za drugiot korisnik
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            // network is available for use
            override fun onAvailable(network: Network) {
                Log.i("KONEKCIJA", "available")

                super.onAvailable(network)

                ispratiNeisprateniPoraki()
            }

            // Network capabilities have changed for the network
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            }

            // lost network connection
            override fun onLost(network: Network) {
                Log.i("KONEKCIJA", "lost")
                super.onLost(network)
            }
        }

        val connectivityManager = getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)


        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val korisnik : Users? = p0.getValue(Users::class.java)

                korisnickoConv.text = korisnik!!.getUsername().toString()
                Picasso.get().load(korisnik.getProfile()).into(profilnaConv)

                korisnickoConv.setOnClickListener {
                    otvoriProfilActivity(idNaDrugiot)
                }
                profilnaConv.setOnClickListener {
                    otvoriProfilActivity(idNaDrugiot)
                }

                popolniPoraki(firebaseKorisnik!!.uid, idNaDrugiot, korisnik.getProfile())
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })

        //--attach na file
        prikaciFajlBtn.setOnClickListener {
            notify = true
            pickImage()
        }

        //--prakjanje na poraka
        ispratiPorakaBtn.setOnClickListener {
            notify = true
            val poraka = novaPorakaEdit.text.toString()
            Log.i("PORAKA", poraka)

            if (poraka != "") {
                ispratiPorakaDoKorisnik(firebaseKorisnik!!.uid, idNaDrugiot, poraka)
            }

            novaPorakaEdit.setText("")
        }

        seenPoraka(idNaDrugiot)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 291) {
            val imgFile: File = File(slikaPath)
            Toast.makeText(this@MessageChatActivity, "Uploading...", Toast.LENGTH_LONG).show()

            if (imgFile.exists()) {
//                Toast.makeText(this@MessageChatActivity, "Uploading 1...", Toast.LENGTH_LONG).show()

                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                slikaUri = getImageUri(bitmap, Bitmap.CompressFormat.JPEG, 70)

                prikaciSlikaBaza()
            }
        }
    }

    var resultLauncherCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                handleCameraImage(result.data)
//                    val data: Intent? = result.data
//                    Log.i("slika", data.toString())
//                    Log.i("slika", data!!.extras!!.get("data").toString())
//                    slikaUri = data!!.data
//
                Toast.makeText(this@MessageChatActivity, "Uploading...", Toast.LENGTH_LONG).show()
//                    prikaciSlikaBaza()
            }
        }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
//                finish()
            }
        }
    }

    fun startCamera() {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "$timeStamp.jpg"
        val storageDir: File = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        slikaPath = storageDir.getAbsolutePath().toString() + "/" + imageFileName
        val file = File(slikaPath)
//        val outputFileUri = Uri.fromFile(file)
        val outputFileUri = FileProvider.getUriForFile(
            this,
            "com.fikt.chatly.fileprovider",  //(use your app signature + ".provider" )
            file
        )

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
        startActivityForResult(cameraIntent, 291)

//        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        resultLauncherCamera.launch(cameraIntent)
    }


    fun getImageUri(src: Bitmap, format: CompressFormat?, quality: Int): Uri? {
        val os = ByteArrayOutputStream()
        src.compress(format, quality, os)
        val path = MediaStore.Images.Media.insertImage(contentResolver, src, "title", null)
        return Uri.parse(path)
    }

    private fun handleCameraImage(intent: Intent?) {
        val bitmap = intent?.extras?.get("data") as Bitmap
//        ivPhoto.setImageBitmap(bitmap)
        Log.i("Before Compress Dimension", bitmap.width.toString());
        Log.i("Before Compress Dimension", bitmap.height.toString());

        slikaUri = getImageUri(bitmap, Bitmap.CompressFormat.JPEG, 70)

        Log.i("SLIKANO", slikaUri.toString())

        prikaciSlikaBaza()
    }

    private fun ispratiNeisprateniPoraki() {
        val roomDb = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "chatx"
        ).allowMainThreadQueries().build()

        var k = roomDb.porakaDao().najdiPorakiChat(idNaDrugiot, FirebaseAuth.getInstance().currentUser!!.uid)

        for (porakaLokalno in k) {
            Log.i("PORAKA_LOKALNO", porakaLokalno.isprateno.toString())
            if (porakaLokalno.isprateno == false) {
                //ne samo prateno tuku se update

                //hashmap prateno = true
                Log.i("PORAKA_LOKALNO", porakaLokalno.poraka.toString())

                val neispratenaPorakaRef = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference.child("chats").child(porakaLokalno.porakaId)

                var enkripcija : Enkripcija
                enkripcija = Enkripcija()
                val enkriptiranaPoraka = porakaLokalno.poraka?.let { enkripcija.encrypt(it).toString() }


                val porakaIsprateno = HashMap<String, Any>()
                porakaIsprateno["prateno"] = true
                porakaIsprateno["isprakjac"] = porakaLokalno.isprakjac.toString()
                porakaIsprateno["primac"] = porakaLokalno.primac.toString()
                porakaIsprateno["seen"] = porakaLokalno.seen!!
                porakaIsprateno["poraka"] = enkriptiranaPoraka.toString()
                porakaIsprateno["porakaId"] = porakaLokalno.porakaId
                porakaIsprateno["url"] = porakaLokalno.url.toString()
                neispratenaPorakaRef.updateChildren(porakaIsprateno)
            }
        }

        popolniPoraki(FirebaseAuth.getInstance().currentUser!!.uid, idNaDrugiot, slikaNaDrugiot)
    }

    private fun dodajVoLokalna(poraka : Chat, isprateno: Boolean) {
        Log.i("PORAKA_LOKALNO", "kje dodavam so status $isprateno - ${poraka.getPorakaId()}")
        //ako tuka ja nema vo musers dodaj ja

        val roomDb = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "chatx"
        ).allowMainThreadQueries().build()

        var ispratenoInternet = isprateno

        if (isOnline(applicationContext)) ispratenoInternet = true

        if (poraka.getPorakaId() != null) {
            var k = roomDb.porakaDao().getAll()

//            for (porakaLokalno in k) {
                var najden = roomDb.porakaDao().searchFor(poraka.getPorakaId().toString())

                if (najden.size == 0) {
                    poraka.getPorakaId()?.let {
                        roomDb.porakaDao().insertAll(PorakaDb(it, poraka.getIsprakjac(), poraka.getPoraka(), poraka.getPrimac(), poraka.getSeen(), poraka.getUrl(), ispratenoInternet))
                    }
                } else {
                        var d = roomDb.porakaDao().namestiIsprateno(PorakaDb(poraka.getPorakaId()!!, poraka.getIsprakjac(), poraka.getPoraka(), poraka.getPrimac(), poraka.getSeen(), poraka.getUrl(), true))

                }
//            }

            if (k.size == 0) {
                poraka.getPorakaId()?.let {
                    roomDb.porakaDao().insertAll(PorakaDb(it, poraka.getIsprakjac(), poraka.getPoraka(), poraka.getPrimac(), poraka.getSeen(), poraka.getUrl(), ispratenoInternet))
                }
            }

            if (isOnline(applicationContext) == false) {
                Log.i("CITANJE_PORAKI", "offline e")
                //
                popolniPorakiLokalno(poraka.getIsprakjac()!!, poraka.getPrimac()!!, slikaNaDrugiot)
            }
        }

    }

    private fun popolniPorakiLokalno(isprakjac: String, primac: String, primacSlikaUrl: String?) {
//        if (primacSlikaUrl != null) {
//            slikaNaDrugiot = primacSlikaUrl
//        }

        Log.i("PORAKI_LOKALNO", "lokalno polnam")

        if (isOnline(applicationContext)) {
            Log.i("PORAKI_LOKALNO", "neam lokalno polnam")

            popolniPoraki(isprakjac, primac, primacSlikaUrl)
        } else {
            if (isprakjac != null && primac != null) {
                (porakiLista as ArrayList<Chat>).clear()

                val roomDb = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "chatx"
                ).allowMainThreadQueries().build()
                var k = roomDb.porakaDao().getAll()


                for (p in k) {
    //                val poraka = p.getValue(Chat::class.java)

                    val poraka =
                        p.isprakjac?.let { p.poraka?.let { it1 ->
                            p.primac?.let { it2 ->
                                p.seen?.let { it3 ->
                                    p.url?.let { it4 ->
                                        p.isprateno?.let { it5 ->
                                            Chat(it,
                                                it1, it2, it3, it4, p.porakaId, it5
                                            )
                                        }
                                    }
                                }
                            }
                        } }

                    if (poraka != null) {

                        Log.i("DATA", poraka.toString())

                        if ((poraka!!.getPrimac().equals(isprakjac) && poraka.getIsprakjac().equals(primac))
                            || poraka.getPrimac().equals(primac) && poraka.getIsprakjac().equals(isprakjac))
                        {
                            Log.i("PORAKA_POLNENJE", "polnam poraka ${poraka.getPoraka()} i pratena: ${poraka.getPrateno()} - ${poraka.getPorakaId()}")
                            (porakiLista as ArrayList<Chat>).add(poraka)
                        }
                        if (chatsAdapter != null) {
                            chatsAdapter!!.notifyDataSetChanged()
                        }
                    }

                }
            }
        }
    }

    private fun otvoriProfilActivity(idNaDrugiot: String) {
        val intent = Intent(this, VisitUserActivity::class.java)
        intent.putExtra("profilZaOtvoranje", idNaDrugiot)
        startActivity(intent)
    }


    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    private fun popolniPoraki(isprakjacId: String, primacId: String?, primacSlikaUrl: String?) {
        if (primacSlikaUrl != null) slikaNaDrugiot = primacSlikaUrl
        Log.i("PORAKA_LOCAL", "polnam")
        porakiLista = ArrayList()
        val reference = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference.child("chats")

        if (isOnline(applicationContext)) {
            reference.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(p0: DataSnapshot)
                {
                    if (isOnline(applicationContext)) {
                        (porakiLista as ArrayList<Chat>).clear()
                        for (snapshot in p0.children)
                        {
                            val poraka = snapshot.getValue(Chat::class.java)

                            Log.i("DATA", poraka.toString())

                            if ((poraka!!.getPrimac().equals(isprakjacId) && poraka.getIsprakjac().equals(primacId))
                                || poraka.getPrimac().equals(primacId) && poraka.getIsprakjac().equals(isprakjacId))
                            {

                                if (poraka.getPrateno() == false) {
//                                    val porakaIsprateno = HashMap<String, Any>()
//                                    porakaIsprateno["prateno"] = true
//
//                                    val neispratenaPorakaRef = poraka!!.getPorakaId()?.let {
//                                        FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference.child("chats").child(it).updateChildren(porakaIsprateno)
//
//                                    }
                                }
                                (porakiLista as ArrayList<Chat>).add(poraka)

                                dodajVoLokalna(poraka, true)

                            }
                            if (slikaNaDrugiot != null) {
                            chatsAdapter = ChatsAdapter(this@MessageChatActivity, (porakiLista as ArrayList<Chat>), slikaNaDrugiot!!, isOnline(this@MessageChatActivity))
                            recyclerPoraki.adapter = chatsAdapter

                            }
                        }
                    } else {
                        if (primacId != null) {
                            popolniPorakiLokalno(isprakjacId, primacId, primacSlikaUrl)
                        }
                    }

                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        } else {
            popolniPorakiLokalno(isprakjacId, primacId!!, primacSlikaUrl!!)
        }
}

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        resultLauncher.launch(intent)
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data

            slikaUri = data!!.data

            Toast.makeText(this@MessageChatActivity, "Uploading...", Toast.LENGTH_LONG).show()

            prikaciSlikaBaza()
        }
    }

    private fun prikaciSlikaBaza() {
        progressBar.visibility = View.VISIBLE

        Log.i("slika", slikaUri.toString())

        if (slikaUri != null) {
            val ref = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference
            val porakaId = ref.push().key
            val fileRef = storageReference!!.child(porakaId.toString() + ".jpg")

            val uploadTask = fileRef.putFile(slikaUri!!)

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val porakaSlikaHash = HashMap<String, Any?>()
                    porakaSlikaHash["isprakjac"] = firebaseKorisnik!!.uid
                    porakaSlikaHash["primac"] = idNaDrugiot
                    porakaSlikaHash["poraka"] = "sent you an image."
                    porakaSlikaHash["seen"] = false
                    porakaSlikaHash["url"] = url
                    porakaSlikaHash["porakaId"] = porakaId

                    dodajVoLokalna(Chat(porakaSlikaHash["isprakjac"].toString(), porakaSlikaHash["poraka"].toString(), porakaSlikaHash["primac"].toString(), false, porakaSlikaHash["url"].toString(), porakaSlikaHash["porakaId"].toString()), false)

                    Log.i("slika", url)

                    ref.child("chats").child(porakaId!!).setValue(porakaSlikaHash).addOnCompleteListener {task ->
                        if (task.isSuccessful) {
                            dodajVoLokalna(Chat(porakaSlikaHash["isprakjac"].toString(), porakaSlikaHash["poraka"].toString(), porakaSlikaHash["primac"].toString(), false, porakaSlikaHash["url"].toString(), porakaSlikaHash["porakaId"].toString()), true)

                            progressBar.visibility = View.INVISIBLE

                            firebaseAnalytics.logEvent("pratil_slika") {
                                param("pratil", FirebaseAuth.getInstance().currentUser!!.uid)
                                param("pratil_na", idNaDrugiot)
                            }

                            //push notifikacii
                            val reference = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference.child("users").child(firebaseKorisnik!!.uid)

                            reference.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(p0: DataSnapshot) {
                                    val korisnik = p0.getValue(Users::class.java)

                                    if (notify) {
                                        ispratiNotifikacija(idNaDrugiot, korisnik!!.getUsername(), "sent you an image.")
                                    }
                                    notify = false
                                }

                                override fun onCancelled(p0: DatabaseError) {

                                }

                            })
                        }
                    }

                }
            }
        }
    }

    private fun ispratiPorakaDoKorisnik(najavenKorisnik: String, idNaDrugiot: String, poraka: String) {
        val ref = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference
        val porakaKey = ref.push().key

        val porakaHashMap = HashMap<String, Any?>()
        var enkripcija : Enkripcija
        enkripcija = Enkripcija()
        val enkriptiranaPoraka = enkripcija.encrypt(poraka).toString()

        porakaHashMap["isprakjac"] = najavenKorisnik
        porakaHashMap["primac"] = idNaDrugiot
        porakaHashMap["poraka"] = enkriptiranaPoraka
        porakaHashMap["seen"] = false
        porakaHashMap["url"] = ""
        porakaHashMap["porakaId"] = porakaKey
        porakaHashMap["prateno"] = true

        if (isOnline(applicationContext)) {
            porakaHashMap["prateno"] = true
        } else {
            porakaHashMap["prateno"] = false
        }

        dodajVoLokalna(Chat(porakaHashMap["isprakjac"].toString(), porakaHashMap["poraka"].toString(), porakaHashMap["primac"].toString(), false, porakaHashMap["url"].toString(), porakaHashMap["porakaId"].toString()), false)

        if (isOnline(applicationContext)) {

            ref.child("chats").child(porakaKey!!).setValue(porakaHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        dodajVoLokalna(
                            Chat(
                                porakaHashMap["isprakjac"].toString(),
                                porakaHashMap["poraka"].toString(),
                                porakaHashMap["primac"].toString(),
                                false,
                                porakaHashMap["url"].toString(),
                                porakaHashMap["porakaId"].toString()
                            ), true
                        )

                        firebaseAnalytics.logEvent("pratil_tekst") {
                            param("pratil", FirebaseAuth.getInstance().currentUser!!.uid)
                            param("pratil_na", idNaDrugiot)
                        }

                        val chatsListReference =
                            FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/")
                                .reference
                                .child("chatList")
                                .child(firebaseKorisnik!!.uid)
                                .child(idNaDrugiot)

                        val momTimestamp = Timestamp(System.currentTimeMillis()).time

                        val chatListTvoj = Chatlist(idNaDrugiot, momTimestamp)
                        val chatListNadrug = Chatlist(firebaseKorisnik!!.uid, momTimestamp)

                        chatsListReference.addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(p0: DataSnapshot) {
                                if (!p0.exists()) {
                                    chatsListReference.setValue(chatListTvoj)
                                } else {
                                    chatsListReference.child("timestamp").setValue(momTimestamp)
                                }

                                val chatlistaNaDrugiotRef =
                                    FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/")
                                        .reference
                                        .child("chatList")
                                        .child(idNaDrugiot)
                                        .child(firebaseKorisnik!!.uid)

                                chatlistaNaDrugiotRef.setValue(chatListNadrug)
                            }

                            override fun onCancelled(p0: DatabaseError) {

                            }
                        })
                    }
                }
        } else {
            popolniPorakiLokalno(najavenKorisnik, idNaDrugiot, slikaNaDrugiot)
        }
        //push notifikacii
        val reference = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference.child("users").child(firebaseKorisnik!!.uid)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val korisnik = p0.getValue(Users::class.java)

                if (notify) {
                    Log.i("notifikacii", "kje prakjam 1")
                    ispratiNotifikacija(idNaDrugiot, korisnik!!.getUsername(), poraka)
                }
                notify = false
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

    private fun ispratiNotifikacija(idNaDrugiot: String, username: String?, poraka: String) {
        val ref = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference.child("tokens")
        Log.i("notifikacii", "kje prakam 2")

        val query = ref.orderByKey().equalTo(idNaDrugiot)

        query.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot)
            {
                for (dataSnapshot in p0.children)
                {
                    val token: Token? = dataSnapshot.getValue(Token::class.java)

                    val data = Data(
                        firebaseKorisnik!!.uid,
                        R.mipmap.ic_launcher,
                        "$username: $poraka",
                        "New Message",
                        idNaDrugiot
                    )

                    val isprakjac = Isprakjac(data!!, token!!.getToken().toString())

                    apiService!!.sendNotification(isprakjac)
                        .enqueue(object : retrofit2.Callback<IResponse>
                        {
                            override fun onResponse(
                                call: Call<IResponse>,
                                response: Response<IResponse>
                            )
                            {
                                if (response.code() == 200)
                                {
                                    Log.i("notifikacii", response.body().toString())
                                    if (response.body()!!.success !== 1)
                                    {
//                                        Toast.makeText(this@MessageChatActivity, getString(R.string.greskaProbaj), Toast.LENGTH_LONG).show()
                                    } else {
//                                        Toast.makeText(this@MessageChatActivity, "Sent.", Toast.LENGTH_LONG).show()

                                    }
                                }
                            }

                            override fun onFailure(call: Call<IResponse>, t: Throwable) {

                            }
                        })
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


    private var seenListener: ValueEventListener? = null

    private fun seenPoraka(korisnikId: String)
    {
        Log.i("seen ke napram", "seen")
        val reference = FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/").reference.child("chats")

        seenListener = reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot)
            {
                for (dataSnapshot in p0.children)
                {
                    val poraka = dataSnapshot.getValue(Chat::class.java)

                    if (poraka!!.getPrimac().equals(firebaseKorisnik!!.uid) && poraka.getIsprakjac().equals(korisnikId))
                    {
                        val hashMap = HashMap<String, Any>()
                        hashMap["seen"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    companion object {
        private const val TAG = "Chatx"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

//    override fun onPause() {
////        super.onPause()
////        dbReference!!.removeEventListener(seenListener!!)
//    }
}
package com.fikt.chatly

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SkenirajQRActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skeniraj_qractivity)
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)



        if (this != null) {
            codeScanner = CodeScanner(this, scannerView)
        }

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                val intent = Intent(this, MessageChatActivity::class.java)
                val intentMain = Intent(this, MainActivity::class.java)

                try {
                    Log.i("QRKOD_REZ", it.text)

                    val korisnikId = it.text.split("user/").toTypedArray()
                    Log.i("QRKOD_REZ", korisnikId[1].toString())
                    if (FirebaseAuth.getInstance().currentUser!!.uid != korisnikId[1]) {

                        val chatsListReference =
                            FirebaseDatabase.getInstance("https://chatly-f63cf-default-rtdb.firebaseio.com/")
                                .reference
                                .child("users")
                                .child(korisnikId[1])
                        chatsListReference.addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(p0: DataSnapshot) {
                                if (p0.exists()) {
                                    if (isTablet(this@SkenirajQRActivity)) {
                                        intentMain.putExtra("idChat", korisnikId[1])
                                        startActivity(intentMain)
                                        finish()
                                    } else {
                                        intent.putExtra("idNaDrugiot", korisnikId[1])
                                        startActivity(intent);
                                        finish()
                                    }
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "Invalid User",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    startActivity(intentMain)
                                    finish()
                                }
                            }

                            override fun onCancelled(p0: DatabaseError) {

                            }
                        })
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Invalid User",
                            Toast.LENGTH_LONG
                        ).show()
                        startActivity(intentMain)
                        finish()
                    }

//
                } catch (err : Error) {
                    Toast.makeText(this,"Invalid QR Code", Toast.LENGTH_LONG).show()
                    startActivity(intentMain)
                    finish()
                }
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
//                intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
            }
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                MessageChatActivity.REQUIRED_PERMISSIONS,
                MessageChatActivity.REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun allPermissionsGranted() = MessageChatActivity.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MessageChatActivity.REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()

                intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun startCamera() {
        codeScanner.startPreview()
    }

    fun isTablet(ctx: Context): Boolean {
        return ctx.getResources()
            .getConfiguration().screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intentMain = Intent(this, MainActivity::class.java)
        startActivity(intentMain)
        finish()
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
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

}
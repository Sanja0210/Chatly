package com.fikt.chatly

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.squareup.picasso.Picasso

class VidiCelosnaSlikaActivity : AppCompatActivity() {
    private var slikaImageView: ImageView? = null
    private var imageUrl: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vidi_celosna_slika)

//        intent = intent
        imageUrl = intent.getStringExtra("url").toString()
        slikaImageView = findViewById(R.id.slikaImageView)

        Picasso.get().load(imageUrl).into(slikaImageView)
    }
}
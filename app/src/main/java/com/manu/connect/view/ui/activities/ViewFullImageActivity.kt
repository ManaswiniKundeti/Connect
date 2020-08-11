package com.manu.connect.view.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.manu.connect.R
import com.squareup.picasso.Picasso

class ViewFullImageActivity : AppCompatActivity() {

    private var fullImageViewer : ImageView? = null
    private var imageUrl : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_full_image)

        imageUrl = intent.getStringExtra("url")
        fullImageViewer = findViewById(R.id.full_image_viewer)

        Picasso.get().load(imageUrl).into(fullImageViewer)

    }
}
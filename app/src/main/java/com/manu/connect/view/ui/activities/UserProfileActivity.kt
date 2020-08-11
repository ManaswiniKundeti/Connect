package com.manu.connect.view.ui.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.manu.connect.R
import com.manu.connect.model.Users
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_profile.*

class UserProfileActivity : AppCompatActivity() {

    private var visitedUserId : String = ""
    var user : Users? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        visitedUserId = intent.getStringExtra("visit_id")

        val dbReference = FirebaseDatabase.getInstance().reference.child("Users").child(visitedUserId)
        dbReference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    user = snapshot.getValue(Users::class.java)
                    profile_username.text = user!!.getUsername()
                    Picasso.get().load(user!!.getProfile()).into(profile_image)
                    Picasso.get().load(user!!.getCover()).into(profile_cover_image)
                }
            }
        })

        profile_facebook.setOnClickListener {
            val uri = Uri.parse(user!!.getFacebook())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        profile_instagram.setOnClickListener {
            val uri = Uri.parse(user!!.getInstagram())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        profile_website.setOnClickListener {
            val uri = Uri.parse(user!!.getWebsite())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        profile_send_message_button.setOnClickListener {
            val intent = Intent(this, ChatMessageActivity::class.java)
            intent.putExtra("visit_id", user!!.getUID())
            startActivity(intent)
        }
    }
}
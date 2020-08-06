package com.manu.connect.view.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.manu.connect.R
import com.manu.connect.model.Users
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.view.*

class SettingsFragment : Fragment() {

    private var usersReference : DatabaseReference? = null
    var firebaseUser : FirebaseUser? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
         val view : View = inflater.inflate(R.layout.fragment_settings, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        usersReference!!.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user : Users? = snapshot.getValue(Users::class.java)
                    if(context != null){
                        if (user != null) {
                            view.settings_username.text = user.getUsername()
                            Picasso.get().load(user.getProfile())
                                .placeholder(R.drawable.profile_image)
                                .into(view.settings_profile_image)
                            Picasso.get().load(user.getCover())
                                .placeholder(R.drawable.cover_image)
                                .into(view.settings_cover_image)

                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        return view
    }
}
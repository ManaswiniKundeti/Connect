package com.manu.connect.view.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.manu.connect.R
import com.manu.connect.model.Users
import com.manu.connect.view.adapter.ViewPagerAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    var referenceUsers : DatabaseReference? = null
    var firebaseUser : FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar_main)
        supportActionBar?.title = "" // as will be using image & username as title

        firebaseUser = FirebaseAuth.getInstance().currentUser
        referenceUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        setupViewPager()

        //display username and profile photo
        referenceUsers!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user : Users? = snapshot.getValue(Users::class.java)
                    username_main.text = user?.getUsername()
                    Picasso.get().load(user?.getProfile())
                        .placeholder(R.drawable.profile_image)
                        .into(profile_image_main)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun setupViewPager() {
        viewPagerAdapter = ViewPagerAdapter(this)

        view_pager_main.adapter = viewPagerAdapter
        TabLayoutMediator(tab_layout_main, view_pager_main, TabLayoutMediator.TabConfigurationStrategy{tab, position ->
            when(position){
                 0 -> tab.text = "CHATS"
                1 -> tab.text = "SEARCH"
                2 -> tab.text = "SETTINGS"
            }
        }).attach()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) : Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                return true
            }
        }
        return false
    }

}
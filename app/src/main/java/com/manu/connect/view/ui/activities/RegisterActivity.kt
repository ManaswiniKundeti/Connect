package com.manu.connect.view.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.manu.connect.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var referenceUsers : DatabaseReference
    private var firebaseUserID : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setSupportActionBar(toolbar_register)
        supportActionBar?.title = "Register"

        //back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_register.setNavigationOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()

        register_button.setOnClickListener{
            registerUser()
        }
    }


    private fun registerUser() {
        val username : String = username_register.text.toString()
        val email : String = email_register.text.toString()
        val password : String = password_register.text.toString()

        when {
            username == "" -> {
                Toast.makeText(this,"Username cannot be empty", Toast.LENGTH_SHORT).show()
            }
            email == "" -> {
                Toast.makeText(this,"Email cannot be empty", Toast.LENGTH_SHORT).show()
            }
            password == "" -> {
                Toast.makeText(this,"Password cannot be empty", Toast.LENGTH_SHORT).show()
            }
            else -> {
                mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener{ task ->
                        if(task.isSuccessful){
                            firebaseUserID = mAuth.currentUser!!.uid
                            referenceUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserID)

                            val userHashMap = HashMap<String, Any>()
                            userHashMap["uid"] = firebaseUserID
                            userHashMap["username"] = username
                            userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/connect-app-3164f.appspot.com/o/profile_image.png?alt=media&token=74191c6f-d777-4031-8f03-1effd71974fe"
                            userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/connect-app-3164f.appspot.com/o/cover_image.jpg?alt=media&token=6cecae49-a2a3-4809-8646-432901be3e79"
                            userHashMap["status"] = "offline"
                            userHashMap["search"] = username.toLowerCase()
                            userHashMap["facebook"] = "https://m.facebook.com"
                            userHashMap["instagram"] = "https://m.instagram.com"
                            userHashMap["website"] = "https://www.google.com"

                            referenceUsers.updateChildren(userHashMap)
                                .addOnCompleteListener{task ->
                                    if(task.isSuccessful){
                                        val intent = Intent(this, MainActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(intent)
                                        finish()
                                    }
                                }

                        }else{
                            Toast.makeText(this,"Error Message : "+ task.exception?.message.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }
}
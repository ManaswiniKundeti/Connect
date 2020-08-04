package com.manu.connect.view.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.manu.connect.R
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setSupportActionBar(toolbar_login)
        supportActionBar?.title = "Login"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //back button
        toolbar_login.setNavigationOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()

        login_button.setOnClickListener{
            loginUser()
        }

    }

    private fun loginUser() {
        val email : String = email_login.text.toString()
        val password : String = password_login.text.toString()

        when {
            email == "" -> {
                Toast.makeText(this,"Email cannot be empty", Toast.LENGTH_SHORT).show()
            }
            password == "" -> {
                Toast.makeText(this,"Password cannot be empty", Toast.LENGTH_SHORT).show()
            }
            else -> {
                mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener { task->
                        if(task.isSuccessful){
                            val intent = Intent(this, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        }else{
                            Toast.makeText(this,"Error Message : "+ task.exception?.message.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }
}
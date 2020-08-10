package com.manu.connect.notification

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService


/*
when A sends message to B, we need a unique token for each message
For each notificton, a separate ID is created in DB under Tokens node
 */

class MyFirebaseInstanceId : FirebaseMessagingService(){

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val refreshToken = FirebaseInstanceId.getInstance().token
        
        if(firebaseUser!= null){
            updatetoken(refreshToken)
        }
    }

    private fun updatetoken(refreshToken: String?) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token = Token(refreshToken!!)
        reference.child(firebaseUser!!.uid).setValue(token)
    }
}
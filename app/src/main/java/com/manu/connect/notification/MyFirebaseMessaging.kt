package com.manu.connect.notification

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.manu.connect.view.ui.activities.ChatMessageActivity

class MyFirebaseMessaging : FirebaseMessagingService(){

    override fun onMessageReceived(mRemoteMessage: RemoteMessage) {
        super.onMessageReceived(mRemoteMessage)

        val sent = mRemoteMessage.data["sent"]
        val user = mRemoteMessage.data["user"]

        val sharedPreference = getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)

        val currentOnlineUser = sharedPreference.getString("currentUser", "none")

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if(firebaseUser != null && sent == firebaseUser.uid){
            if(currentOnlineUser != user){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    //for Oreo notifications
                    sendOreoNotifications(mRemoteMessage)
                }else{
                    //for other version's
                    sendNotifications(mRemoteMessage)
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun sendNotifications(mRemoteMessage: RemoteMessage) {
        val user = mRemoteMessage.data["user"]
        val icon = mRemoteMessage.data["icon"]
        val title = mRemoteMessage.data["title"]
        val body = mRemoteMessage.data["body"]

        val notification = mRemoteMessage.notification
        val j = user!!.replace("[\\D]".toRegex(),"").toInt()
        val intent = Intent(this, ChatMessageActivity::class.java)

        val bundle = Bundle()
        bundle.putString("userId", user)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent =PendingIntent.getActivity(this, j , intent, PendingIntent.FLAG_ONE_SHOT)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder : NotificationCompat.Builder? = NotificationCompat.Builder(this)
            .setSmallIcon(icon!!.toInt())
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentIntent(pendingIntent)

        val notificationService = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var i = 0
        if(j > 0){
            i = j
        }

        notificationService.notify(i, builder!!.build())
    }

    private fun sendOreoNotifications(mRemoteMessage: RemoteMessage) {
        val user = mRemoteMessage.data["user"]
        val icon = mRemoteMessage.data["icon"]
        val title = mRemoteMessage.data["title"]
        val body = mRemoteMessage.data["body"]

        val notification = mRemoteMessage.notification
        val j = user!!.replace("[\\D]".toRegex(),"").toInt()
        val intent = Intent(this, ChatMessageActivity::class.java)

        val bundle = Bundle()
        bundle.putString("userId", user)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent =PendingIntent.getActivity(this, j , intent, PendingIntent.FLAG_ONE_SHOT)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val oreoNotification = OreoNotification(this)

        val builder : Notification.Builder = oreoNotification.getOreoNotification(title, body, pendingIntent, defaultSound, icon)

        var i = 0
        if(j > 0){
            i = j
        }
        oreoNotification.getManager!!.notify(i, builder.build())
    }
}
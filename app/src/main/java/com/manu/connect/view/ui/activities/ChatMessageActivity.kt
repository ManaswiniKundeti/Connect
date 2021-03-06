package com.manu.connect.view.ui.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.session.MediaSession
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.manu.connect.R
import com.manu.connect.extensions.hide
import com.manu.connect.extensions.show
import com.manu.connect.model.Chat
import com.manu.connect.model.NotificationData
import com.manu.connect.model.Users
import com.manu.connect.notification.Client
import com.manu.connect.notification.MyReponse
import com.manu.connect.notification.Sender
import com.manu.connect.notification.Token
import com.manu.connect.view.adapter.ChatAdapter
import com.manu.connect.view.ui.fragments.ApiService
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat_message.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatMessageActivity : AppCompatActivity() {

    var userIdVisit : String = ""
    var firebaseUser : FirebaseUser? = null
    var chatAdapter : ChatAdapter? = null
    var mChatList : List<Chat>? = null  //retrieve all chats and save messages in array list to send into ChatAdapter
    private var chatsRecyclerView : RecyclerView? = null  //late init var chatsRecyclerView : RecyclerView
    var reference : DatabaseReference? = null

    var notify = false
    var apiService : ApiService?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_message)

        val toolbar : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_chat_message)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(ApiService::class.java)

        intent = intent
        userIdVisit = intent.getStringExtra("visit_id")
        firebaseUser = FirebaseAuth.getInstance().currentUser

        chatsRecyclerView = findViewById(R.id.recycler_view_chat_message)
        chatsRecyclerView?.setHasFixedSize(true)

        var chatLayoutManager = LinearLayoutManager(this)
        chatLayoutManager.stackFromEnd = true
        chatsRecyclerView?.layoutManager = chatLayoutManager

        reference = FirebaseDatabase.getInstance().reference.child("Users").child(userIdVisit)

        //set receiver's profile pic and username from DB on the ChatMessage activty's AppBar
        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
               val user : Users? = snapshot.getValue(Users::class.java)
                username_chat_message.text = user!!.getUsername()
                Picasso.get().load(user.getProfile()).into(profile_image_chat_message)

                retrieveMessages(firebaseUser!!.uid, userIdVisit, user.getProfile())
            }
        })

        //when sender clicks on send button, sending message to receiver
        send_message_button.setOnClickListener {
            notify = true
            val message = text_message.text.toString()
            if(message == ""){
                Toast.makeText(this,"Please enter a message", Toast.LENGTH_LONG).show()
            }else{
                sendMessageToUser(firebaseUser!!.uid, userIdVisit, message)
            }
            text_message.setText("")
        }

        //when sender clicks on attachment button, opening local storage to pick image, send to receiver
        attach_image_button.setOnClickListener {
            notify = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Pick Image"), 438)
        }

        seenMessage(userIdVisit)
    }

    private fun retrieveMessages(senderId: String, receiverId: String?, receiverImageUrl: String) {
        mChatList = ArrayList()

        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        reference!!.addValueEventListener(object  : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot)
            {
                (mChatList as ArrayList<Chat>).clear()
                for(snapshotItem in snapshot.children)
                {
                    val chat = snapshotItem.getValue(Chat::class.java)
                    if(chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId)
                        || chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId))
                    {
                        (mChatList as ArrayList<Chat>).add(chat)
                    }
                    chatAdapter = ChatAdapter(this@ChatMessageActivity, mChatList as ArrayList<Chat>, receiverImageUrl!!)
                    chatsRecyclerView!!.adapter = chatAdapter
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun sendMessageToUser(senderId: String, receiverId: String?, message: String) {
        chat_message_progress_bar.show()
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String, Any>()
        messageHashMap["sender"] = senderId
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverId.toString()
        messageHashMap["isseen"] = false
        messageHashMap["url"] = ""    //if sending an image attachment as message, url will have firebase storage link to that image
        messageHashMap["messageId"] = messageKey.toString()

        reference.child("Chats")
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    //add receiver to sender's ChatList
                    val chatsListSenderReference = FirebaseDatabase.getInstance().reference
                        .child("ChatList")
                        .child(firebaseUser!!.uid)
                        .child(userIdVisit)

                    chatsListSenderReference.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(!snapshot.exists()){
                                chatsListSenderReference.child("id").setValue(userIdVisit)
                            }
                            //adding sender to receiver's ChatList
                            val chatsListReceiverReference = FirebaseDatabase.getInstance()
                                .reference.child("ChatList")
                                .child(userIdVisit)
                                .child(firebaseUser!!.uid)
                            chatsListReceiverReference.child("id").setValue(firebaseUser!!.uid)
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })


                }
            }
        chat_message_progress_bar.hide()
        //Implementing Push Notification
        val userReference = FirebaseDatabase.getInstance().reference
            .child("Users").child(firebaseUser!!.uid)
        userReference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)
                if(notify){
                    sendNotification(receiverId, user!!.getUsername(), message)
                }
                notify = false
            }
        })
    }

    private fun sendNotification(receiverId: String?, username: String, message: String) {
        val reference = FirebaseDatabase.getInstance().reference.child("Tokens")

        val query = reference.orderByKey().equalTo(receiverId)
        query.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snapshotItem in snapshot.children){
                    val token : Token? = snapshotItem.getValue(Token::class.java)
                    val data = NotificationData(firebaseUser!!.uid, R.mipmap.ic_launcher, "$username : $message", "New Message", userIdVisit)
                    val sender = Sender(data, token!!.getToken().toString())

                    apiService!!.sendNotification(sender)
                        .enqueue(object : Callback<MyReponse>{

                            override fun onFailure(call: Call<MyReponse>, t: Throwable) {
                            }

                            override fun onResponse(
                                call: Call<MyReponse>,
                                response: Response<MyReponse>
                            ) {
                                if(response.code() == 200){
                                    if(response.body()!!.success != 1){
                                        Toast.makeText(this@ChatMessageActivity, "Failed to send notification", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }

                        })
                }
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        chat_message_progress_bar.show()

        if(requestCode == 438 && resultCode == RESULT_OK && data != null && data!!.data != null) {
            val fileUri = data!!.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val reference = FirebaseDatabase.getInstance().reference
            val messageId = reference.push().key
            val filePath = storageReference.child("$messageId.jpg")

            var uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val messageHashMap = HashMap<String, Any>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["message"] = "Sent an image.."
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["isseen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageId.toString()

                    reference.child("Chats").child(messageId!!).setValue(messageHashMap)
                        .addOnCompleteListener{task ->
                            if(task.isSuccessful){
                                chat_message_progress_bar.hide()

                                //Implementing Push Notification
                                val reference = FirebaseDatabase.getInstance().reference
                                    .child("Users").child(firebaseUser!!.uid)
                                reference.addValueEventListener(object : ValueEventListener{
                                    override fun onCancelled(error: DatabaseError) {
                                    }

                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val user = snapshot.getValue(Users::class.java)
                                        if(notify){
                                            sendNotification(userIdVisit, user!!.getUsername(), "Sent an image..")
                                        }
                                        notify = false
                                    }

                                })
                            }
                        }

                }
            }
        }
    }

    var seenListener : ValueEventListener? = null

    private fun seenMessage(userId : String){
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        seenListener = reference.addValueEventListener(object  : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
            override fun onDataChange(snapshot: DataSnapshot) {
               for(snapshotItem in snapshot.children){
                   val chat = snapshotItem.getValue(Chat::class.java)

                   if(chat!!.getReceiver().equals(firebaseUser!!.uid) && chat!!.getSender().equals(userId)){
                       val hashMap = HashMap<String, Any>()
                       hashMap["isseen"] = true
                       snapshotItem.ref.updateChildren(hashMap)
                   }
               }
            }

        })
    }

    override fun onPause() {
        super.onPause()

        reference!!.removeEventListener(seenListener!!)
    }
}
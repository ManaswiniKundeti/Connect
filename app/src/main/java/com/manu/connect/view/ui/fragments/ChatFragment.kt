package com.manu.connect.view.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.manu.connect.R
import com.manu.connect.model.Chatlist
import com.manu.connect.model.Users
import com.manu.connect.notification.Token
import com.manu.connect.view.adapter.UserAdapter

class ChatFragment : Fragment() {

    private var userAdapter : UserAdapter? = null
    private var mUsers : List<Users>? = null
    private var mUsersChatList : List<Chatlist>? = null
    lateinit var chatListRecyclerView : RecyclerView
    private var firebaseUser : FirebaseUser? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_chat, container, false)

        chatListRecyclerView = view.findViewById(R.id.recycler_view_chat_fragment)
        chatListRecyclerView?.setHasFixedSize(true)
        chatListRecyclerView.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        mUsersChatList = ArrayList()

        val reference = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
        reference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) { }
            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsersChatList as ArrayList).clear()

                for(snapshotItem in snapshot.children){
                    val chatList = snapshotItem.getValue(Chatlist::class.java)
                    (mUsersChatList as ArrayList).add(chatList!!)
                }
                retrieveChatList()
            }

        })

        //For notifications
        updateToken(FirebaseInstanceId.getInstance().token)

        return view
    }

    private fun updateToken(token: String?) {
        val reference = FirebaseDatabase.getInstance().reference.child("Tokens")
        val curentToken = Token(token!!)
        reference.child(firebaseUser!!.uid).setValue(curentToken)
    }

    private fun retrieveChatList(){
        mUsers = ArrayList()

        var reference = FirebaseDatabase.getInstance().reference.child("Users")
        reference!!.addValueEventListener(object :ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList).clear()

                for(snapshotItem in snapshot.children){
                    val user = snapshotItem.getValue(Users::class.java)

                    for(eachChatList in mUsersChatList!!){
                        if(user!!.getUID() == eachChatList.getId()){
                            (mUsers as ArrayList).add(user!!)
                        }
                    }
                }
                userAdapter = UserAdapter(context!!, mUsers!! as ArrayList<Users>, true)
                chatListRecyclerView.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}
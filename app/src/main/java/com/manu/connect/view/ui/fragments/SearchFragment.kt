package com.manu.connect.view.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.manu.connect.R
import com.manu.connect.model.Users
import com.manu.connect.view.adapter.UserAdapter
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : Fragment() {

    private var userAdapter : UserAdapter? = null
    private var mUsers : List<Users>? = null
    private var mSearchEditText : EditText? = null
    private var mRecyclerView : RecyclerView? = null

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view : View = inflater.inflate(R.layout.fragment_search, container, false)

        mRecyclerView = view.findViewById(R.id.search_list)
        mRecyclerView?.setHasFixedSize(true)
        mRecyclerView?.layoutManager = LinearLayoutManager(context)

        mUsers = ArrayList()
        retrieveAllUsers()

        mSearchEditText = view.findViewById(R.id.search_users_edit_text)
        mSearchEditText!!.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchUser(charSequence.toString().toLowerCase())
            }

        })

        return view
    }

    private fun retrieveAllUsers() {
        var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val refUsers = FirebaseDatabase.getInstance().reference.child("Users")

        refUsers.addValueEventListener(object : ValueEventListener {

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()
                if(mSearchEditText?.text.toString() == ""){
                    for(currentSnapshot in snapshot.children){
                        val user : Users? = currentSnapshot.getValue(Users::class.java)
                        if(user!!.getUID() != firebaseUserID){
                            (mUsers as ArrayList<Users>).add(user)
                        }
                    }
                    userAdapter = UserAdapter(context!!, mUsers!!, false)
                    mRecyclerView!!.adapter = userAdapter
                }
            }

        })
    }

    private fun searchUser(name : String){
        var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val queryUsers = FirebaseDatabase.getInstance().reference
            .child("Users")
            .orderByChild("search")
            .startAt(name)
            .endAt(name + "\uf8ff")

        queryUsers.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()

                for(currentsSnapshot in snapshot.children){
                    val user : Users? = currentsSnapshot.getValue(Users::class.java)
                    if(user!!.getUID() != firebaseUserID){
                        (mUsers as ArrayList<Users>).add(user)
                    }
                }
                userAdapter = UserAdapter(context!!, mUsers!!, false)
                mRecyclerView!!.adapter = userAdapter
            }

        })


    }
}
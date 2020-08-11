package com.manu.connect.view.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.manu.connect.R
import com.manu.connect.extensions.hide
import com.manu.connect.extensions.show
import com.manu.connect.model.Chat
import com.manu.connect.model.Users
import com.manu.connect.view.ui.activities.ChatMessageActivity
import com.manu.connect.view.ui.activities.MainActivity
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.search_item.view.*

class UserAdapter(mContext: Context, mUsers : List<Users>, isChatCheck : Boolean) : RecyclerView.Adapter<ItemViewHolder>() {

    private val mContext : Context = mContext
    private val mUsers : List<Users> = mUsers
    private val isChatCheck : Boolean = isChatCheck
    var lastmessage : String? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemViewHolder {
        val view : View = LayoutInflater.from(mContext).inflate(R.layout.search_item, viewGroup, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val user : Users = mUsers[position]
        holder.bindView(user)

        //To display latest message in the chatlist view
        if(isChatCheck){
            retrieveLastMessage(user.getUID(), holder.lastMessageTextView)
        }else{
            holder.lastMessageTextView.hide()
        }

        //to have online/offline color ball icon
        if(isChatCheck){
            if(user.getStatus() == "online"){
                holder.onlineImageView.show()
                holder.offlineImageView.hide()
            }else{
                holder.onlineImageView.hide()
                holder.offlineImageView.show()
            }
        }else{
            holder.onlineImageView.hide()
            holder.offlineImageView.hide()
        }

        //to show a different dialog option for chats and search fragment items
        holder.itemView.setOnClickListener {
            if(isChatCheck){
                val intent = Intent(mContext, ChatMessageActivity::class.java)
                intent.putExtra("visit_id", user.getUID())
                mContext.startActivity(intent)
            }else{
                val options = arrayOf<CharSequence>(
                    "Send Message",
                    "Visit Profile"
                )
                val builder : AlertDialog.Builder = AlertDialog.Builder(mContext)
                builder.setTitle("What do you want?")
                builder.setItems(options, DialogInterface.OnClickListener { dialog, position ->
                    if(position == 0){
                        val intent = Intent(mContext, ChatMessageActivity::class.java)
                        intent.putExtra("visit_id", user.getUID())
                        mContext.startActivity(intent)
                    }
                    if(position == 1){
                        //go to their profile
                    }
                })
                builder.show()
            }
        }
    }

    private fun retrieveLastMessage(onlineUserId: String, lastMessageTexView: TextView) {

        lastmessage = "Default Last Message"

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val dbReference = FirebaseDatabase.getInstance().reference.child("Chats")

        dbReference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snapshotItem in snapshot.children){
                    val chat : Chat? = snapshotItem.getValue(Chat::class.java)
                    if(firebaseUser != null && chat != null){
                        if(chat.getReceiver() == firebaseUser!!.uid &&
                            chat.getSender() == onlineUserId ||
                            chat.getReceiver() == onlineUserId &&
                            chat.getSender() == firebaseUser!!.uid){
                            lastmessage = chat.getMessage()
                        }
                    }
                }
                when(lastmessage){
                    "Default Last Message" -> lastMessageTexView.text = "No Message"
                    "Sent an image.." -> lastMessageTexView.text = "image sent"
                    else -> lastMessageTexView.text = lastmessage
                }
                lastmessage = "Default Last Message"
            }
        })

    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

}
class ItemViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        var onlineImageView : CircleImageView = itemView.findViewById(R.id.image_online_search_item)
        var offlineImageView : CircleImageView = itemView.findViewById(R.id.image_offline_search_item)
        var lastMessageTextView : TextView = itemView.findViewById(R.id.last_message_search_item)

    fun bindView(user : Users){
        itemView.username_search_item.text = user.getUsername()

        Picasso.get().load(user.getProfile())
            .placeholder(R.drawable.profile_image)
            .into(itemView.profile_image_search_item)

    }
}
package com.manu.connect.view.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.manu.connect.R
import com.manu.connect.extensions.hide
import com.manu.connect.extensions.show
import com.manu.connect.model.Chat
import com.manu.connect.view.ui.activities.ViewFullImageActivity
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter(
    mContext: Context,
    mChatlist: List<Chat>,
    imageUrl: String
) : RecyclerView.Adapter<ChatItemViewHolder>(){

    private val mContext : Context = mContext
    private val mChatlist : List<Chat> = mChatlist
    private val imageUrl : String = imageUrl
    var firebaseUser : FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ChatItemViewHolder {
        //1 -position of right message; 0 - position of left
        return if(position == 1){
            val view : View = LayoutInflater.from(mContext).inflate(R.layout.message_item_right,parent,false)
            ChatItemViewHolder(view)
        }else{
            val view : View = LayoutInflater.from(mContext).inflate(R.layout.message_item_left,parent,false)
            ChatItemViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return mChatlist.size
    }

    override fun onBindViewHolder(holder: ChatItemViewHolder, position: Int) {
        val chat: Chat = mChatlist[position]

        Picasso.get().load(imageUrl).into(holder.profile_image_left)

        //For Image Messages
        if(chat.getMessage().equals("Sent an image..") && !chat.getUrl().equals("")){
            if(chat.getSender().equals(firebaseUser.uid)){
                //right side - SENDER
                holder.chat_message_text_view.hide()
                holder.image_message_right?.show()
                Picasso.get().load(chat.getUrl()).into(holder.image_message_right)

                //Operations on Image Message
                holder.image_message_right!!.setOnClickListener {
                    val options = arrayOf(
                        "View Full Image",
                        "Delete Image",
                        "Cancel"
                    )
                    var builder : AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Select Operation")
                    builder.setItems(options, DialogInterface.OnClickListener { dialogInterface, which ->
                        if(which == 0){
                            val intent = Intent(mContext, ViewFullImageActivity::class.java)
                            intent.putExtra("url", chat.getUrl())
                            mContext.startActivity(intent)
                        }else if(which == 1){
                            deleteSentMessage(position,holder)
                        }
                    })
                    builder.show()
                }

            } else if (!chat.getSender().equals(firebaseUser!!.uid)){
                //left side - RECEIVER
                holder.chat_message_text_view.hide()
                holder.image_message_left?.show()
                Picasso.get().load(chat.getUrl()).into(holder.image_message_left)

                //Operations on Image Message
                holder.image_message_left!!.setOnClickListener {
                    val options = arrayOf(
                        "View Full Image",
                        "Cancel"
                    )
                    var builder : AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Select Operation")
                    builder.setItems(options, DialogInterface.OnClickListener { dialogInterface, which ->
                        if(which == 0){
                            val intent = Intent(mContext, ViewFullImageActivity::class.java)
                            intent.putExtra("url", chat.getUrl())
                            mContext.startActivity(intent)
                        }
                    })
                    builder.show()
                }
            }
        }else{
            //For Text Messages
            holder.chat_message_text_view.text = chat.getMessage()

            //Operation on Text Messages

            //sender[the logged in user] can only delete the messages sent by him
            if(firebaseUser.uid!! == chat.getSender()){
                holder.chat_message_text_view!!.setOnClickListener {
                    val options = arrayOf(
                        "Delete Message",
                        "Cancel"
                    )
                    var builder : AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Select Operation")
                    builder.setItems(options, DialogInterface.OnClickListener { dialogInterface, which ->
                        if(which == 0){
                            deleteSentMessage(position,holder)
                        }
                    })
                    builder.show()
                }
            }
        }

        //sent and seen messages
        if(position == mChatlist.size - 1){
            if(chat.getIsseen()!!){
                holder.seen_text_view.text = "Seen"
                //if it is an image message, move seen text view a little
                if(chat.getMessage().equals("Sent an image..") && !chat.getUrl().equals("")){
                    val lp : ConstraintLayout.LayoutParams? = holder.seen_text_view.layoutParams as ConstraintLayout.LayoutParams?
                    lp!!.setMargins(0,245,10, 0)
                    holder.seen_text_view.layoutParams = lp
                }
            }else{
                holder.seen_text_view.text = "Sent"
                //if it is an image message, move sent text view a little
                if(chat.getMessage().equals("Sent an image..") && !chat.getUrl().equals("")){
                    val lp : ConstraintLayout.LayoutParams? = holder.seen_text_view.layoutParams as ConstraintLayout.LayoutParams?
                    lp!!.setMargins(0,245,10, 0)
                    holder.seen_text_view.layoutParams = lp
                }
            }
        }else{
            //if there are 0 messages
            holder.seen_text_view.hide()
        }
    }

    override fun getItemViewType(position: Int): Int {
        //if sender if == firebaseUID, then current user is the online user -> SENDER -> so will use right side -> 1
        return  if(mChatlist[position].getSender().equals(firebaseUser.uid)){
            1
        }else{
            0
        }

    }

    private fun deleteSentMessage(position : Int, viewHolder: ChatItemViewHolder){
        val dbReference = FirebaseDatabase.getInstance().reference.child("Chats")
            .child(mChatlist[position].getMessageId()!!)
            .removeValue()
            .addOnCompleteListener { task->
                if(task.isSuccessful){
                    Toast.makeText(viewHolder.itemView.context, "Message Deleted", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(viewHolder.itemView.context, "Error deleting message", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

class ChatItemViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
{
    var profile_image_left : CircleImageView = itemView.findViewById(R.id.profile_image_message_item_left)
    var chat_message_text_view : TextView = itemView.findViewById(R.id.chat_message)
    var image_message_left : ImageView? = itemView.findViewById(R.id.image_view_left)
    var seen_text_view : TextView = itemView.findViewById(R.id.seen_text_view)
    var image_message_right : ImageView? = itemView.findViewById(R.id.image_view_right_message_item)
}

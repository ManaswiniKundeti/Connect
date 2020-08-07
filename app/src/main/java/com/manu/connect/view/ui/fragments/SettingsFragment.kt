package com.manu.connect.view.ui.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.manu.connect.R
import com.manu.connect.extensions.hide
import com.manu.connect.extensions.show
import com.manu.connect.model.Users
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*

class SettingsFragment : Fragment() {

    private var usersReference : DatabaseReference? = null
    var firebaseUser : FirebaseUser? = null
    private var imageUri : Uri? = null
    private var storageReference : StorageReference? = null
    private var imageTypeCheck : String? = null
    private var socialLinkTypeCheck : String? = null

    companion object {
        private const val REQUEST_CODE = 438
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
         val view : View = inflater.inflate(R.layout.fragment_settings, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storageReference = FirebaseStorage.getInstance().reference.child("User Images")

        usersReference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user : Users? = snapshot.getValue(Users::class.java)
                    if(context != null){
                        if (user != null) {
                            view.settings_username.text = user.getUsername()
                            Picasso.get().load(user.getProfile())
                                .placeholder(R.drawable.profile_image)
                                .into(view.settings_profile_image)
                            Picasso.get().load(user.getCover())
                                .placeholder(R.drawable.cover_image)
                                .into(view.settings_cover_image)

                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        view.settings_profile_image.setOnClickListener {
            pickImage()
        }

        view.settings_cover_image.setOnClickListener {
            imageTypeCheck = "cover"
            pickImage()
        }

        view.settings_facebook.setOnClickListener {
            socialLinkTypeCheck = "facebook"
            setSocialLinks()
        }

        view.settings_instagram.setOnClickListener {
            socialLinkTypeCheck = "instagram"
            setSocialLinks()
        }

        view.settings_website.setOnClickListener {
            socialLinkTypeCheck = "website"
            setSocialLinks()
        }

        return view
    }

    private fun setSocialLinks() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_AppCompat_Dialog_Alert)

        if(socialLinkTypeCheck == " website"){
            builder.setTitle("Write URL : ")
        }else{
            builder.setTitle("Write username : ")
        }

        val editText = EditText(context)
        if(socialLinkTypeCheck == " website"){
            editText.hint = "e.g : www.google.com"
        }else{
            editText.hint = "e.g mkundeti1"
        }
        builder.setView(editText)
        builder.setPositiveButton("Create",DialogInterface.OnClickListener{
            dialog, which ->
            val str = editText.text.toString()

            if(str == ""){
                Toast.makeText(context, "Please write something ...", Toast.LENGTH_LONG).show()
            }else{
                saveSocialLink(str)
            }
        })
        builder.setNegativeButton("Cancel",DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })
        builder.show()
    }

    private fun saveSocialLink(string: String) {
        val mapSocial = HashMap<String, Any>()
        when(socialLinkTypeCheck){
            "facebook" -> {
                mapSocial["facebook"] = "https://m.facebook.com/$string"
            }
            "instagram" -> {
                mapSocial["instagram"] = "https://m.instagram.com/$string"
            }
            "website" -> {
                mapSocial["website"] = "https://www.google.com"
            }
        }
        usersReference!!.updateChildren(mapSocial).addOnCompleteListener {
            task->
            if(task.isSuccessful){
               Toast.makeText(context, "Saved Social Links",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data!!.data != null){
            imageUri = data.data
            Toast.makeText(context, "Uploading Image ...", Toast.LENGTH_LONG).show()
            uploadImageToFirebaseDatabase()
        }
    }

    //upload image to firebase db
    private fun uploadImageToFirebaseDatabase() {
        settings_progress_bar.show()

        if(imageUri != null){
            val fileReference = storageReference!!.child(System.currentTimeMillis().toString() + ".jpg")
            var uploadTask : StorageTask<*>
            uploadTask = fileReference.putFile(imageUri!!)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>>{task ->
                if(!task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation fileReference.downloadUrl
            }).addOnCompleteListener { task->
                if(task.isSuccessful){
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    if(imageTypeCheck == "cover"){
                        val mapCoverImage = HashMap<String, Any>()
                        mapCoverImage["cover"] = url
                        usersReference!!.updateChildren(mapCoverImage)
                        imageTypeCheck = ""
                    }else {
                        val mapProfileImage = HashMap<String, Any>()
                        mapProfileImage["profile"] = url
                        usersReference!!.updateChildren(mapProfileImage)
                        imageTypeCheck = ""
                    }
                    settings_progress_bar.hide()
                }
            }
        }
    }
}
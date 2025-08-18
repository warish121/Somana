package com.example.anew

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.files.BackendlessFile
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import java.io.File
import com.example.anew.DataClass.BannerBack
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.apply


class Profile_ : AppCompatActivity() {


    private lateinit var databaseReference: DatabaseReference
    private lateinit var name: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        name = findViewById(R.id.username)
        databaseReference = Firebase.database.reference
        val userId = Firebase.auth.currentUser?.uid
        databaseReference.child("users").child(userId.toString()).get()
            .addOnSuccessListener {
                val user = it.child("name").value.toString()
                name.text = user
            }
            .addOnFailureListener {
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()

            }

        loadBannerImage()
        loadProfile()


        val goAllpost = findViewById<CardView>(R.id.All_Post)
        goAllpost.setOnClickListener {
            startActivity(Intent(this, All_Post::class.java))
        }


        findViewById<ImageView>(R.id.changeProfile).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            profilePicker.launch(intent)
        }

        findViewById<CardView>(R.id.uploadbanner).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            bannerPicker.launch(intent)
        }

        findViewById<CardView>(R.id.upload_Post).setOnClickListener {
            startActivity(Intent(this, Upload_Post::class.java))
        }


    }

    private val profilePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data!!

                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )


                uploadToBackendlessImage(uri, "Profile") { backendlessUrl ->


                    // Update the profile image in Firebase
                    val userId = Firebase.auth.currentUser?.uid ?: return@uploadToBackendlessImage
                    databaseReference.child("users").child(userId).child("profileImage")
                        .setValue(backendlessUrl)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show()

                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT)
                                .show()
                        }


                }


            }
        }


    private val bannerPicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data!!

                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )


                //Uploading image to backendless

                uploadToBackendlessImage(uri, "Banner") { backendlessUrl ->
                    //uploading img to firebase database
                    val bannerbach = databaseReference
                        .child("users")
                        .child(Firebase.auth.currentUser!!.uid)
                        .child("BannerImg")
                        .push()
                    bannerbach.setValue(BannerBack(backendlessUrl))
                        .addOnCompleteListener {
                            Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show()
                            loadBannerImage()

                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "No", Toast.LENGTH_SHORT).show()
                        }


                }


            }
        }


    private fun uploadToBackendlessImage(
        uri: Uri,
        folderName: String,
        onSuccess: (String) -> Unit
    ) {
        try {
            val file = uriToFile(uri) ?: run {
                Toast.makeText(this, "Could not get file from URI", Toast.LENGTH_SHORT).show()
                return
            }


            Backendless.Files.upload(
                file,
                folderName,
                true,
                object : AsyncCallback<BackendlessFile> {
                    override fun handleResponse(response: BackendlessFile?) {
                        runOnUiThread {
                            response?.fileURL?.let { url ->
                                saveImageUrl(url, (folderName == "").toString())
                                onSuccess(url) // Callback with the Backendless URL
                                Toast.makeText(
                                    this@Profile_,
                                    "$folderName upload successful!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun handleFault(fault: BackendlessFault) {
                        runOnUiThread {
                            Toast.makeText(
                                this@Profile_,
                                "$folderName upload failed: ${fault.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            Log.e("UploadError", e.stackTraceToString())
        }
    }


    // Helper to save URL to SharedPreferences
    private fun saveImageUrl(url: String, type: String) {
        val sharedPref = getSharedPreferences("BackendlessImages", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(type, url) // "profile_images" or "banner_images"
            apply()
        }
    }


    // Helper to convert Uri to File
    private fun uriToFile(uri: Uri): File? {
        return when (uri.scheme) {
            "content" -> {
                try {
                    val tempFile = File.createTempFile("upload", ".temp", cacheDir)
                    contentResolver.openInputStream(uri)?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    tempFile
                } catch (_: Exception) {
                    null
                }
            }

            "file" -> File(uri.path!!)
            else -> null
        }
    }


    //fetching image from firebase database
    private fun loadBannerImage() {
        val bannerRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(Firebase.auth.currentUser!!.uid)
            .child("BannerImg")

        // Get the most recent banner (last pushed entry)
        bannerRef.orderByKey().limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (bannerSnapshot in snapshot.children) {
                        val bannerUrl =
                            bannerSnapshot.child("bannerImg").getValue(String::class.java)
                        bannerUrl?.let { url ->
                            Toast.makeText(this@Profile_, "Ho gaya", Toast.LENGTH_SHORT).show()
                            Glide.with(this@Profile_)
                                .load(url)
                                .into(findViewById(R.id.bannerimg))


                        } ?: run {
                            Toast.makeText(this@Profile_, "No banner found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Profile_, "Failed to load banner", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }


    private fun loadProfile() {
        val profileRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(Firebase.auth.currentUser!!.uid)
            .child("ProfileImg") // Make sure this matches your database exactly

        // Get the most recent profile image
        profileRef.orderByKey().limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (profileSnapshot in snapshot.children) {
                        val profileBack =
                            profileSnapshot.child("profileImg").getValue(String::class.java)
                        profileBack?.let { url ->
                            Glide.with(this@Profile_)
                                .load(url)
                                .into(findViewById(R.id.profileback))
                        } ?: run {
                            Toast.makeText(
                                this@Profile_,
                                "No profile image found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@Profile_,
                        "Failed to load profile: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }


}



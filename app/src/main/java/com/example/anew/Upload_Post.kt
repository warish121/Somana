package com.example.anew


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.files.BackendlessFile
import com.example.anew.DataClass.Post
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import java.io.File

class Upload_Post : AppCompatActivity() {


    private lateinit var databaseReference: DatabaseReference
    private lateinit var create: TextView
    private lateinit var uploadbtn: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload_post)

        val backtoprofile = findViewById<ImageView>(R.id.backArrow)
        backtoprofile.setOnClickListener {
            startActivity(Intent(this, Profile_::class.java))
        }


        uploadbtn = findViewById<TextView>(R.id.uploadtext)
        create = findViewById<TextView>(R.id.createnewpost)
        databaseReference = Firebase.database.reference



        uploadbtn.setOnClickListener {
            ImagePicker.with(this)
                .provider(ImageProvider.BOTH)

                .cropFreeStyle()
                .createIntentFromDialog { launcher.launch(it) }


        }


    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val uri = it.data?.data!!

                val postImage = findViewById<ImageView>(R.id.uploadedPost)
                val uploadthepost = findViewById<TextView>(R.id.uploadtopost)
                val captiontext = findViewById<EditText>(R.id.captionText)

                captiontext.visibility = View.VISIBLE
                postImage.visibility = View.VISIBLE
                uploadthepost.visibility = View.VISIBLE

                postImage.setImageURI(uri)
                uploadbtn.visibility = View.GONE
                create.visibility = View.GONE

                // Take persistable permission (important for Android 11+)
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                uploadthepost.setOnClickListener {
                    // Upload to Backendless first
                    uploadImage(uri, "Post") { backendlessUrl ->
                        // Only after successful upload, save to Firebase
                        val userPostsRef = databaseReference
                            .child("users")
                            .child(Firebase.auth.currentUser!!.uid)
                            .child("posts")
                            .push()

                        userPostsRef.setValue(Post(backendlessUrl))
                            .addOnCompleteListener {
                                Toast.makeText(
                                    this,
                                    "Post uploaded successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Failed to save post: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            }
        }

    private fun uploadImage(uri: Uri, folderName: String, onSuccess: (String) -> Unit) {
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
                                saveImageUrl(url, (folderName == "Post").toString())
                                onSuccess(url) // Callback with the Backendless URL
                                Toast.makeText(
                                    this@Upload_Post,
                                    "$folderName upload successful!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun handleFault(fault: BackendlessFault) {
                        runOnUiThread {
                            Toast.makeText(
                                this@Upload_Post,
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
                    val tempFile = File.createTempFile("upload_", ".tmp", cacheDir)
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


}
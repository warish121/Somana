package com.example.anew

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.example.anew.DataClass.BannerBack
import com.example.anew.DataClass.Users
import com.example.anew.databinding.ActivityProfileBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.*
import java.io.File

class Profile_ : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var name: TextView
    private lateinit var binding: ActivityProfileBinding
    private lateinit var users: Users
    private var isFollowing = false
    private var followersCount = 0
    private var followingCount = 0
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        name = findViewById(R.id.username)
        databaseReference = Firebase.database.reference
        currentUserId = Firebase.auth.currentUser?.uid ?: ""

        // Get user ID from intent (for visited profiles) or fallback to current user
        val userId = intent.getStringExtra("USER_ID") ?: currentUserId

        if (userId.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Use this userId for all database operations
        databaseReference.child("users").child(userId).get()
            .addOnSuccessListener {
                val user = it.child("name").value.toString()
                name.text = user
            }
            .addOnFailureListener {
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }

        // Initialize users object with the correct ID
        users = Users(uid = userId)

        // Check if this is current user's profile or someone else's
        val isCurrentUserProfile = userId == currentUserId

        // Show/hide edit controls based on profile ownership
        findViewById<ImageView>(R.id.changeProfile).visibility =
            if (isCurrentUserProfile) View.VISIBLE else View.GONE
        findViewById<CardView>(R.id.uploadbanner).visibility =
            if (isCurrentUserProfile) View.VISIBLE else View.GONE
        findViewById<CardView>(R.id.upload_Post).visibility =
            if (isCurrentUserProfile) View.VISIBLE else View.GONE

        // Load user data including followers
        loadUserData(userId)
        loadBannerImage(userId)
        loadProfile(userId)
        setupBottomNavigation(userId)

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
                    // Update the profile image in Firebase for current user
                    databaseReference.child("users").child(currentUserId).child("profileImage")
                        .setValue(backendlessUrl)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show()
                            // Reload current user's profile image
                            loadProfile(currentUserId)
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

                // Uploading image to backendless
                uploadToBackendlessImage(uri, "Banner") { backendlessUrl ->
                    // Uploading img to firebase database for current user
                    val bannerbach = databaseReference
                        .child("users")
                        .child(currentUserId)
                        .child("BannerImg")
                        .push()
                    bannerbach.setValue(BannerBack(backendlessUrl))
                        .addOnCompleteListener {
                            Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show()
                            loadBannerImage(currentUserId)
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

    // Fetching image from firebase database
    private fun loadBannerImage(userId: String) {
        val bannerRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(userId)  // Use the passed userId
            .child("BannerImg")

        // Get the most recent banner (last pushed entry)
        bannerRef.orderByKey().limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (bannerSnapshot in snapshot.children) {
                        val bannerUrl =
                            bannerSnapshot.child("bannerImg").getValue(String::class.java)
                        bannerUrl?.let { url ->
                            Toast.makeText(this@Profile_, "Banner loaded", Toast.LENGTH_SHORT).show()
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

    private fun loadProfile(userId: String) {
        val profileRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(userId)  // Use the passed userId
            .child("profileImage") // Make sure this matches your database exactly

        // Get the profile image
        profileRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl = snapshot.getValue(String::class.java)
                profileImageUrl?.let { url ->
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

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@Profile_,
                    "Failed to load profile: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupBottomNavigation(userId: String) {
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Set home as selected initially
        bottomNavView.selectedItemId = R.id.profile

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.profile -> {
                    // Already on home, do nothing or refresh
                    true
                }
                R.id.home -> {
                    val intent = Intent(this, Home_Page::class.java)
                    startActivity(intent)
                    true
                }
                R.id.search -> {
                    val intent = Intent(this, Search_View::class.java)
                    startActivity(intent)
                    true
                }
                R.id.setting -> {
                    val intent = Intent(this, Setting::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }


        // Use the passed userId for follow functionality
        val ownerId = userId
        checkedIfFollowing(ownerId)

        binding.followback.setOnClickListener {
            toggleFollowing(ownerId)
        }
    }

    private fun loadUserData(userId: String) {
        databaseReference.child("users").child(userId).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(Users::class.java)
                    user?.let {
                        users = it.copy(uid = userId) // Ensure UID is set
                        binding.username.text = user.name ?: "Unknown User"

                        // Load followers count
                        loadFollowersCount(userId)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Profile_, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun checkedIfFollowing(ownerId: String) {
        val currentUser = auth.currentUser?.uid ?: return

        // Don't show follow button for own profile
        if (currentUser == ownerId) {
            binding.followback.visibility = View.GONE
            return
        } else {
            binding.followback.visibility = View.VISIBLE
            binding.changprofile.visibility = View.GONE
            binding.uploadbanner.visibility = View.GONE
            binding.uploadPost.visibility = View.GONE
        }

        databaseReference.child("users").child(ownerId).child("followers").child(currentUser)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isFollowing = snapshot.exists()
                    updateFollowersButton()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Profile_, "Failed to check follow status", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun toggleFollowing(ownerId: String) {
        val currentUser = auth.currentUser?.uid ?: return
        val followRef = databaseReference.child("users").child(ownerId).child("followers").child(currentUser)

        if (isFollowing) {
            // Unfollow - remove from followers
            followRef.removeValue()
                .addOnSuccessListener {
                    isFollowing = false
                    updateFollowersButton()
                    loadFollowersCount(ownerId) // Refresh count
                    Toast.makeText(this@Profile_, "Unfollowed", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this@Profile_, "Failed to unfollow", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Follow - add to followers
            followRef.setValue(true)
                .addOnSuccessListener {
                    isFollowing = true
                    updateFollowersButton()
                    loadFollowersCount(ownerId) // Refresh count
                    Toast.makeText(this@Profile_, "Followed", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this@Profile_, "Failed to follow", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadFollowersCount(ownerId: String) {
        databaseReference.child("users").child(ownerId).child("followers")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount.toInt()
                    binding.Followers.text = count.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Profile_, "Failed to load followers count", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateFollowersButton() {
        binding.followertext.text = if (isFollowing) "Following" else "Follow"
    }
}
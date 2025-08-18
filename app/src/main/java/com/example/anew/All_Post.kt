package com.example.anew

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.GridView
import android.widget.Toast
import com.bumptech.glide.Glide  // Add Glide for image loading
import com.example.anew.DataClass.Post
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class All_Post : AppCompatActivity() {
    private lateinit var gridView: GridView
    private lateinit var postAdapter: PostGridAdapter
    private val postList = mutableListOf<String>()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_all_post2)

        auth = FirebaseAuth.getInstance()
        gridView = findViewById(R.id.postsGridView)

        postAdapter = PostGridAdapter(this, postList)
        gridView.adapter = postAdapter

        loadPosts()
    }

    private fun loadPosts() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val databaseReference = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(currentUser.uid)
            .child("posts")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear()

                // Check if posts exist
                if (!snapshot.exists()) {
                    Toast.makeText(this@All_Post, "No posts found", Toast.LENGTH_SHORT).show()
                    return
                }

                // Iterate through each post
                for (postSnapshot in snapshot.children) {
                    try {
                        // Get the postImg URL from each post
                        val postImgUrl = postSnapshot.child("postImg").getValue(String::class.java)

                        postImgUrl?.let { url ->
                            if (url.startsWith("http")) {
                                postList.add(url)
                                Log.d("ImageLoad", "Added URL: $url")
                            } else {
                                Log.e("ImageLoad", "Invalid URL format: $url")
                            }
                        } ?: run {
                            Log.e("ImageLoad", "postImg is null for post: ${postSnapshot.key}")
                        }
                    } catch (e: Exception) {
                        Log.e("ImageLoad", "Error parsing post: ${e.message}")
                    }
                }

                // Update adapter
                postAdapter.notifyDataSetChanged()

                // If no valid URLs were found
                if (postList.isEmpty()) {
                    Toast.makeText(this@All_Post, "No valid image URLs found", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@All_Post,
                    "Failed to load posts: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("FirebaseError", "Database error: ${error.message}")
            }
        })
    }
}
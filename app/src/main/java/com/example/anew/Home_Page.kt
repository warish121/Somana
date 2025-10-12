package com.example.anew

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.anew.Adapter.PostAdapter
import com.example.anew.DataClass.Post
import com.example.anew.DataClass.Users
import com.example.anew.databinding.ActivityHomePageBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Home_Page : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var postadapter: PostAdapter
    private val userList = mutableListOf<Users>()
    private val profileList = mutableListOf<String>()
    private val postList = mutableListOf<Post>()
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Setup UI
        setupRecyclerView()
        setupBottomNavigation()

        // Check authentication
        if (auth.currentUser == null) {
            Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Fetch all users
        loadPosts()
        setupExceptionHandler()
    }

    private fun setupRecyclerView() {
        postadapter = PostAdapter(userList, profileList, postList) { user ->
            navigateToProfile(user)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = postadapter

        // Add scroll protection
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (layoutManager.findLastCompletelyVisibleItemPosition() == postadapter.itemCount - 1) {
                    // Reached end of list - prevent overscrolling
                    recyclerView.stopScroll()
                }
            }
        })
    }

    private fun setupBottomNavigation() {
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Set home as selected initially
        bottomNavView.selectedItemId = R.id.home

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    // Already on home, do nothing or refresh
                    true
                }
                R.id.profile -> {
                    val intent = Intent(this, Profile_::class.java)
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
    }

    private fun loadPosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("users")
        var hasAnyPosts = false

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear existing data
                userList.clear()
                profileList.clear()
                postList.clear()
                hasAnyPosts = false

                // First check if any posts exist in the database
                snapshot.children.forEach { userSnapshot ->
                    if (userSnapshot.child("posts").children.count() > 0) {
                        hasAnyPosts = true
                        return@forEach
                    }
                }

                // If posts exist, load them
                snapshot.children.forEach { userSnapshot ->
                    try {
                        val user = userSnapshot.getValue(Users::class.java) ?: return@forEach
                        val profileImg =
                            userSnapshot.child("profileImage").getValue(String::class.java) ?: ""
                        val postsNode = userSnapshot.child("posts")

                        if (postsNode.exists()) {
                            postsNode.children.forEach { postSnapshot ->
                                try {
                                    val postImg =
                                        postSnapshot.child("postImg").getValue(String::class.java)
                                    if (postImg != null) {
                                        userList.add(user)
                                        profileList.add(profileImg)
                                        postList.add(Post(PostImg = postImg))
                                    }
                                } catch (e: Exception) {
                                    Log.e("PostLoad", "Error loading post: ${e.message}")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("UserLoad", "Error loading user: ${e.message}")
                    }
                }

                // Update UI based on loaded data
                if (postList.isNotEmpty()) {
                    postadapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Home_Page, "Failed to load posts", Toast.LENGTH_SHORT).show()
                Log.e("FirebaseError", "Database error: ${error.message}")
            }
        })
    }

    private fun setupExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Crash: ${throwable.javaClass.simpleName}\n${throwable.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("APP_CRASH", "Crash details:", throwable)
            }
            // Wait to ensure toast shows
            Thread.sleep(3000)
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    private fun navigateToProfile(user: Users) {
        val intent = Intent(this, Profile_::class.java).apply {
            putExtra("USER_ID", user.uid)
            putExtra("USER_NAME", user.name)
        }
        startActivity(intent)
    }
    override fun onBackPressed() {
        // Double tap to exit
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finishAffinity() // Close the app completely
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }
}
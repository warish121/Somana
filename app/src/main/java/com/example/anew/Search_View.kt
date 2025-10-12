package com.example.anew

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.anew.Adapter.SearchAdapter
import com.example.anew.DataClass.Users
import com.example.anew.databinding.ActivitySearchViewBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Search_View : AppCompatActivity() {

    private lateinit var binding: ActivitySearchViewBinding
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private val userList = mutableListOf<Users>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchViewBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupBottomNavigation()

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        // Initialize adapter with empty list first
        searchAdapter = SearchAdapter(emptyList()).apply {
            onItemClick = { user ->
                navigateToUserProfile(user)
            }
        }
        binding.userlist.layoutManager = LinearLayoutManager(this)
        binding.userlist.adapter = searchAdapter

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchAdapter.filter(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchAdapter.filter(it) }
                return true
            }
        })

        loadUsers()
    }

    private fun loadUsers() {
        val postsRef = databaseReference.child("users")
        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                snapshot.children.forEach { userSnapshot ->
                    val user = userSnapshot.getValue(Users::class.java)
                    user?.let {
                        userList.add(it)
                    }
                }
                searchAdapter.updateList(userList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@Search_View,
                    "Failed to load users: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("FirebaseError", "Database error: ${error.message}")
            }
        })
    }

    private fun navigateToUserProfile(user: Users) {
        val intent = Intent(this, Profile_::class.java).apply {
            putExtra("USER_ID", user.uid)  // This should match what Profile_ expects
            putExtra("USER_NAME", user.name)
        }
        startActivity(intent)
    }
    private fun setupBottomNavigation() {
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Set home as selected initially
        bottomNavView.selectedItemId = R.id.search

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.search -> {
                    // Already on home, do nothing or refresh
                    true
                }
                R.id.profile -> {
                    val intent = Intent(this, Profile_::class.java)
                    startActivity(intent)
                    true
                }
                R.id.home -> {
                    val intent = Intent(this, Home_Page::class.java)
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
}
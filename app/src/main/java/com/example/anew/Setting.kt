package com.example.anew

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.anew.databinding.ActivitySettingBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class Setting : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var nametext: TextView
    private lateinit var binding: ActivitySettingBinding
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize binding FIRST
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root) // This is crucial!

        binding.back.setOnClickListener {
            val intent = Intent(this, Home_Page::class.java)
            startActivity(intent)
        }



        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        databaseReference = Firebase.database.reference
        currentUserId = Firebase.auth.currentUser?.uid ?: ""
        val userId = intent.getStringExtra("USER_ID") ?: currentUserId

        // Now you can use binding to access views
        nametext = binding.username // Use binding instead of findViewById

        // Load user data
        databaseReference.child("users").child(userId).get()
            .addOnSuccessListener {
                val name = it.child("name").value.toString()
                binding.username.text = name
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load user data: ${it.message}", Toast.LENGTH_SHORT).show()
            }

        // Set up logout button using binding
        binding.loginOutBtn.setOnClickListener {
            Firebase.auth.signOut()

            val intent = Intent(this, Log_In::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

            Toast.makeText(this, "LogOut Successful", Toast.LENGTH_SHORT).show()
        }
    }
}
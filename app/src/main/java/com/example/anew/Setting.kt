package com.example.anew

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.anew.DataClass.Users
import com.example.anew.databinding.ActivitySettingBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class Setting : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var user: Users
    private lateinit var nametext: TextView
    private lateinit var binding: ActivitySettingBinding
    private lateinit var currentUserId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        databaseReference = Firebase.database.reference
        currentUserId = Firebase.auth.currentUser?.uid ?: ""
        val userId = intent.getStringExtra("USER_ID") ?: currentUserId

        nametext = findViewById(R.id.username)

        databaseReference.child("users").child(userId).get()
            .addOnSuccessListener {
                val name = it.child("name").value.toString()
                val email = it.child("email").value.toString()
                binding.emailtext.text = email
                binding.username.text = name

            }
            .addOnFailureListener {
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }

        user = Users(uid = userId)

        val logout = findViewById<Button>(R.id.loginOut_btn)

        logout.setOnClickListener {
            Firebase.auth.signOut()

            val intent = Intent(this, Log_In::class.java)
            startActivity(intent)

            Toast.makeText(this, "LogOut Successfull", Toast.LENGTH_SHORT).show()
        }

    }
}
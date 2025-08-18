package com.example.anew

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.anew.databinding.ActivityLogInBinding


import com.google.firebase.auth.FirebaseAuth

class Log_In : AppCompatActivity() {
    private val binding: ActivityLogInBinding by lazy {
        ActivityLogInBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, Home_Page::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        binding.logintosignup.setOnClickListener {
            startActivity(Intent(this, Sign_Up::class.java))
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.emailLogin.text.toString()
            val password = binding.passwordLogin.text.toString()

            if (email.isEmpty() && password.isEmpty()) {
                Toast.makeText(this, "Please Enter all fields", Toast.LENGTH_SHORT).show()

            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Successfully Logged In", Toast.LENGTH_SHORT)
                                .show()
                            startActivity(Intent(this, Home_Page::class.java))
                        } else {
                            Toast.makeText(this, "Log In failed", Toast.LENGTH_SHORT).show()
                        }

                    }
            }

        }


    }
}
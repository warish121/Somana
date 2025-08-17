package com.example.anew

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.anew.DataClass.Users
import com.example.anew.databinding.ActivitySignUpBinding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Sign_Up : AppCompatActivity() {
    private val binding: ActivitySignUpBinding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)



        // Initialize database reference to "users" node
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        auth = FirebaseAuth.getInstance()

        binding.signuptologin.setOnClickListener {
            startActivity(Intent(this, Log_In::class.java))
        }

        binding.signupBtn.setOnClickListener {
            val userName = binding.usernamesignup.text.toString()
            val Email = binding.emailSign.text.toString()
            val SignPassword = binding.passwordSignup.text.toString()
            val confirmpass = binding.ConfirmpasswordSignupn.text.toString()

            if(userName.isEmpty() || Email.isEmpty() || SignPassword.isEmpty() || confirmpass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
            else if(SignPassword != confirmpass) {
                Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
            }
            else {
                auth.createUserWithEmailAndPassword(Email, SignPassword)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            startActivity(Intent(this, Home_Page::class.java))
                            finish()
                            val currentUser = auth.currentUser
                            currentUser?.let { user ->
                                // Create user data object
                                val dataItem = Users(userName)

                                // Save user data under their UID
                                databaseReference.child(user.uid).setValue(dataItem)
                                    .addOnCompleteListener { dbTask ->
                                        if(dbTask.isSuccessful) {
                                            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()

                                        } else {
                                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                                            Log.e("FIREBASE_DB_ERROR", "Failed to save user data", dbTask.exception)
                                        }
                                    }
                            }
                        } else {
                            val exception = task.exception
                            val errorMessage = when (exception) {
                                is FirebaseAuthWeakPasswordException -> "Password is too weak."
                                is FirebaseNetworkException -> "Network error occurred. Please check your connection."
                                is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                                is FirebaseAuthUserCollisionException -> "Email already in use."
                                else -> "Registration failed: ${exception?.message ?: "Unknown error"}"
                            }
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                            Log.e("FIREBASE_ERROR", "Registration failed", exception)
                        }
                    }
            }
        }
    }
}
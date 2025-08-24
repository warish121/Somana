package com.example.anew

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.anew.DataClass.Users
import com.example.anew.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Sign_Up : AppCompatActivity() {
    private val binding: ActivitySignUpBinding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Initialize Firebase components
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        configureGoogleSignIn()

        binding.signuptologin.setOnClickListener {
            startActivity(Intent(this, Log_In::class.java))
        }

        binding.googleSignin.setOnClickListener {
            signInWithGoogle()
        }

        binding.signupBtn.setOnClickListener {
            val userName = binding.usernamesignup.text.toString()
            val Email = binding.emailSign.text.toString()
            val SignPassword = binding.passwordSignup.text.toString()
            val confirmpass = binding.ConfirmpasswordSignupn.text.toString()

            if (userName.isEmpty() || Email.isEmpty() || SignPassword.isEmpty() || confirmpass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (SignPassword != confirmpass) {
                Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
            } else {
                createUserWithEmailAndPassword(userName, Email, SignPassword)
            }
        }
    }

    private fun configureGoogleSignIn() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize the Google Sign-In launcher
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleGoogleSignInResult(task)
            } else {
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account?.let {
                // Get the user's name from Google account
                val userName = it.displayName ?: "Google User"
                val email = it.email ?: ""
                firebaseAuthWithGoogle(it.idToken!!, userName, email)
            }
        } catch (e: ApiException) {
            Log.w(TAG, "Google sign in failed", e)
            Toast.makeText(this, "Google Sign-In failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, userName: String, email: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    user?.let {
                        // Save user data to Realtime Database with the actual name
                        saveUserToDatabase(userName, it.uid)
                        startActivity(Intent(this, Home_Page::class.java))
                        finish()
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createUserWithEmailAndPassword(userName: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    currentUser?.let { user ->
                        saveUserToDatabase(userName, user.uid)
                        startActivity(Intent(this, Home_Page::class.java))
                        finish()
                    }
                } else {
                    handleRegistrationError(task.exception)
                }
            }
    }

    private fun saveUserToDatabase(userName: String, uid: String) {
        val dataItem = Users(
            name = userName,
            uid = uid



        )

        databaseReference.child(uid).setValue(dataItem)
            .addOnCompleteListener { dbTask ->
                if (dbTask.isSuccessful) {
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "User saved to database: $userName, $uid")
                } else {
                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                    Log.e("FIREBASE_DB_ERROR", "Failed to save user data", dbTask.exception)
                }
            }
    }

    private fun handleRegistrationError(exception: Exception?) {
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

    companion object {
        private const val TAG = "Sign_Up"
    }
}
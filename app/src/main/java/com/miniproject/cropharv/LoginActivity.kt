package com.miniproject.cropharv

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot

class LoginActivity : AppCompatActivity() {

    private lateinit var mEmail: EditText
    private lateinit var mPass: EditText
    private lateinit var signInBtn: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Change status bar icon color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        mEmail = findViewById(R.id.leditTextEmail)
        mPass = findViewById(R.id.leditTextPassword)
        signInBtn = findViewById(R.id.circularButton)

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance() // Initialize Firestore

        signInBtn.setOnClickListener { loginUser() }

        changeStatusBarColor()
    }

    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.login_bk_color)
        }
    }

    private fun loginUser() {
        val email = mEmail.text.toString().trim()
        val pass = mPass.text.toString()

        if (email.isEmpty()) {
            mEmail.error = "Empty Fields Are not Allowed"
            mEmail.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.error = "Please Enter Correct Email"
            mEmail.requestFocus()
            return
        }
        if (pass.isEmpty()) {
            mPass.error = "Empty Fields Are not Allowed"
            mPass.requestFocus()
            return
        }

        mAuth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // On successful login, fetch user details from Firestore
                    val currentUser = mAuth.currentUser?.uid
                    if (currentUser != null) {
                        fetchUserInfoFromFirestore(email)
                    } else {
                        Toast.makeText(this@LoginActivity, "Error: User not found!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Login Failed !!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to fetch user info from Firestore
    private fun fetchUserInfoFromFirestore(loggedInEmail: String) {
        val usersCollectionRef = firestore.collection("Users")

        // Query Firestore where the "email" field matches the logged-in email
        usersCollectionRef
            .whereEqualTo("email", loggedInEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Retrieve the first document that matches the email
                    val document = querySnapshot.documents[0]

                    // Extract user information from the document
                    val username = document.getString("name")
                    val email = document.getString("email")
                    val phoneNumber = document.getString("mobile")
                    Log.d("log","Logged in")
                    Log.d("LoginActivity", "Username: $username, Email: $email, Phone: $phoneNumber")
                    // Pass user info to HomeActivity using Intent
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    intent.putExtra("username", username)
                    intent.putExtra("email", email)
                    intent.putExtra("phoneNumber", phoneNumber)
                    startActivity(intent)
                    finish()
                } else {
                    // No user found with the matching email
                    Toast.makeText(
                        this@LoginActivity,
                        "No user data found for the provided email!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                // Handle error
                Toast.makeText(
                    this@LoginActivity,
                    "Failed to fetch user data: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun onLoginClick(view: View) {
        startActivity(Intent(this, RegisterActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay)
    }
}

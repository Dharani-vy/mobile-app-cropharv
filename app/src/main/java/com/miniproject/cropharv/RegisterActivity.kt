package com.miniproject.cropharv

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var mEmail: EditText
    private lateinit var mPass: EditText
    private lateinit var username: EditText
    private lateinit var phone: EditText
    private lateinit var signUpBtn: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mEmail = findViewById(R.id.reditTextEmail)
        mPass = findViewById(R.id.reditTextPassword)
        username = findViewById(R.id.reditTextName)
        phone = findViewById(R.id.reditTextMobile)
        signUpBtn = findViewById(R.id.cirRegisterButton)
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        signUpBtn.setOnClickListener { newUser() }
        changeStatusBarColor()
    }

    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.register_bk_color)
        }
    }

    private fun newUser() {
        val email = mEmail.text.toString().trim()
        val pass = mPass.text.toString()
        val name = username.text.toString()
        val mobile = phone.text.toString()

        if (name.isEmpty()) {
            username.error = "Enter the full name"
            username.requestFocus()
            return
        }
        if (mobile.isEmpty()) {
            phone.error = "Enter the mobile number"
            phone.requestFocus()
            return
        }
        val phonePattern = "^[+]?[0-9]{10,13}$"  // Adjust the pattern as needed
        if (!mobile.matches(phonePattern.toRegex())) {
            phone.error = "Please enter a valid mobile number"
            phone.requestFocus()
            return
        }
        if (email.isEmpty()) {
            mEmail.error = "Empty Fields Are not Allowed"
            mEmail.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.error = "Please enter the Email Correctly!"
            mEmail.requestFocus()
            return
        }
        if (pass.isEmpty()) {
            mPass.error = "Empty Fields Are not Allowed"
            mPass.requestFocus()
            return
        }
        val passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"

        // Validate password format
        if (!pass.matches(passwordPattern.toRegex())) {
            mPass.error = "Password must be at least 8 characters long, and include one uppercase letter, one lowercase letter, one number, and one special character."
            mPass.requestFocus()
            return
        }

        mAuth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = hashMapOf(
                        "name" to name,
                        "mobile" to mobile,
                        "email" to email
                    )
                    firestore.collection("Users")
                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .set(user)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this@RegisterActivity, "Registered Successfully!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this@RegisterActivity, "Registration Error!", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this@RegisterActivity, "Registration Error!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun onLoginClick(view: View) {
        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
    }
}

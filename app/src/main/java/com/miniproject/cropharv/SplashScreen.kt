package com.miniproject.cropharv

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        val icon = findViewById<ImageView>(R.id.icon)
        val animation = AnimationUtils.loadAnimation(this, R.anim.icon_animation)
        icon.startAnimation(animation)
        Handler().postDelayed({
            // Start your main activity here
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 4000)
    }
}
package com.droid.symbipool

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_authentication.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && user.isEmailVerified) {
            Handler().postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 1200)
        } else {
            Handler().postDelayed({
                startActivity(Intent(this, AuthenticationActivity::class.java))
                finish()
            }, 1200)
        }
    }
}

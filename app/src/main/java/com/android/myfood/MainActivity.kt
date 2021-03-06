@file:Suppress("DEPRECATION")

package com.android.myfood

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.android.myfood.home.HomeActivity
import com.android.myfood.login.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser

        Handler().postDelayed({
            if( user != null)
            {
                Credencials.USER_ID = user.uid
                val signInIntent = Intent(this, HomeActivity::class.java)
                startActivity(signInIntent)
            }
            else
            {
                val welcomeIntent = Intent(this, WelcomeActivity::class.java)
                startActivity(welcomeIntent)
            }
        }, 2000)
    }
}
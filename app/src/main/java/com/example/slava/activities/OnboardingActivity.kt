package com.example.slava.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.slava.R
import com.example.slava.activities.LoginActivity
import com.example.slava.databinding.ActivityOnboardingBinding
import com.example.slava.utils.SupabaseClient

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private val supabaseClient: SupabaseClient = SupabaseClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        val response = supabaseClient.getToken(this@OnboardingActivity)
//        Log.d("123", response.toString())
//        if (response != null) {
//            startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
//        }

        binding.onboardingLoginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.onboardingSignupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}
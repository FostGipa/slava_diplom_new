package com.example.slava.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slava.R
import com.example.slava.databinding.ActivityLoginBinding
import com.example.slava.utils.SupabaseClient
import com.example.slava.utils.UserState
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val supabaseClient: SupabaseClient = SupabaseClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.loginToSignupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }

        binding.loginForgotPassButton.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.loginBackButton.setOnClickListener {
            finish()
        }

        binding.loginButton.setOnClickListener {
            supabaseClient.login(this@LoginActivity,
                binding.emailEditText.text.toString(), binding.passwordEditText.text.toString()
            )
            lifecycleScope.launch {
                supabaseClient.userState.collect { state ->
                    when (state) {
                        is UserState.Success -> {
                            Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT)
                                .show()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }

                        is UserState.Error -> {
                            Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                        UserState.Loading -> {
                            // Можно показать индикатор загрузки, если необходимо
                        }
                    }
                }
            }
        }
    }
}
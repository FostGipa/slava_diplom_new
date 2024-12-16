package com.example.slava.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slava.R
import com.example.slava.databinding.ActivityForgotPasswordBinding
import com.example.slava.utils.SupabaseClient
import com.example.slava.utils.UserState
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val supabaseClient: SupabaseClient = SupabaseClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.forgotOkButton.setOnClickListener {
            if (binding.codeLinearLayout.visibility == View.GONE) {
                supabaseClient.sendEmailOtp(binding.emailEditText.text.toString())
                lifecycleScope.launch {
                    supabaseClient.userState.collect { state ->
                        when (state) {
                            is UserState.Success -> {
                                binding.codeLinearLayout.visibility = View.VISIBLE
                            }

                            is UserState.Error -> {
                                Toast.makeText(this@ForgotPasswordActivity, state.message, Toast.LENGTH_SHORT)
                                    .show()
                            }

                            UserState.Loading -> {
                                // Можно показать индикатор загрузки, если необходимо
                            }
                        }
                    }
                }
            } else {
                supabaseClient.checkOtp(binding.emailEditText.text.toString(), binding.codeEditText.text.toString())
                lifecycleScope.launch {
                    supabaseClient.userState.collect { state ->
                        when (state) {
                            is UserState.Success -> {
                                startActivity(Intent(this@ForgotPasswordActivity, UpdatePasswordActivity::class.java))
                                finish()
                            }

                            is UserState.Error -> {
                                Toast.makeText(this@ForgotPasswordActivity, state.message, Toast.LENGTH_SHORT)
                                    .show()
                            }

                            UserState.Loading -> {

                            }
                        }
                    }
                }
            }
        }

        binding.forgotBackButton.setOnClickListener {
            finish()
        }
    }
}
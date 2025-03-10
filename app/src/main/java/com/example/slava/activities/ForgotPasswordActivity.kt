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
                lifecycleScope.launch {
                    val result = supabaseClient.sendEmailOtp(binding.emailEditText.text.toString())
                    result.onSuccess {
                        binding.codeLinearLayout.visibility = View.VISIBLE
                        Toast.makeText(this@ForgotPasswordActivity, "Код выслан на почту", Toast.LENGTH_SHORT).show()
                    }.onFailure { error ->
                        Toast.makeText(this@ForgotPasswordActivity, error.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                lifecycleScope.launch {
                    val result = supabaseClient.checkOtp(binding.codeEditText.text.toString(), binding.emailEditText.text.toString())
                    result.onSuccess {
                        Toast.makeText(this@ForgotPasswordActivity, "Пароль изменен", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@ForgotPasswordActivity, UpdatePasswordActivity::class.java))
                    }.onFailure { error ->
                        Toast.makeText(this@ForgotPasswordActivity, error.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.forgotBackButton.setOnClickListener {
            finish()
        }
    }
}
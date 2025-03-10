package com.example.slava.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slava.R
import com.example.slava.databinding.ActivityLoginBinding
import com.example.slava.utils.SupabaseClient
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

        // Кнопка для перехода в "SignupActivity"
        binding.loginToSignupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }

        // Кнопка для перехода в "ForgotPasswordActivity"
        binding.loginForgotPassButton.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Кнопка "Назад"
        binding.loginBackButton.setOnClickListener {
            finish()
        }


        // Действие по нажатию на кнопку Логин
        binding.loginButton.setOnClickListener {
            lifecycleScope.launch {
                // Вызываем метод "login" с файлика "SupabaseClient"
                val result = supabaseClient.login(this@LoginActivity,
                    binding.emailEditText.text.toString(), binding.passwordEditText.text.toString())
                // Действие если успех
                result.onSuccess {
                    // Переход на главный экран
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                // Тут если провал
                }.onFailure { error ->
                    Toast.makeText(this@LoginActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
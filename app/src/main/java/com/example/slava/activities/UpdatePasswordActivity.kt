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
import com.example.slava.databinding.ActivityUpdatePasswordBinding
import com.example.slava.utils.SupabaseClient
import kotlinx.coroutines.launch

class UpdatePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdatePasswordBinding
    private val  supabaseClient : SupabaseClient = SupabaseClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUpdatePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.updateBackButton.setOnClickListener {
            finish()
        }

        binding.updateOkButton.setOnClickListener {
            if (binding.passwordEditText.text.toString() == binding.confirmPasswordEditText.text.toString()) {
                lifecycleScope.launch {
                    val result = supabaseClient.updatePassword(binding.passwordEditText.text.toString())
                    result.onSuccess {
                        startActivity(Intent(this@UpdatePasswordActivity, MainActivity::class.java))
                    }.onFailure { error ->
                        Toast.makeText(this@UpdatePasswordActivity, error.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@UpdatePasswordActivity, "Пароли не совпадают!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
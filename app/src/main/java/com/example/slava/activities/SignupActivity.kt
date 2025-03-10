package com.example.slava.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slava.R
import com.example.slava.databinding.ActivitySignupBinding
import com.example.slava.utils.SupabaseClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SignupActivity : AppCompatActivity() {

    // Инициализируем глобальные переменные
    private lateinit var binding: ActivitySignupBinding
    private val calendar = Calendar.getInstance()
    private val supabaseClient: SupabaseClient = SupabaseClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Кнопка "Назад"
        binding.signupBackButton.setOnClickListener {
            finish()
        }

        binding.signToLoginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.dateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        val editTexts = listOf(binding.nameEditText, binding.passwordEditText, binding.emaiEditText, binding.phoneEditText, binding.dateEditText)

        binding.signupButton.setOnClickListener {
            var allFieldsFilled = true

            editTexts.forEach { editText ->
                if (editText.text.isEmpty()) {
                    editText.setBackgroundResource(R.drawable.custom_error_edittext)
                    allFieldsFilled = false
                }
            }

            if (allFieldsFilled) {
                val email = binding.emaiEditText.text.toString().trim()
                val password = binding.passwordEditText.text.toString().trim()

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (password.length < 6) {
                    Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                lifecycleScope.launch {
                    val result = supabaseClient.signup(
                        this@SignupActivity,
                        email,
                        password,
                        binding.nameEditText.text.toString(),
                        binding.phoneEditText.text.toString(),
                        binding.dateEditText.text.toString()
                    )
                    result.onSuccess {
                        startActivity(Intent(this@SignupActivity, MainActivity::class.java))
                    }.onFailure { error ->
                        Toast.makeText(this@SignupActivity, error.message, Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private val dateSetListener =
        DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

    private fun updateDateInView() {
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        binding.dateEditText.setText(simpleDateFormat.format(calendar.time))
    }
}
package com.example.slava.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
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
import com.example.slava.utils.UserState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val supabaseClient: SupabaseClient = SupabaseClient()
    private val calendar = Calendar.getInstance()

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
                try {
                    supabaseClient.signUp(
                        this@SignupActivity,
                        binding.emaiEditText.text.toString(),
                        binding.passwordEditText.text.toString(),
                        binding.nameEditText.text.toString(),
                        binding.phoneEditText.text.toString(),
                        binding.dateEditText.text.toString()
                    )

                    lifecycleScope.launch {
                        supabaseClient.userState.collect { state ->
                            when (state) {
                                is UserState.Success -> {
                                    Toast.makeText(this@SignupActivity, state.message, Toast.LENGTH_SHORT)
                                        .show()
                                    startActivity(Intent(this@SignupActivity, MainActivity::class.java))
                                    finish()
                                }

                                is UserState.Error -> {
                                    Toast.makeText(this@SignupActivity, state.message, Toast.LENGTH_SHORT)
                                        .show()
                                }

                                UserState.Loading -> {
                                    // Можно показать индикатор загрузки, если необходимо
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
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
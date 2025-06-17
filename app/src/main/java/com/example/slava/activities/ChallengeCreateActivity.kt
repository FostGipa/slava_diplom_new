package com.example.slava.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slava.R
import com.example.slava.activities.UserChallengeDetailActivity
import com.example.slava.databinding.ActivityChallengeCreateBinding
import com.example.slava.utils.SupabaseClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChallengeCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChallengeCreateBinding
    private val supabaseClient: SupabaseClient = SupabaseClient()
    private val calendar = Calendar.getInstance()
    private var selectedCategory = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChallengeCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.startDateEditText.setOnClickListener {
            showDatePickerDialog { date -> binding.startDateEditText.setText(date) }
        }

        binding.endDateEditText.setOnClickListener {
            showDatePickerDialog { date -> binding.endDateEditText.setText(date) }
        }

        val categories = mutableListOf("Выберите категорию", "Чтение", "Программирование", "Спорт", "Языки")

        val adapter = object : ArrayAdapter<String>(this@ChallengeCreateActivity, R.layout.black_spinner_item, categories) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(ContextCompat.getColor(context, R.color.black))
                view.textSize = 16f
                return view
            }
        }

        binding.categorySpinner.adapter = adapter

        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.categorySpinner.setBackgroundResource(R.drawable.category_unselected)
            }
        }

        binding.createButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val description = binding.descriptionEditText.text.toString().trim()
            val reward = binding.rewardEditText.text.toString().trim()
            val startDate = binding.startDateEditText.text.toString().trim()
            val endDate = binding.endDateEditText.text.toString().trim()
            val tasksText = binding.taskEditText.text.toString().trim()

            val tasksList = tasksText
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            if (name.isEmpty() || description.isEmpty() || reward.isEmpty()
                || startDate.isEmpty() || endDate.isEmpty()
                || selectedCategory == 0 || tasksList.isEmpty()
            ) {
                Toast.makeText(this, "Пожалуйста, заполните все поля и выберите категорию", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val response = supabaseClient.getUserById(supabaseClient.getToken(this@ChallengeCreateActivity).toString())
                response.onSuccess { user ->
                    val response = supabaseClient.createChallenge(
                        name = name,
                        id_category = selectedCategory,
                        description = description,
                        tasks = tasksList,
                        reward = reward,
                        challenge_start_date = formatDateForDB(startDate),
                        challenge_end_date = formatDateForDB(endDate),
                        userId = user.id_user!!
                    )
                    response.onSuccess {
                        finish()
                        Toast.makeText(this@ChallengeCreateActivity, "Ваш челлендж отправлен на модерацию", Toast.LENGTH_SHORT).show()
                    }
                }.onFailure {
                }
            }
        }

    }

    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                onDateSelected(simpleDateFormat.format(selectedCalendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun formatDateForDB(date: String): String {
        val inputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return outputFormat.format(inputFormat.parse(date)!!)
    }
}


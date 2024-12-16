package com.example.slava.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.slava.databinding.ActivityChallengeDetailBinding
import com.example.slava.models.Challenge
import com.example.slava.utils.SupabaseClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChallengeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChallengeDetailBinding
    private val supabaseClient : SupabaseClient = SupabaseClient()
    private var challengeId : Int = 0
    private var userId : Int = 0
    private var challengeIsActive : Boolean = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChallengeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        val challenge = intent.getParcelableExtra<Challenge>("challenge")

        challenge?.let {
            binding.nameTextView.text = it.name
            binding.descriptionTextView.text = it.description
            binding.rewardTextView.text = it.rewards
            binding.taskTextView.text = it.tasks
            if (it.id_category == 1) {
                binding.categoryTextView.text = "Чтение"
            }
            challengeId = it.id!!
            binding.dateEditText.text = it.challenge_start_date + "-" + it.challenge_end_date
        } ?: run {
            Toast.makeText(this, "Данные о вызове не найдены", Toast.LENGTH_SHORT).show()
            finish()
        }

        try {
            lifecycleScope.launch {
                binding.userCountTextView.text = supabaseClient.getChallengeUsers(challengeId).toString()
            }
        } catch (e: Exception) {
            Log.e("123", e.message.toString())
        }


        val token = supabaseClient.getToken(this@ChallengeDetailActivity)
        if (token != null) {
            lifecycleScope.launch {
                try {
                    val user = supabaseClient.getUserById(token)
                    if (user != null) {
                        val isChallengeActive = supabaseClient.isChallengeActive(user.id!!, challengeId)
                        if (isChallengeActive) {
                            userId = user.id
                            val progress = supabaseClient.getChallengeProgress(user.id, challengeId)
                            binding.progressBar.progress = progress!!.progress
                            binding.challengeTakeDateTextView.text = progress.date
                            binding.progressTaskTextView.text = progress.steps.toString()
                            challengeIsActive = true
                            binding.hidenProgressLayout.visibility = View.VISIBLE
                            binding.acceptButton.text = "Начать тест"
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ChallengeDetailActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this@ChallengeDetailActivity, "Токен не найден", Toast.LENGTH_SHORT).show()
        }

        binding.acceptButton.setOnClickListener{
            if (challengeIsActive == true) {
                val intent = Intent(this@ChallengeDetailActivity, TestActivity::class.java)
                intent.putExtra("challenge_id", challengeId)
                startActivityForResult(intent, REQUEST_CODE_TEST)
            } else {
                val token = supabaseClient.getToken(this@ChallengeDetailActivity)
                if (token != null) {
                    lifecycleScope.launch {
                        try {
                            val user = supabaseClient.getUserById(token)
                            if (user != null) {
                                supabaseClient.insertUserChallenge(user.id!!, challengeId)
                                Toast.makeText(this@ChallengeDetailActivity, "Вызов принят!", Toast.LENGTH_SHORT).show()
                                binding.hidenProgressLayout.visibility = View.VISIBLE
                                binding.acceptButton.text = "Начать тест"
                                challengeIsActive = true
                                supabaseClient.insertProgress(challengeId, getCurrentDate(), "Пройти тест", 0, user.id)

                            } else {
                                Toast.makeText(this@ChallengeDetailActivity, "Пользователь не найден", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@ChallengeDetailActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@ChallengeDetailActivity, "Токен не найден", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.ratingButton.setOnClickListener{
            startActivity(Intent(this@ChallengeDetailActivity, RatingActivity::class.java).putExtra("challengeId", challengeId))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateButtonState(text: String) {
        binding.resultTextView.text = "Ваш результат: ${text}"
        binding.acceptButton.text = "Тест пройден"
        binding.acceptButton.isEnabled = false
        binding.progressBar.progress = 100
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_TEST && resultCode == RESULT_OK) {
            val score = data?.getIntExtra("score", 0) ?: 0
            updateButtonState(score.toString())
        }
    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }

    companion object {
        private const val REQUEST_CODE_TEST = 1
    }
}
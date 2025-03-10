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
import com.example.slava.utils.SupabaseClient
import kotlinx.coroutines.launch

class ChallengeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChallengeDetailBinding
    private val supabaseClient = SupabaseClient()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChallengeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val challengeId = intent.getIntExtra("challengeId", 0)

        binding.backButton.setOnClickListener { finish() }
        binding.ratingButton.setOnClickListener {
            startActivity(Intent(this, RatingActivity::class.java).putExtra("challengeId", challengeId))
        }

        loadChallengeDetails(challengeId)
        binding.acceptButton.setOnClickListener { acceptChallenge(challengeId) }
    }

    @SuppressLint("SetTextI18n")
    private fun loadChallengeDetails(challengeId: Int) {
        binding.linearLayout.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            supabaseClient.fetchChallenge(challengeId).onSuccess { challenge ->
                binding.apply {
                    nameTextView.text = challenge.name
                    descriptionTextView.text = challenge.description
                    categoryTextView.text = challenge.category?.name
                    rewardTextView.text = challenge.reward
                    taskTextView.text = challenge.tasks.joinToString("\n") { "• $it" }
                    dateEditText.text = "${challenge.challenge_start_date} - ${challenge.challenge_end_date}"
                }
            }.onFailure { error ->
                Log.e("ChallengeDetail", "Error fetching challenge: ${error.message}")
            }
            val count = supabaseClient.getChallengeUsers(challengeId).getOrElse {
                Log.e("ChallengeDetail", it.message.toString())
                0 // Устанавливаем 0 в случае ошибки
            }
            binding.userCountTextView.text = count.toString()
            binding.progressBar.visibility = View.GONE
            binding.linearLayout.visibility = View.VISIBLE
        }
    }

    private fun acceptChallenge(challengeId: Int) {
        lifecycleScope.launch {
            val response = supabaseClient.getUserById(supabaseClient.getToken(this@ChallengeDetailActivity).toString())
            response.onSuccess { user ->
                val response = supabaseClient.fetchChallenge(challengeId)
                response.onSuccess { challenge ->
                    supabaseClient.insertUserChallenge(
                        userId = user.id_user!!,
                        challengeId = challengeId,
                        step = challenge.tasks[0],
                        progress = 0
                    )
                    finish()
                    Toast.makeText(this@ChallengeDetailActivity, "Челлендж принят!", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(this@ChallengeDetailActivity, "Ошибка принятия челленджа", Toast.LENGTH_SHORT).show()
                }
            }.onFailure {
                Toast.makeText(this@ChallengeDetailActivity, "Ошибка получения пользователя!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

package com.example.slava.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slava.R
import com.example.slava.databinding.ActivityUserChallengeDetailBinding
import com.example.slava.utils.SupabaseClient
import kotlinx.coroutines.launch

class UserChallengeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserChallengeDetailBinding
    private val supabaseClient : SupabaseClient = SupabaseClient()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserChallengeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val challengeId = intent.getIntExtra("challengeId", 0)

        binding.backButton.setOnClickListener { finish() }
        binding.ratingButton.setOnClickListener {
            startActivity(Intent(this, RatingActivity::class.java).putExtra("challengeId", challengeId))
        }

        loadChallengeDetails(challengeId)
        loadChallengeUsersCount(challengeId)
        binding.acceptButton.setOnClickListener { acceptChallenge(challengeId) }
    }

    @SuppressLint("SetTextI18n")
    private fun loadChallengeDetails(challengeId: Int) {
        binding.linearLayout.visibility = View.GONE
        binding.progressBar1.visibility = View.VISIBLE
        lifecycleScope.launch {
            val response = supabaseClient.getUserById(supabaseClient.getToken(this@UserChallengeDetailActivity).toString())
            response.onSuccess { user ->
                supabaseClient.fetchUserAcceptedChallenge(challengeId = challengeId, userId = user.id_user!!).onSuccess { challenge ->
                    binding.apply {
                        nameTextView.text = challenge.challenge?.name
                        descriptionTextView.text = challenge.challenge?.description
                        categoryTextView.text = challenge.challenge?.name
                        rewardTextView.text = challenge.challenge?.reward
                        taskTextView.text = challenge.challenge?.tasks?.joinToString("\n") { "• $it" }
                        dateEditText.text = "${challenge.challenge?.challenge_start_date} - ${challenge.challenge?.challenge_end_date}"
                        progressTaskTextView.text = challenge.step
                        progressBar.progress = challenge.progress
                    }
                }.onFailure { error ->
                    Log.e("ChallengeDetail", "Error fetching challenge: ${error.message}")
                }
            }.onFailure {
                Toast.makeText(this@UserChallengeDetailActivity, "Ошибка получения пользователя!", Toast.LENGTH_SHORT).show()
            }

            binding.progressBar1.visibility = View.GONE
            binding.linearLayout.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadChallengeUsersCount(challengeId: Int) {
        lifecycleScope.launch {
            val count = supabaseClient.getChallengeUsers(challengeId).getOrElse {
                Log.e("ChallengeDetail", it.message.toString())
                0 // Устанавливаем 0 в случае ошибки
            }
            binding.userCountTextView.text = count.toString()
        }
    }

    private fun acceptChallenge(challengeId: Int) {
        lifecycleScope.launch {
            runCatching {
                val response = supabaseClient.getUserById(supabaseClient.getToken(this@UserChallengeDetailActivity).toString())
                response.onSuccess { user ->
                    val challenge = supabaseClient.fetchChallenge(challengeId).getOrThrow()
                    supabaseClient.insertUserChallenge(
                        userId = user.id_user!!,
                        challengeId = challengeId,
                        step = challenge.tasks[0],
                        progress = 0
                    )
                }.onFailure {
                    Toast.makeText(this@UserChallengeDetailActivity, "Ошибка получения пользователя!", Toast.LENGTH_SHORT).show()
                }
            }.onSuccess {
                Toast.makeText(this@UserChallengeDetailActivity, "Вызов принят!", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { error ->
                Toast.makeText(this@UserChallengeDetailActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
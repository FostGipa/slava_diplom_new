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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.slava.R
import com.example.slava.adapters.MyChallengesAdapter
import com.example.slava.adapters.MyOtvetAdapter
import com.example.slava.databinding.ActivityUserChallengeDetailBinding
import com.example.slava.utils.SupabaseClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class UserChallengeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserChallengeDetailBinding
    private val supabaseClient : SupabaseClient = SupabaseClient()
    private var testId = 0
    private var userId = 0
    private var challengeId = 0
    private lateinit var myChallengesAdapter: MyOtvetAdapter

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

        myChallengesAdapter = MyOtvetAdapter(emptyList())
        binding.myOtvetRecyclerView.layoutManager = LinearLayoutManager(this@UserChallengeDetailActivity)
        binding.myOtvetRecyclerView.adapter = myChallengesAdapter

        challengeId = intent.getIntExtra("challengeId", 0)

        binding.backButton.setOnClickListener { finish() }
        binding.ratingButton.setOnClickListener {
            startActivity(Intent(this, RatingActivity::class.java).putExtra("challengeId", challengeId))
        }

        loadChallengeDetails(challengeId)
        loadChallengeUsersCount(challengeId)
        binding.testButton.setOnClickListener {
            val intent = Intent(this, TestActivity::class.java)
            intent.putExtra("taskId", testId)
            intent.putExtra("userId", userId)
            intent.putExtra("challengeId", challengeId)
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadChallengeDetails(challengeId: Int) {
        binding.relativeLayout.visibility = View.GONE
        binding.progressBar1.visibility = View.VISIBLE
        lifecycleScope.launch {
            val response = supabaseClient.getUserById(supabaseClient.getToken(this@UserChallengeDetailActivity).toString())
            response.onSuccess { user ->
                userId = user.id_user!!
                supabaseClient.fetchUserAcceptedChallenge(challengeId = challengeId, userId = user.id_user).onSuccess { challenge ->
                    binding.apply {
                        nameTextView.text = challenge.challenge?.name
                        descriptionTextView.text = challenge.challenge?.description
                        categoryTextView.text = challenge.challenge?.category?.name
                        rewardTextView.text = challenge.challenge?.reward
                        taskTextView.text = challenge.challenge?.tasks?.joinToString("\n") { "• $it" }
                        dateEditText.text = "${challenge.challenge?.challenge_start_date} - ${challenge.challenge?.challenge_end_date}"
                        progressBar.progress = challenge.progress
                        challengeTakeDateTextView.text = "Дата принятия: ${formatDate(challenge.user_start_date.toString())}"
                        progressTextView.text = challenge.progress.toString() + " %"
                        val createdChallengeResult = supabaseClient.fetchUserTasksResultsChallenge(user.id_user, challengeId)
                        createdChallengeResult.onSuccess { myChallenge ->
                            myChallengesAdapter.updateData(myChallenge)
                        }.onFailure {
                            // не показываем ошибку, если ничего не создано
                        }
                    }

                    supabaseClient.fetchNextPendingTask(user.id_user, challengeId).onSuccess { nextTask ->
                        if (nextTask == null) {
                            binding.testButton.visibility = View.GONE
                            binding.progressTaskTextView.text = "Челлендж пройден!"
                            binding.progressBar.progress = 100
                            binding.progressTextView.text = "100 %"
                        } else {
                            binding.progressTaskTextView.text = nextTask.name
                            testId = nextTask.id!!
                        }

                        val intent = when (nextTask?.type) {
                            "test" ->
                            {
                                binding.testButton.text = "Пройти тест"
                                Intent(this@UserChallengeDetailActivity, TestActivity::class.java)
                            }
                            "file" -> {
                                binding.testButton.text = "Добавить ответ"
                                Intent(this@UserChallengeDetailActivity, ChallengeAddFileActivity::class.java)
                            }
                            else -> Intent(this@UserChallengeDetailActivity, ChallengeAddFileActivity::class.java)
                        }

                        // Добавляем дополнительные параметры в Intent
                        intent.putExtra("taskId", testId)
                        intent.putExtra("userId", userId)
                        intent.putExtra("challengeId", challengeId)

                        // Запускаем нужное активити
                        binding.testButton.setOnClickListener {
                            startActivity(intent)
                        }
                    }.onFailure {
                        Log.e("ChallengeDetail", "Ошибка при получении следующего теста: ${it.message}")
                    }
                }.onFailure { error ->
                    Log.e("ChallengeDetail", "Error fetching challenge: ${error.message}")
                }
            }.onFailure {
                Toast.makeText(this@UserChallengeDetailActivity, "Ошибка получения пользователя!", Toast.LENGTH_SHORT).show()
            }

            binding.progressBar1.visibility = View.GONE
            binding.relativeLayout.visibility = View.VISIBLE
        }
    }

    fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMMM, yyyy", Locale("ru"))

        val date = inputFormat.parse(dateString)
        return date?.let { outputFormat.format(it) } ?: "Некорректная дата"
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

    override fun onResume() {
        super.onResume()
        loadChallengeDetails(challengeId)
    }
}
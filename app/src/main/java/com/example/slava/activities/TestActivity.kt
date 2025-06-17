package com.example.slava.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
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
import com.example.slava.adapters.AnswerAdapter
import com.example.slava.databinding.ActivityTestBinding
import com.example.slava.models.QuestionWithAnswers
import com.example.slava.utils.SupabaseClient
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.exp

class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding
    private val supabaseClient = SupabaseClient()

    private var questions: List<QuestionWithAnswers> = listOf()
    private var currentQuestionIndex = 0
    private var selectedAnswerIndex: Int = -1
    private var score = 0
    private var taskId = 0
    private var challengeId = 0
    private var userId = 0
    private var isAnswerSelected = false

    private lateinit var answerAdapter: AnswerAdapter
    private var startTime: Long = 0L
    private var timeSpent: Long = 0L
    private lateinit var timer: CountDownTimer

    private val BASE_POINTS = 100.0
    private val TIME_COEFF = 30.0
    private var totalPts = 0
    private var questionStartTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.loginBackButton.setOnClickListener {
            finish()
        }

        setupRecyclerView()

        taskId = intent.getIntExtra("taskId", 0)
        userId = intent.getIntExtra("userId", 0)
        challengeId = intent.getIntExtra("challengeId", 0)

        loadData(testId = taskId)
        loadQuestions(testId = taskId)
        startTimer()
        showQuestion()

        binding.nextButton.setOnClickListener {
            checkAnswer()
            if (selectedAnswerIndex == -1) {
                Toast.makeText(this, "Выберите ответ перед переходом к следующему вопросу", Toast.LENGTH_SHORT).show()
            } else {
                if (currentQuestionIndex < questions.size - 1) {
                    currentQuestionIndex++
                    selectedAnswerIndex = -1
                    showQuestion()
                } else {
                    stopTimer()
                    lifecycleScope.launch {
                        supabaseClient.insertTestResult(
                            userId = userId,
                            taskId = taskId,
                            result = "${score}/${questions.size}",
                            challengeId = challengeId
                        )
                        supabaseClient.updateUserChallengePts(userId = userId, challengeId = challengeId, totalPts)
                        supabaseClient.updateUserPts(userId = userId, pts = totalPts)

                    }
                    showResults()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewAnswers.layoutManager = LinearLayoutManager(this)
        answerAdapter = AnswerAdapter(mutableListOf()) { selectedAnswer ->
            if (!isAnswerSelected) {
                selectedAnswerIndex = selectedAnswer.id
                isAnswerSelected = true
                binding.nextButton.isEnabled = true
            }
        }
        binding.recyclerViewAnswers.adapter = answerAdapter
    }

    private fun loadData(testId: Int) {
        lifecycleScope.launch {
            val response = supabaseClient.fetchTasksById(testId)
            response.onSuccess { task ->
                binding.nameTextView.text = task.name
            }
        }
    }

    private fun loadQuestions(testId: Int) {
        binding.linearLayout.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                questions = supabaseClient.fetchQuestionsByTestId(testId)
                if (questions.isEmpty()) {
                    Log.e("TestActivity", "Ошибка: вопросы не найдены для testId = $testId")
                    Toast.makeText(this@TestActivity, "Вопросы не найдены", Toast.LENGTH_LONG).show()
                    finish()
                    return@launch
                }
                binding.progressBar.visibility = View.GONE
                binding.linearLayout.visibility = View.VISIBLE
                showQuestion()
                updateProgress()
            } catch (e: Exception) {
                Log.e("TestActivity", "Ошибка загрузки вопросов: ${e.message}")
                Toast.makeText(this@TestActivity, "Ошибка загрузки вопросов", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }


    private fun showQuestion() {
        if (questions.isEmpty()) {
            Log.e("TestActivity", "Попытка показать вопрос, но список вопросов пуст!")
            return
        }

        val question = questions[currentQuestionIndex]
        binding.questionTextView.text = question.text
        answerAdapter.updateAnswers(question.answers.toMutableList())

        updateProgress()

        questionStartTime = System.currentTimeMillis()
        isAnswerSelected = false
        binding.nextButton.isEnabled = false
    }


    private fun checkAnswer() {
        val correctAnswer = questions[currentQuestionIndex].answers.firstOrNull { it.isCorrect }

        if (correctAnswer?.id == selectedAnswerIndex) {
            score++

            val timeTaken = (System.currentTimeMillis() - questionStartTime) / 1000.0

            val earnedPts = (BASE_POINTS * exp(-timeTaken / TIME_COEFF)).toInt()
            totalPts += earnedPts

            Log.d("TestActivity", "Правильный ответ! Время: ${timeTaken}s, Начислено: $earnedPts pts")
        }
    }

    private fun startTimer() {
        startTime = System.currentTimeMillis()
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
            }
        }.start()
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgress() {
        val progress = ((currentQuestionIndex + 1).toFloat() / questions.size * 100).toInt()
        binding.questionsProgressBar.progress = progress
        binding.questionsProgressTextView.text = "${currentQuestionIndex + 1}/${questions.size}"
    }

    private fun stopTimer() {
        timeSpent = System.currentTimeMillis() - startTime
        timer.cancel()
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(milliseconds: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun showResults() {
        startActivity(
            Intent(this@TestActivity, TaskResultActivity::class.java)
                .putExtra("score", "${score}/${questions.size}")
                .putExtra("pts", totalPts.toString())
                .putExtra("time", formatTime(timeSpent).toString())
        )
        finish()
    }
}

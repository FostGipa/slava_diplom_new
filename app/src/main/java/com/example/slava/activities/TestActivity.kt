package com.example.slava.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.slava.adapters.AnswerAdapter
import com.example.slava.databinding.ActivityTestBinding
import com.example.slava.models.QuestionWithAnswers
import com.example.slava.utils.SupabaseClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding
    private val supabaseClient = SupabaseClient()

    private var questions: List<QuestionWithAnswers> = listOf()
    private var currentQuestionIndex = 0
    private var selectedAnswerIndex: Int = -1
    private var score = 0
    private var testId = 0
    private var challengeId = 0

    private lateinit var answerAdapter: AnswerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBackButton.setOnClickListener {
            finish()
        }

        try {
            binding.recyclerViewAnswers.layoutManager = LinearLayoutManager(this)
            answerAdapter = AnswerAdapter(mutableListOf()) { selectedAnswer ->
                selectedAnswerIndex = selectedAnswer.id
            }
            binding.recyclerViewAnswers.adapter = answerAdapter
        } catch (e: Exception) {
            Log.e("123", e.message.toString())
        }

        testId = intent.getIntExtra("test_id", 0)
        challengeId = intent.getIntExtra("challenge_id", 0)

        lifecycleScope.launch {
            loadQuestions(1)
            showQuestion()
        }

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
                    lifecycleScope.launch {
//                        saveTestResult(challengeId)
                    }
                    showResults()
                }
            }
        }
    }

    private suspend fun loadQuestions(testId: Int) {
        try {
            questions = supabaseClient.fetchQuestionsByTestId(testId)
        } catch (e: Exception) {
            Log.e("TestActivity", "Ошибка загрузки вопросов: ${e.message}")
        }
    }

    private fun showQuestion() {
        val question = questions[currentQuestionIndex]
        binding.questionTextView.text = question.text

        // Обновляем адаптер с новыми ответами
        answerAdapter.updateAnswers(question.answers.toMutableList())
    }

    private fun checkAnswer() {
        if (selectedAnswerIndex == -1) {
            Toast.makeText(this, "Выберите ответ", Toast.LENGTH_SHORT).show()
            return
        }

        val correctAnswer = questions[currentQuestionIndex].answers.firstOrNull { it.isCorrect }
        if (correctAnswer?.id == selectedAnswerIndex) {
            score++
        }
    }

    private fun showResults() {
        Toast.makeText(this, "Ваш результат: $score из ${questions.size}", Toast.LENGTH_LONG).show()

        // Возвращаем результат в родительскую активность
        val resultIntent = Intent()
        resultIntent.putExtra("score", score)
        setResult(RESULT_OK, resultIntent)

        finish()
    }

//    private suspend fun saveTestResult(challengeId: Int) {
//        try {
//            val userId = supabaseClient.getUserById(supabaseClient.getToken(this@TestActivity).toString())
//            supabaseClient.saveTestResult(userId!!.id_user!!, challengeId, score)
//            supabaseClient.updateProgress(100, challengeId, userId.id_user, getCurrentDate())
//            Log.d("TestActivity", "Результаты успешно сохранены.")
//        } catch (e: Exception) {
//            Log.e("TestActivity", "Ошибка сохранения результата: ${e.message}")
//        }
//    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }
}


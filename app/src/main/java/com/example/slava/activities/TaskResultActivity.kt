package com.example.slava.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.slava.R
import com.example.slava.databinding.ActivityTaskResultBinding

class TaskResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskResultBinding
    private var timeSpent = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTaskResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.endButton.setOnClickListener { finish() }

        val pts = intent.getStringExtra("pts")
        val score = intent.getStringExtra("score")
        timeSpent = intent.getStringExtra("time").toString()

        binding.resultTextView.text = "Поздравляем!\nВаш результат - $score\nПотраченное время - $timeSpent\nВы получили $pts очков!"
    }
}
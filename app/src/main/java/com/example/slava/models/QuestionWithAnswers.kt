package com.example.slava.models

data class QuestionWithAnswers(
    val id: Int,
    val text: String,
    val answers: List<Answer>
)

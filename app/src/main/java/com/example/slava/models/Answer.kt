package com.example.slava.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Answer(
    @SerialName("id")
    val id: Int,
    @SerialName("id_questions")
    val id_questions: Int,
    @SerialName("text")
    val text: String,
    @SerialName("is_correct")
    val isCorrect: Boolean
)

package com.example.slava.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Question(
    @SerialName("id")
    val id: Int,
    @SerialName("id_test")
    val id_test: Int,
    @SerialName("text")
    val text: String
)
package com.example.slava.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Challenge(
    @SerialName("id_challenge")
    val id_challenge: Int? = null,
    @SerialName("id_category")
    val id_category: Int? = null,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String,
    @SerialName("tasks")
    val tasks: List<String>,
    @SerialName("reward")
    val reward: String,
    @SerialName("challenge_start_date")
    val challenge_start_date: String,
    @SerialName("challenge_end_date")
    val challenge_end_date: String,
    @SerialName("category")
    val category: Category? = null
)
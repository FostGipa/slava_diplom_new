package com.example.slava.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserChallenge(
    @SerialName("id_user_challenge")
    val id_user_challenge: Int? = null,
    @SerialName("id_user")
    val id_user: Int? = null,
    @SerialName("id_challenge")
    val id_challenge: Int? = null,
    @SerialName("user_start_date")
    val user_start_date: String? = null,
    @SerialName("step")
    val step: String,
    @SerialName("progress")
    val progress: Int,
    @SerialName("challenge")
    val challenge: Challenge? = null,
    @SerialName("users")
    val user: User? = null
)

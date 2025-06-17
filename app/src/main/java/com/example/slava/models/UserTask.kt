package com.example.slava.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserTask(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("id_task")
    val id_task: Int,
    @SerialName("id_user")
    val id_user: Int,
    @SerialName("result")
    val result: String,
    @SerialName("comment")
    val comment: String? = "",
    @SerialName("status")
    val status: String? = "",
    @SerialName("id_challenge")
    val id_challenge: Int? = null
)

package com.example.slava.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("chat_id")
    val chat_id: Int,
    @SerialName("type")
    val type: String,
    @SerialName("file_uri")
    val fileUri: String? = null,
    @SerialName("sender_id")
    val sender_id: Int,
    @SerialName("text")
    val text: String,
    @SerialName("created_at")
    val created_at: String? = ""
)

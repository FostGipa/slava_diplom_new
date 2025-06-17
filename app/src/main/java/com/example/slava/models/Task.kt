package com.example.slava.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    @SerialName("id")
    val id : Int? = null,
    @SerialName("name")
    val name : String,
    @SerialName("id_challenge")
    val id_challenge: Int,
    @SerialName("type")
    val type: String
)

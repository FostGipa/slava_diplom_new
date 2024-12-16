package com.example.slava.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Progress(
    @SerialName("id")
    val id : Int? = null,
    @SerialName("id_challenge")
    val id_challenge : Int,
    @SerialName("date")
    val date : String,
    @SerialName("steps")
    val steps : String,
    @SerialName("progress")
    val progress : Int,
    @SerialName("id_user")
    val id_user : Int
)

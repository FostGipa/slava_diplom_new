package com.example.slava.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Rating (
    @SerialName("id")
    val id : Int? = null,
    @SerialName("id_challenge")
    val id_challenge : Int,
    @SerialName("score")
    val score : Int,
    @SerialName("id_user")
    val id_user : Int
)
package com.example.slava.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("id")
    val id : Int? = null,
    @SerialName("uid")
    val uid : String,
    @SerialName("name")
    val name : String,
    @SerialName("phone")
    val phone : String,
    @SerialName("challenges")
    val challenges : List<String>? = emptyList(),
    @SerialName("date_of_birth")
    val date_of_birth : String
)

package com.example.slava.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("id_user")
    val id_user : Int? = null,
    @SerialName("uid")
    val uid : String? = "",
    @SerialName("name")
    val name : String,
    @SerialName("phone")
    val phone : String,
    @SerialName("date_of_birth")
    val date_of_birth : String,
    @SerialName("password")
    val password: String? = null,
    @SerialName("role")
    val role: String,
    @SerialName("user_pts")
    val user_pts: Int
)

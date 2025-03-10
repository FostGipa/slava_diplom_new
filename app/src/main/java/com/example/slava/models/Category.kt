package com.example.slava.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    @SerialName("id_category")
    val id_category : Int? = null,
    @SerialName("name")
    val name: String
)

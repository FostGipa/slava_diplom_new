package com.example.slava.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("user1_id")
    val user1_id: Int,
    @SerialName("user2_id")
    val user2_id: Int,
    @SerialName("created_at")
    val created_at: String? = "",
) {
    fun getOtherUserId(currentUserId: Int): Int {
        return if (currentUserId == user1_id) user2_id else user1_id
    }
}

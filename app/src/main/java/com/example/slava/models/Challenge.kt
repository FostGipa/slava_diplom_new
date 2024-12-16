package com.example.slava.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Challenge (
    @SerialName("id")
    val id : Int? = null,
    @SerialName("id_Категории_челленджа")
    val id_category : Int,
    @SerialName("Название")
    val name : String,
    @SerialName("Описание")
    val description : String,
    @SerialName("Задание")
    val tasks : String,
    @SerialName("Награда")
    val rewards : String,
    @SerialName("Время_начала")
    val start_date : String,
    @SerialName("Время_окончания")
    val end_date : String,
    @SerialName("Дата_начала_проведения")
    val challenge_start_date : String,
    @SerialName("Дата_окончание_проведения")
    val challenge_end_date : String
) : Parcelable
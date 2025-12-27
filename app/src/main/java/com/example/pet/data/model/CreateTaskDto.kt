package com.example.pet.data.model

import com.google.gson.annotations.SerializedName

/**
 * DTO для создания новой задачи.
 */
data class CreateTaskDto(
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("day")
    val day: String,
    @SerializedName("isCompleted")
    val isCompleted: Boolean = false
)


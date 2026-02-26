package com.example.pet.data.model

import com.example.pet.data.local.entity.TaskEntity
import com.google.gson.annotations.SerializedName
import java.util.UUID

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

fun CreateTaskDto.toEntity(): TaskEntity = TaskEntity(
    title = title,
    description = description ?: "",
    day = day,
    isCompleted = isCompleted
)

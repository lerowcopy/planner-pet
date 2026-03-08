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
    @SerializedName("startMinutes")
    val startMinutes: Int,
    @SerializedName("endMinutes")
    val endMinutes: Int,
    @SerializedName("isCompleted")
    val isCompleted: Boolean = false,
    @SerializedName("isSynced")
    val isSynced: Boolean = false
)

fun CreateTaskDto.toEntity(): TaskEntity = TaskEntity(
    title = this.title,
    description = this.description ?: "",
    day = this.day,
    startMinutes = this.startMinutes,
    endMinutes = this.endMinutes,
    isCompleted = this.isCompleted,
    isSynced = this.isSynced
)

package com.example.pet.domain.model

import com.example.pet.data.local.entity.TaskEntity
import com.example.pet.data.model.TaskDto

/**
 * Domain модель задачи.
 * Представляет бизнес-логику и не зависит от фреймворков.
 */
data class Task(
    val id: String,
    val title: String,
    val description: String? = null,
    val day: String,
    val startMinutes: Int,
    val endMinutes: Int,
    val isCompleted: Boolean = false,
    val isSynced: Boolean = false
)


fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = this.id.toLong(),
        title = this.title,
        description = this.description ?: "",
        day = this.day,
        startMinutes = this.startMinutes,
        endMinutes = this.endMinutes,
        isCompleted = this.isCompleted
    )
}

fun Task.toDto(): TaskDto {
    return TaskDto(
        id = this.id,
        title = this.title,
        description = this.description,
        day = this.day,
        startMinutes = this.startMinutes,
        endMinutes = this.endMinutes,
        isCompleted = this.isCompleted
    )
}



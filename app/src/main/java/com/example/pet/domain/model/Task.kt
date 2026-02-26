package com.example.pet.domain.model

import com.example.pet.data.local.entity.TaskEntity

/**
 * Domain модель задачи.
 * Представляет бизнес-логику и не зависит от фреймворков.
 */
data class Task(
    val id: String,
    val title: String,
    val description: String? = null,
    val day: String,
    val isCompleted: Boolean = false
)

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = this.id.toLong(),
        title = this.title,
        description = this.description ?: "",
        day = this.day,
        isCompleted = this.isCompleted
    )
}



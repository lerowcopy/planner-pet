package com.example.pet.data.model

import com.example.pet.domain.model.Task

/**
 * Data Transfer Object (DTO) для задачи.
 * Представляет модель данных, приходящую с сервера.
 */
data class TaskDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val day: String,
    val isCompleted: Boolean = false
) {
    /**
     * Преобразовать DTO в Domain модель.
     */
    fun toDomain(): Task {
        return Task(
            id = id,
            title = title,
            description = description,
            day = day,
            isCompleted = isCompleted
        )
    }
}

/**
 * Преобразовать список DTO в список Domain моделей.
 */
fun List<TaskDto>.toDomain(): List<Task> {
    return map { it.toDomain() }
}


package com.example.pet.domain.model

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


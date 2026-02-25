package com.example.pet.data.mapper

import com.example.pet.data.local.entity.TaskEntity
import com.example.pet.domain.model.Task

fun TaskEntity.toDomain() = Task(
    id = id.toString(),
    title = title,
    description = description,
    day = day,
    isCompleted = isCompleted
)

fun List<TaskEntity>.toDomain(): List<Task> {
    return map { it.toDomain() }
}
package com.example.pet.data.mapper

import com.example.pet.data.local.entity.TaskEntity
import com.example.pet.data.model.CreateTaskDto
import com.example.pet.data.model.TaskDto
import com.example.pet.domain.model.Task

fun TaskEntity.toCreateDto(): CreateTaskDto {
    return CreateTaskDto(
        title = this.title,
        description = this.description,
        day = this.day,
        isCompleted = this.isCompleted,
        isSynced = this.isSynced
    )
}

fun TaskEntity.toDomain() = Task(
    id = this.id.toString(),
    title = this.title,
    description = this.description,
    day = this.day,
    isCompleted = this.isCompleted,
    isSynced = this.isSynced
)

fun List<TaskEntity>.toDomain(): List<Task> {
    return map { it.toDomain() }
}
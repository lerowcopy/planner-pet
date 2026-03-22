package com.example.pet.data.model

import com.example.pet.data.local.entity.TaskEntity
import com.example.pet.domain.model.Task

data class TaskDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val day: String,
    val startMinutes: Int,
    val endMinutes: Int,
    val isCompleted: Boolean = false,
    val isSynced: Boolean = false
)

fun TaskDto.toEntity(): TaskEntity {
    return TaskEntity(
        id = this.id.toLong(),
        title = this.title,
        description = this.description ?: "",
        day = this.day,
        startMinutes = this.startMinutes,
        endMinutes = this.endMinutes,
        isCompleted = this.isCompleted,
        isSynced = this.isSynced
    )
}

fun TaskDto.toDomain(): Task {
    return Task(
        id = this.id,
        title = this.title,
        description = this.description,
        day = this.day,
        startMinutes = this.startMinutes,
        endMinutes = this.endMinutes,
        isCompleted = this.isCompleted,
        isSynced = this.isSynced
    )
}

fun List<TaskDto>.toDomain(): List<Task> {
    return map { it.toDomain() }
}

fun List<TaskDto>.toEntity(): List<TaskEntity> {
    return map { it.toEntity() }
}

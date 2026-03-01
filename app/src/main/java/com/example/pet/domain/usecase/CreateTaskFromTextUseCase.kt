package com.example.pet.domain.usecase

import com.example.pet.data.local.entity.TaskEntity
import com.example.pet.domain.repository.TaskRepository
import javax.inject.Inject

class CreateTaskFromTextUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke (input: String): Result<TaskEntity> {
        return runCatching { taskRepository.createTaskFromText(input) }
    }
}
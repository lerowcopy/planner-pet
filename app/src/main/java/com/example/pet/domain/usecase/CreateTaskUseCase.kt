package com.example.pet.domain.usecase

import com.example.pet.domain.model.Task
import com.example.pet.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case для создания новой задачи.
 * Инкапсулирует бизнес-логику создания задачи.
 */
class CreateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Выполнить создание задачи.
     * @param title Название задачи
     * @param description Описание задачи (опционально)
     * @param day День выполнения задачи
     * @return Flow с результатом созданной задачи
     */
    suspend operator fun invoke(
        title: String,
        description: String? = null,
        day: String,
        startMinutes: Int = 0,
        endMinutes: Int = 60
    ): Flow<Result<Task>> {
        return taskRepository.createTask(title, description, day, startMinutes, endMinutes)
    }
}


package com.example.pet.domain.usecase

import com.example.pet.domain.model.Task
import com.example.pet.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case для получения задачи по ID.
 */
class GetTaskByIdUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Выполнить получение задачи по ID.
     * @param taskId ID задачи
     * @return Flow с результатом задачи
     */
    suspend operator fun invoke(taskId: String): Flow<Result<Task>> {
        return taskRepository.getTaskById(taskId)
    }
}


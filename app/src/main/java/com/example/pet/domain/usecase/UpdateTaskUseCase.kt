package com.example.pet.domain.usecase

import com.example.pet.domain.model.Task
import com.example.pet.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case для обновления задачи.
 * Инкапсулирует бизнес-логику обновления задач.
 */
class UpdateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Выполнить обновление задачи.
     * @param task Обновленная задача
     * @return Flow с результатом обновленной задачи
     */
    suspend operator fun invoke(task: Task): Flow<Result<Task>> {
        return taskRepository.updateTask(task)
    }
}

package com.example.pet.domain.usecase

import com.example.pet.domain.model.Task
import com.example.pet.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case для получения списка задач.
 * Инкапсулирует бизнес-логику получения задач.
 */
class GetTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Выполнить получение списка задач.
     * @return Flow со списком задач
     */
    suspend operator fun invoke(): Flow<Result<List<Task>>> {
        return taskRepository.getTasks()
    }
}


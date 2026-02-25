package com.example.pet.domain.usecase

import com.example.pet.domain.repository.TaskRepository
import javax.inject.Inject

class RefreshTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {

    /**
     * Загрузить задачи
     */
    suspend operator fun invoke(): Result<Unit>{
        return taskRepository.refreshTasks()
    }
}
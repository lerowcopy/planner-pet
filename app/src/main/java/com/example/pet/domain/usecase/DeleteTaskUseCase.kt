package com.example.pet.domain.usecase

import com.example.pet.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Выполнить удаление задачи по ID.
     * @param taskId ID задачи
     * @return Flow с результатом операции (Unit при успехе)
     */
    suspend operator fun invoke(taskId: String): Flow<Result<Unit>> {
        return taskRepository.deleteTask(taskId)
    }
}


package com.example.pet.domain.usecase

import com.example.pet.domain.model.Task
import com.example.pet.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Dispatcher
import javax.inject.Inject

class GetTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Выполнить получение списка задач.
     * @return Flow со списком задач
     */
    operator fun invoke(): Flow<Result<List<Task>>> {
        return taskRepository.getTasks()
    }
}


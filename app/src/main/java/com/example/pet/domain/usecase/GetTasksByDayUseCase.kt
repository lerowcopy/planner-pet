package com.example.pet.domain.usecase

import com.example.pet.domain.model.Task
import com.example.pet.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTasksByDayUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(day: String): Flow<Result<List<Task>>> {
        return repository.getTasks().map { result ->
            result.map { tasks -> tasks.filter { it.day == day } }
        }
    }
}
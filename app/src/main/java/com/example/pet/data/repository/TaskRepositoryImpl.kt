package com.example.pet.data.repository

import com.example.pet.data.model.toDomain
import com.example.pet.data.remote.TaskRemoteDataSource
import com.example.pet.domain.model.Task
import com.example.pet.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Реализация репозитория для работы с задачами.
 * Координирует работу с разными источниками данных (сеть, локальная БД и т.д.).
 */
class TaskRepositoryImpl @Inject constructor(
    private val remoteDataSource: TaskRemoteDataSource
) : TaskRepository {
    
    override suspend fun getTasks(): Flow<Result<List<Task>>> = flow {
        val result = remoteDataSource.getTasks()
        emit(result.map { it.toDomain() })
    }
    
    override suspend fun getTaskById(taskId: String): Flow<Result<Task>> = flow {
        val result = remoteDataSource.getTaskById(taskId)
        emit(result.map { it.toDomain() })
    }
    
    override suspend fun createTask(
        title: String,
        description: String?,
        day: String
    ): Flow<Result<Task>> = flow {
        val createTaskDto = com.example.pet.data.model.CreateTaskDto(
            title = title,
            description = description,
            day = day,
            isCompleted = false
        )
        val result = remoteDataSource.createTask(createTaskDto)
        emit(result.map { it.toDomain() })
    }
}


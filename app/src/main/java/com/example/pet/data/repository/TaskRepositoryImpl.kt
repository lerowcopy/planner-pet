package com.example.pet.data.repository

import android.util.Log
import com.example.pet.data.local.dao.TaskDao
import com.example.pet.data.mapper.toDomain
import com.example.pet.data.model.toDomain
import com.example.pet.data.model.toDto
import com.example.pet.data.model.toEntity
import com.example.pet.data.remote.TaskRemoteDataSource
import com.example.pet.domain.model.Task
import com.example.pet.domain.model.TaskEvent
import com.example.pet.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Реализация репозитория для работы с задачами.
 * Координирует работу с разными источниками данных (сеть, локальная БД и т.д.).
 */
class TaskRepositoryImpl @Inject constructor(
    private val remoteDataSource: TaskRemoteDataSource,
    private val taskDao: TaskDao
) : TaskRepository {
    
    private val _taskEvents = MutableSharedFlow<TaskEvent>(extraBufferCapacity = 1)
    override val taskEvents: SharedFlow<TaskEvent> = _taskEvents.asSharedFlow()

    override fun getTasks(): Flow<Result<List<Task>>> {
        return taskDao.getTasks() // DAO должен возвращать Flow<List<TaskEntity>>
            .map { entities ->
                Result.success(entities.map { it.toDomain() })
            }
            .catch { exception ->
                emit(Result.failure(exception))
            }
            .flowOn(Dispatchers.IO)
    }
    
    override suspend fun getTaskById(taskId: String): Flow<Result<Task>> = flow {
        val localResult = taskDao.getTaskById(taskId)
        emit(Result.success(localResult.toDomain()))

        /*val result = remoteDataSource.getTaskById(taskId)
        emit(result.mapCatching { it.toDomain() })*/
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
        result.onSuccess { taskDto ->
            //val task = taskDto.toDomain()

            withContext(Dispatchers.IO) {
                taskDao.insertTask(taskDto.toEntity())
            }

                //_taskEvents.tryEmit(TaskEvent.TaskCreated(task))
        }

        emit(result.mapCatching { it.toDomain() })
    }
    
    override suspend fun updateTask(task: Task): Flow<Result<Task>> = flow {
        val taskDto = task.toDto()
        val result = remoteDataSource.updateTask(taskDto)
        result.onSuccess { updatedDto ->
            val updatedTask = updatedDto.toDomain()
            _taskEvents.tryEmit(TaskEvent.TaskUpdated(updatedTask))
        }
        emit(result.mapCatching { it.toDomain() })
    }

    override suspend fun refreshTasks(): Result<Unit> {
        return remoteDataSource.getTasks()
            .mapCatching { dtos ->
                Log.i("dtos", dtos.toString())
                withContext(Dispatchers.IO){
                    val entities = dtos.map { it.toEntity() }
                    taskDao.insertAll(entities)
                }
            }
    }

    override suspend fun deleteTask(taskId: String): Flow<Result<Unit>> = flow {
        val result = remoteDataSource.deleteTask(taskId)
        result.onSuccess {
            // Используем tryEmit, чтобы не блокировать выполнение
            _taskEvents.tryEmit(TaskEvent.TaskDeleted(taskId))
        }
        emit(result)
    }
}


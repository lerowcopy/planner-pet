package com.example.pet.data.repository

import android.util.Log
import com.example.pet.data.local.dao.TaskDao
import com.example.pet.data.local.entity.TaskEntity
import com.example.pet.data.mapper.toDomain
import com.example.pet.data.model.toDomain
import com.example.pet.data.model.toDto
import com.example.pet.data.model.toEntity
import com.example.pet.data.remote.TaskRemoteDataSource
import com.example.pet.domain.model.Task
import com.example.pet.domain.model.TaskEvent
import com.example.pet.domain.model.toEntity
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
    private val remoteDataSource: TaskRemoteDataSource, private val taskDao: TaskDao
) : TaskRepository {

    private val _taskEvents = MutableSharedFlow<TaskEvent>(extraBufferCapacity = 1)
    override val taskEvents: SharedFlow<TaskEvent> = _taskEvents.asSharedFlow()

    override fun getTasks(): Flow<Result<List<Task>>> {
        return taskDao.getTasks() // DAO должен возвращать Flow<List<TaskEntity>>
            .map { entities ->
                Result.success(entities.map { it.toDomain() })
            }.catch { exception ->
                emit(Result.failure(exception))
            }.flowOn(Dispatchers.IO)
    }

    override suspend fun getTaskById(taskId: String): Flow<Result<Task>> = flow {
        val tempTask: TaskEntity;
        withContext(Dispatchers.IO){
            tempTask = taskDao.getTaskById(taskId)
        }
        emit(Result.success(tempTask.toDomain()))

        /*val result = remoteDataSource.getTaskById(taskId)
        emit(result.mapCatching { it.toDomain() })*/
    }

    override suspend fun createTask(
        title: String, description: String?, day: String
    ): Flow<Result<Task>> = flow {
        val createTaskDto = com.example.pet.data.model.CreateTaskDto(
            title = title, description = description, day = day, isCompleted = false
        )

        val tempEntity = createTaskDto.toEntity();

        withContext(Dispatchers.IO) {
            taskDao.insertTask(tempEntity)
        }
        emit(Result.success(tempEntity.toDomain()))

        remoteDataSource.createTask(createTaskDto)
            .onSuccess { taskDto ->
                withContext(Dispatchers.IO) {
                    taskDao.deleteById(tempEntity.id.toString())
                    taskDao.insertTask(taskDto.toEntity())
                }
            }.onFailure { exception ->
                Log.i("exception", "TaskRepositoryImpl line 84: " + exception.message.toString())
            }

        /*val result = remoteDataSource.createTask(createTaskDto)
        result.onSuccess { taskDto ->
            //val task = taskDto.toDomain()

            withContext(Dispatchers.IO) {
                taskDao.insertTask(taskDto.toEntity())
            }

                //_taskEvents.tryEmit(TaskEvent.TaskCreated(task))
        }

        emit(result.mapCatching { it.toDomain() })*/
    }

    override suspend fun updateTask(task: Task): Flow<Result<Task>> = flow {
        withContext(Dispatchers.IO) {
            taskDao.updateTask(task.toEntity())
        }
        emit(Result.success(task))

        val taskDto = task.toDto()
        remoteDataSource.updateTask(taskDto)
            .onFailure { exception ->
                Log.i(
                    "exception",
                    "[Network error] TaskRepositoryImpl - line 101: " + exception.message
                )
            }

    }

    override suspend fun refreshTasks(): Result<Unit> {
        return remoteDataSource.getTasks().mapCatching { dtos ->
            Log.i("dtos", dtos.toString())
            withContext(Dispatchers.IO) {
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


package com.example.pet.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.pet.data.local.dao.TaskDao
import com.example.pet.data.local.entity.TaskEntity
import com.example.pet.data.mapper.toDomain
import com.example.pet.data.model.toDomain
import com.example.pet.data.model.toEntity
import com.example.pet.data.remote.GeminiTaskParser
import com.example.pet.data.remote.TaskRemoteDataSource
import com.example.pet.data.worker.SyncTasksWorker
import com.example.pet.domain.model.Task
import com.example.pet.domain.model.TaskEvent
import com.example.pet.domain.model.toDto
import com.example.pet.domain.model.toEntity
import com.example.pet.domain.repository.TaskRepository
import dagger.hilt.android.qualifiers.ApplicationContext
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Реализация репозитория для работы с задачами.
 * Координирует работу с разными источниками данных (сеть, локальная БД и т.д.).
 */
class TaskRepositoryImpl @Inject constructor(
    private val remoteDataSource: TaskRemoteDataSource,
    private val taskDao: TaskDao,
    private val parser: GeminiTaskParser,

    @param:ApplicationContext
    private val context: Context
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
        withContext(Dispatchers.IO) {
            tempTask = taskDao.getTaskById(taskId)
        }
        emit(Result.success(tempTask.toDomain()))

        /*val result = remoteDataSource.getTaskById(taskId)
        emit(result.mapCatching { it.toDomain() })*/
    }

    override suspend fun createTask(
        title: String, description: String?, day: String, startMinutes: Int, endMinutes: Int
    ): Flow<Result<Task>> = flow {
        val createTaskDto = com.example.pet.data.model.CreateTaskDto(
            title = title,
            description = description,
            day = day,
            isCompleted = false,
            startMinutes = startMinutes,
            endMinutes = endMinutes
        )

        val tempEntity = createTaskDto.toEntity();
        val entityId: Long

        withContext(Dispatchers.IO) {
            entityId = taskDao.insertTask(tempEntity)
        }
        emit(Result.success(tempEntity.toDomain()))

        remoteDataSource.createTask(createTaskDto)
            .onSuccess { taskDto ->
                val syncedTask = taskDto.copy(isSynced = true)
                withContext(Dispatchers.IO) {
                    taskDao.deleteById(entityId.toString())
                    taskDao.insertTask(syncedTask.toEntity())
                }
            }.onFailure { exception ->
                enqueueSyncWorker()
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

    private fun enqueueSyncWorker() {
        val syncRequest = OneTimeWorkRequestBuilder<SyncTasksWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "sync_tasks",                        // уникальное имя
                ExistingWorkPolicy.KEEP,             // не создавать дубликаты
                syncRequest
            )
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
        withContext(Dispatchers.IO) {
            Log.i("exception", taskId)
            taskDao.deleteById(taskId)
        }

        val result = remoteDataSource.deleteTask(taskId)
        result.onSuccess {
            // Используем tryEmit, чтобы не блокировать выполнение
            _taskEvents.tryEmit(TaskEvent.TaskDeleted(taskId))
        }
        emit(result)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun createTaskFromText(input: String): TaskEntity {
        if (parser.parse(input) == null) Log.i("ai", "ошибка создания")
        val dto = parser.parse(input) ?: throw Exception("Не удалось распарсить задачу")
        withContext(Dispatchers.IO) {
            taskDao.insertTask(dto.toEntity())
        }
        return dto.toEntity()
    }
}


package com.example.pet.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pet.data.local.dao.TaskDao
import com.example.pet.data.mapper.toCreateDto
import com.example.pet.data.remote.TaskRemoteDataSource

class SyncTasksWorker(
    context: Context,
    params: WorkerParameters,
    private val taskDao: TaskDao,
    private val remoteDataSource: TaskRemoteDataSource
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.i("SyncWorker", "Начало синхронизации")
            // Проверяем доступность БД — просто делаем запрос
            val unsyncedTasks = taskDao.getUnsyncedTasks()

            if (unsyncedTasks.isEmpty()) return Result.success()

            var hasError = false
            for (task in unsyncedTasks) {
                val result = remoteDataSource.createTask(task.toCreateDto())
                result.onSuccess {
                    taskDao.markAsSynced(task.id)
                }.onFailure {
                    hasError = true
                }
            }

            if (hasError) Result.retry() else{
                Result.success()
            }

        } catch (e: Exception) {
            // БД недоступна или другая ошибка — повторим позже
            Result.retry()
        }
    }
}
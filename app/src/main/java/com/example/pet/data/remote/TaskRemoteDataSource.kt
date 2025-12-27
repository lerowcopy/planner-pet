package com.example.pet.data.remote

import com.example.pet.data.model.TaskDto
import javax.inject.Inject

/**
 * Источник данных для получения задач из сети.
 * Обрабатывает сетевые запросы и преобразует ответы.
 */
class TaskRemoteDataSource @Inject constructor(
    private val apiService: TaskApiService
) {
    /**
     * Получить список задач из сети.
     * @return Результат с списком DTO задач
     */
    suspend fun getTasks(): Result<List<TaskDto>> {
        return try {
            val response = apiService.getTasks()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch tasks: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Получить задачу по ID из сети.
     * @param taskId ID задачи
     * @return Результат с DTO задачи
     */
    suspend fun getTaskById(taskId: String): Result<TaskDto> {
        return try {
            val response = apiService.getTaskById(taskId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch task: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Создать новую задачу в сети.
     * @param createTaskDto Данные для создания задачи
     * @return Результат с DTO созданной задачи
     */
    suspend fun createTask(createTaskDto: com.example.pet.data.model.CreateTaskDto): Result<TaskDto> {
        return try {
            val response = apiService.createTask(createTaskDto)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create task: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


package com.example.pet.data.remote

import com.example.pet.data.model.TaskDto
import kotlinx.coroutines.delay
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class TaskRemoteDataSource @Inject constructor(
    private val apiService: TaskApiService
) {
    /**
     * Получить список задач из сети с retry логикой.
     * @return Результат с списком DTO задач
     */
    suspend fun getTasks(): Result<List<TaskDto>> {
        return executeWithRetry {
            try {
                val response = apiService.getTasks()
                val body = response.body()
                
                when {
                    response.isSuccessful && body != null -> {
                        Result.success(body)
                    }
                    response.isSuccessful && body == null -> {
                        Result.failure(NetworkException.HttpError(
                            code = response.code(),
                            errorMessage = "Empty response body"
                        ))
                    }
                    else -> {
                        Result.failure(NetworkException.HttpError(
                            code = response.code(),
                            errorMessage = response.message()
                        ))
                    }
                }
            } catch (e: SocketTimeoutException) {
                Result.failure(NetworkException.TimeoutError())
            } catch (e: IOException) {
                Result.failure(NetworkException.NetworkError(e.message ?: "Connection failed"))
            } catch (e: Exception) {
                Result.failure(NetworkException.UnknownError(e.message ?: "Unknown error"))
            }
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
            val body = response.body()
            
            when {
                response.isSuccessful && body != null -> {
                    Result.success(body)
                }
                response.isSuccessful && body == null -> {
                    Result.failure(NetworkException.HttpError(
                        code = response.code(),
                        errorMessage = "Failed to create task"
                    ))
                }
                else -> {
                    Result.failure(NetworkException.HttpError(
                        code = response.code(),
                        errorMessage = response.message()
                    ))
                }
            }
        } catch (e: SocketTimeoutException) {
            Result.failure(NetworkException.TimeoutError())
        } catch (e: IOException) {
            Result.failure(NetworkException.NetworkError(e.message ?: "Connection failed"))
        } catch (e: Exception) {
            Result.failure(NetworkException.UnknownError(e.message ?: "Unknown error"))
        }
    }
    
    /**
     * Обновить задачу.
     * @param taskDto Обновленные данные задачи
     * @return Результат с обновленной задачей
     */
    suspend fun updateTask(taskDto: TaskDto): Result<TaskDto> {
        return try {
            val response = apiService.updateTask(taskDto.id, taskDto)
            val body = response.body()
            
            when {
                response.isSuccessful && body != null -> {
                    Result.success(body)
                }
                response.isSuccessful && body == null -> {
                    Result.failure(NetworkException.HttpError(
                        code = response.code(),
                        errorMessage = "Failed to update task"
                    ))
                }
                else -> {
                    Result.failure(NetworkException.HttpError(
                        code = response.code(),
                        errorMessage = response.message()
                    ))
                }
            }
        } catch (e: SocketTimeoutException) {
            Result.failure(NetworkException.TimeoutError())
        } catch (e: IOException) {
            Result.failure(NetworkException.NetworkError(e.message ?: "Connection failed"))
        } catch (e: Exception) {
            Result.failure(NetworkException.UnknownError(e.message ?: "Unknown error"))
        }
    }
    
    /**
     * Удалить задачу по ID из сети.
     * @param taskId ID задачи
     * @return Результат операции (Unit при успехе)
     */
    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            val response = apiService.deleteTask(taskId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(NetworkException.HttpError(
                    code = response.code(),
                    errorMessage = response.message()
                ))
            }
        } catch (e: SocketTimeoutException) {
            Result.failure(NetworkException.TimeoutError())
        } catch (e: IOException) {
            Result.failure(NetworkException.NetworkError(e.message ?: "Connection failed"))
        } catch (e: Exception) {
            Result.failure(NetworkException.UnknownError(e.message ?: "Unknown error"))
        }
    }
    
    /**
     * Выполнить операцию с retry логикой и экспоненциальной задержкой.
     */
    private suspend fun <T> executeWithRetry(
        times: Int = 3,
        initialDelay: Long = 1000L,
        maxDelay: Long = 5000L,
        factor: Double = 2.0,
        block: suspend () -> Result<T>
    ): Result<T> {
        var currentDelay = initialDelay
        repeat(times - 1) { attempt ->
            val result = block()
            
            // Retry только для сетевых ошибок, не для HTTP ошибок
            if (result.isSuccess || result.exceptionOrNull() !is NetworkException.NetworkError) {
                return result
            }
            
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
        
        // Последняя попытка
        return block()
    }
}


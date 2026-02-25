package com.example.pet.data.remote

import com.example.pet.data.model.CreateTaskDto
import com.example.pet.data.model.TaskDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * API сервис для работы с задачами.
 * Определяет endpoints для сетевых запросов.
 */
interface TaskApiService {
    /**
     * Получить список всех задач.
     */
    @GET("tasks")
    suspend fun getTasks(): Response<List<TaskDto>>
    
    /**
     * Получить задачу по ID.
     * @param taskId ID задачи
     */
    @GET("tasks/{id}")
    suspend fun getTaskById(@Path("id") taskId: String): Response<TaskDto>
    
    /**
     * Создать новую задачу.
     * @param createTaskDto Данные для создания задачи
     * @return Созданная задача
     */
    @POST("tasks")
    suspend fun createTask(@Body createTaskDto: CreateTaskDto): Response<TaskDto>
    
    /**
     * Обновить задачу (например, изменить статус выполнения).
     * @param taskId ID задачи
     * @param task Обновленные данные задачи
     * @return Обновленная задача
     */
    @PATCH("tasks/{id}")
    suspend fun updateTask(
        @Path("id") taskId: String,
        @Body task: TaskDto
    ): Response<TaskDto>
    
    /**
     * Удалить задачу по ID.
     * @param taskId ID задачи
     */
    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") taskId: String): Response<Unit>
}


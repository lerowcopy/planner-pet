package com.example.pet.domain.repository

import com.example.pet.data.local.entity.TaskEntity
import com.example.pet.domain.model.Task
import com.example.pet.domain.model.TaskEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Интерфейс репозитория для работы с задачами.
 * Определяет контракт для получения данных из разных источников (сеть, база данных и т.д.).
 */
interface TaskRepository {
    /**
     * Поток событий об изменениях задач.
     * Подписываясь на этот поток, можно получать уведомления о создании, обновлении или удалении задач.
     */
    val taskEvents: SharedFlow<TaskEvent>
    /**
     * Получить список всех задач.
     * @return Flow со списком задач
     */
    fun getTasks(): Flow<Result<List<Task>>>
    
    /**
     * Получить задачу по ID.
     * @param taskId ID задачи
     * @return Flow с результатом задачи
     */
    suspend fun getTaskById(taskId: String): Flow<Result<Task>>
    
    /**
     * Создать новую задачу.
     * @param title Название задачи
     * @param description Описание задачи (опционально)
     * @param day День выполнения задачи
     * @return Flow с результатом созданной задачи
     */
    suspend fun createTask(
        title: String,
        description: String? = null,
        day: String
    ): Flow<Result<Task>>
    
    /**
     * Обновить задачу.
     * @param task Обновленная задача
     * @return Flow с результатом обновленной задачи
     */
    suspend fun updateTask(task: Task): Flow<Result<Task>>

    /**
     * Загрузить задачи.
     */
    suspend fun refreshTasks(): Result<Unit>
    
    /**
     * Удалить задачу по ID.
     * @param taskId ID задачи
     * @return Flow с результатом операции (Unit при успехе)
     */
    suspend fun deleteTask(taskId: String): Flow<Result<Unit>>

    /**
     * Создать задачу через AI
     * @param input Текст для париснга через AI
     * @return Задча для создания и добавления в базу данных
     */
    suspend fun createTaskFromText(input: String): TaskEntity
}


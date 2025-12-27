package com.example.pet.domain.repository

import com.example.pet.domain.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс репозитория для работы с задачами.
 * Определяет контракт для получения данных из разных источников (сеть, база данных и т.д.).
 */
interface TaskRepository {
    /**
     * Получить список всех задач.
     * @return Flow со списком задач
     */
    suspend fun getTasks(): Flow<Result<List<Task>>>
    
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
}


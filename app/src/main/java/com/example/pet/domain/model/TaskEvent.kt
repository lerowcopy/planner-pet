package com.example.pet.domain.model

/**
 * События, связанные с изменениями задач.
 * Используется для уведомления о создании, обновлении или удалении задач.
 */
sealed interface TaskEvent {
    /**
     * Задача была создана.
     */
    data class TaskCreated(val task: Task) : TaskEvent
    
    /**
     * Задача была удалена.
     */
    data class TaskDeleted(val taskId: String) : TaskEvent
    
    /**
     * Задача была обновлена.
     */
    data class TaskUpdated(val task: Task) : TaskEvent
}


package com.example.pet.domain.model

@Deprecated("Временно не используются")
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


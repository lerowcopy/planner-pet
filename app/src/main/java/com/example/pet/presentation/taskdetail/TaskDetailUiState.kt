package com.example.pet.presentation.taskdetail

import com.example.pet.domain.model.Task

sealed interface TaskDetailUiState {
    /**
     * Инициализация / загрузка.
     */
    data object Loading : TaskDetailUiState
    
    /**
     * Успешная загрузка данных.
     * @param task Задача
     */
    data class Success(val task: Task) : TaskDetailUiState

    /**
     * Ошибка при загрузке данных.
     * @param message Сообщение об ошибке
     */
    data class Error(val message: String) : TaskDetailUiState
}


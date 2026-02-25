package com.example.pet.presentation.home

import com.example.pet.domain.model.Task

/**
 * UI состояние экрана Home.
 * Представляет все возможные состояния экрана для отображения в UI.
 */
sealed interface HomeUiState {
    /**
     * Инициализация / загрузка.
     */
    data object Loading : HomeUiState

    /**
     * Задачи из сети успешно загружены
     */
    data object RefreshSuccess : HomeUiState
    
    /**
     * Успешная загрузка данных.
     * @param tasks Список задач
     */
    data class Success(val tasks: List<Task>) : HomeUiState
    
    /**
     * Ошибка при загрузке данных.
     * @param message Сообщение об ошибке
     */
    data class Error(val message: String) : HomeUiState
}

/**
 * Одноразовые UI события для показа пользователю.
 */
sealed interface UiEvent {
    /**
     * Показать сообщение об ошибке.
     */
    data class ShowError(val message: String) : UiEvent
    
    /**
     * Показать информационное сообщение.
     */
    data class ShowMessage(val message: String) : UiEvent
}


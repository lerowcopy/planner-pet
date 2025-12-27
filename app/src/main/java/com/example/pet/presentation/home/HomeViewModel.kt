package com.example.pet.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet.domain.model.Task
import com.example.pet.domain.usecase.CreateTaskUseCase
import com.example.pet.domain.usecase.GetTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel для экрана Home.
 * Управляет состоянием UI и взаимодействует с Use Cases.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val createTaskUseCase: CreateTaskUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadTasks()
    }
    
    /**
     * Загрузить список задач.
     * @param showLoading Показывать ли состояние загрузки (по умолчанию true)
     */
    private fun loadTasks(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.value = HomeUiState.Loading
            }
            
            getTasksUseCase()
                .catch { exception ->
                    _uiState.value = HomeUiState.Error(
                        message = exception.message ?: "Неизвестная ошибка"
                    )
                }
                .collect { result ->
                    result.onSuccess { tasks ->
                        _uiState.value = HomeUiState.Success(tasks)
                    }.onFailure { exception ->
                        _uiState.value = HomeUiState.Error(
                            message = exception.message ?: "Ошибка при загрузке задач"
                        )
                    }
                }
        }
    }
    
    /**
     * Создать новую задачу (шаблонную).
     * После создания обновляет список задач без показа состояния загрузки.
     */
    fun createTask() {
        viewModelScope.launch {
            val day = SimpleDateFormat("EEEE", Locale("ru")).format(Date())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            
            val title = "Новая задача"
            val description = "Описание задачи"
            
            // Убеждаемся, что title не пустой
            if (title.isBlank()) {
                return@launch
            }
            
            createTaskUseCase(
                title = title,
                description = description,
                day = day
            )
                .catch {
                    // В случае ошибки можно показать сообщение, но не блокируем UI
                }
                .collect { result ->
                    result.onSuccess { createdTask ->
                        // После успешного создания обновляем список задач
                        // Не показываем Loading, чтобы созданная задача сразу отобразилась
                        loadTasks(showLoading = false)
                    }.onFailure {
                        // Ошибка создания - можно обработать отдельно
                    }
                }
        }
    }
}


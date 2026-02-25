package com.example.pet.presentation.taskdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet.domain.model.Task
import com.example.pet.domain.usecase.DeleteTaskUseCase
import com.example.pet.domain.usecase.GetTaskByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана детальной информации о задаче.
 */
@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<TaskDetailUiState>(TaskDetailUiState.Loading)
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()
    
    private val _deleteResult = MutableStateFlow<Result<Unit>?>(null)
    val deleteResult: StateFlow<Result<Unit>?> = _deleteResult.asStateFlow()
    
    /**
     * Загрузить задачу по ID.
     */
    fun loadTask(taskId: String) {
        viewModelScope.launch {
            _uiState.value = TaskDetailUiState.Loading
            
            getTaskByIdUseCase(taskId)
                .catch { exception ->
                    _uiState.value = TaskDetailUiState.Error(
                        message = exception.message ?: "Неизвестная ошибка"
                    )
                }
                .collect { result ->
                    result.onSuccess { task ->
                        _uiState.value = TaskDetailUiState.Success(task)
                    }.onFailure { exception ->
                        _uiState.value = TaskDetailUiState.Error(
                            message = exception.message ?: "Ошибка при загрузке задачи"
                        )
                    }
                }
        }
    }
    
    /**
     * Удалить задачу по ID.
     */
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            _deleteResult.value = null
            
            deleteTaskUseCase(taskId)
                .catch { exception ->
                    _deleteResult.value = Result.failure(
                        Exception(exception.message ?: "Неизвестная ошибка")
                    )
                }
                .collect { result ->
                    _deleteResult.value = result
                }
        }
    }
    
    /**
     * Сбросить результат удаления.
     */
    fun clearDeleteResult() {
        _deleteResult.value = null
    }
}


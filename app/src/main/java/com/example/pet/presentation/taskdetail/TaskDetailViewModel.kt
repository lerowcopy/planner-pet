package com.example.pet.presentation.taskdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet.domain.model.Task
import com.example.pet.domain.usecase.DeleteTaskUseCase
import com.example.pet.domain.usecase.GetTaskByIdUseCase
import com.example.pet.domain.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TaskDetailUiState>(TaskDetailUiState.Loading)
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TaskDetailEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<TaskDetailEvent> = _events.asSharedFlow()

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
                    result
                        .onSuccess { task ->
                            _uiState.value = TaskDetailUiState.Success(task)
                        }
                        .onFailure { exception ->
                            _uiState.value = TaskDetailUiState.Error(
                                message = exception.message ?: "Ошибка при загрузке задачи"
                            )
                        }
                }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            deleteTaskUseCase(taskId)
                .catch { exception ->
                    _events.emit(
                        TaskDetailEvent.DeleteError(
                            message = exception.message ?: "Не удалось удалить задачу"
                        )
                    )
                }
                .collect { result ->
                    result
                        .onSuccess {
                            _events.emit(TaskDetailEvent.DeleteSuccess)
                        }
                        .onFailure { exception ->
                            _events.emit(
                                TaskDetailEvent.DeleteError(
                                    message = exception.message ?: "Не удалось удалить задачу"
                                )
                            )
                        }
                }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = !task.isCompleted)
            updateTaskUseCase(updated)
                .catch { }
                .collect { result ->
                    result.onSuccess {
                        _uiState.value = TaskDetailUiState.Success(updated)
                    }
                }
        }
    }
}

sealed interface TaskDetailEvent {
    data object DeleteSuccess : TaskDetailEvent
    data class DeleteError(val message: String) : TaskDetailEvent
}
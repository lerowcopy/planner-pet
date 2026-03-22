package com.example.pet.presentation.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.pet.data.audio.SpeechToTextService
import com.example.pet.domain.model.Task
import com.example.pet.domain.repository.TaskRepository
import com.example.pet.domain.usecase.CreateTaskFromTextUseCase
import com.example.pet.domain.usecase.CreateTaskUseCase
import com.example.pet.domain.usecase.DeleteTaskUseCase
import com.example.pet.domain.usecase.GetTasksByDayUseCase
import com.example.pet.domain.usecase.GetTasksUseCase
import com.example.pet.domain.usecase.RefreshTasksUseCase
import com.example.pet.domain.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
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
 * Автоматически обновляет список задач при изменениях (создание, удаление, обновление).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val getTasksByDayUseCase: GetTasksByDayUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val refreshTasksUseCase: RefreshTasksUseCase,
    private val createTaskFromTextUseCase: CreateTaskFromTextUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,

    taskRepository: TaskRepository,

    private val speechToTextService: SpeechToTextService
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _selectedDay = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )

    val pagedTasks: Flow<PagingData<Task>> = taskRepository.getTasksPaged()
        .cachedIn(viewModelScope)


    private val _uiEvents = Channel<UiEvent>()
    val uiEvents = _uiEvents.receiveAsFlow()

    var isRecording by mutableStateOf(false)
        private set

    var isModelLoading by mutableStateOf(false)
        private set

    init {
        observeTasks()
        refreshFromNetwork()
        loadVoiceModel()

    }

    private fun loadVoiceModel() {
        viewModelScope.launch {
            isModelLoading = true
            speechToTextService.loadModel()
            isModelLoading = false
        }
    }

    fun startVoiceInput() {
        isRecording = true
        Log.i("ai", isRecording.toString())
        speechToTextService.startListening(
            onResult = { text ->
                isRecording = false
                viewModelScope.launch {
                    Log.i("ai", text)
                    createTaskFromTextUseCase(
                        text
                    )
                }
            },
            onError = { error ->
                isRecording = false
                speechToTextService.stopListening()
                Log.i("ai", error)

            }
        )
    }

    fun stopVoiceInput() {
        speechToTextService.stopListening()
        isRecording = false
    }

    override fun onCleared() {
        super.onCleared()
        speechToTextService.release()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            getTasksUseCase()
                .catch { exception ->
                    _uiState.value = HomeUiState.Error(exception.message ?: "Неизвестная ошибка")
                }
                .collect { result ->
                    result
                        .onSuccess { tasks -> _uiState.value = HomeUiState.Success(tasks) }
                        .onFailure { exception ->
                            _uiState.value = HomeUiState.Error(exception.message ?: "Ошибка")
                        }
                }
        }
    }

    private fun refreshFromNetwork(showLoading: Boolean = false) {
        viewModelScope.launch {
            if (showLoading) _uiState.value = HomeUiState.Loading
            refreshTasksUseCase()
        }
    }

    fun deleteTaskById(taskId: String) {
        viewModelScope.launch {
            Log.i("exception", "func started")
            deleteTaskUseCase(
                taskId
            ).catch { exception ->
                Log.i("exception", exception.message.toString())
            }
                .collect { result ->
                    result.onFailure { exception ->
                        Log.i("exception", exception.message.toString())
                    }
                }
        }

    }

    fun createTask(
        title: String,
        description: String? = null,
        day: String,
        startMinutes: Int = 0,
        endMinutes: Int = 60
    ) {
        viewModelScope.launch {
            if (title.isBlank()) {
                return@launch
            }

            createTaskUseCase(
                title = title,
                description = description,
                day = day,
                startMinutes = startMinutes,
                endMinutes = endMinutes
            )
                .catch { exception ->
                    _uiEvents.send(
                        UiEvent.ShowError(
                            ("[Error]HomeViewModel - line181: " + exception.message)
                        )
                    )
                }
                .collect { result ->
                    result.onSuccess {
                        _uiEvents.send(UiEvent.ShowMessage("Задача создана"))
                    }.onFailure { exception ->
                        _uiEvents.send(
                            UiEvent.ShowError(
                                ("line189: " + exception.message)
                            )
                        )
                    }
                }
        }
    }


    fun updateTaskCompletion(task: Task, isCompleted: Boolean) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = isCompleted)

            updateTaskUseCase(updatedTask)
                .catch { exception ->
                    _uiEvents.send(
                        UiEvent.ShowError(
                            "182" ?: "Ошибка при обновлении задачи"
                        )
                    )
                }
                .collect { result ->
                    result.onFailure { exception ->
                        _uiEvents.send(
                            UiEvent.ShowError(
                                "188" ?: "Ошибка при обновлении задачи"
                            )
                        )
                    }
                }
        }
    }
}


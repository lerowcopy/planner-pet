package com.example.pet.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet.domain.model.Task
import com.example.pet.domain.model.TaskEvent
import com.example.pet.domain.repository.TaskRepository
import com.example.pet.domain.usecase.CreateTaskUseCase
import com.example.pet.domain.usecase.GetTasksUseCase
import com.example.pet.domain.usecase.RefreshTasksUseCase
import com.example.pet.domain.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val refreshTasksUseCase: RefreshTasksUseCase,
    private val taskRepository: TaskRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _uiEvents = Channel<UiEvent>()
    val uiEvents = _uiEvents.receiveAsFlow()
    
    init {
        /*loadTasks()
        refreshTask(showLoading = false)*/
        observeTasks()
        refreshFromNetwork()
        
        // Подписываемся на события изменений задач для автоматического обновления списка
        /*taskRepository.taskEvents
            .onEach { event ->
                when (event) {
                    is TaskEvent.TaskCreated -> {
                        // Задача создана - обновляем список
                        refreshTasks()
                    }
                    is TaskEvent.TaskDeleted -> {
                        // Задача удалена - обновляем список
                        refreshTasks()
                    }
                    is TaskEvent.TaskUpdated -> {
                        // Задача обновлена - обновляем список
                        refreshTasks()
                    }
                }
            }
            .launchIn(viewModelScope)*/
    }
    
    /**
     * Обновить список задач без показа состояния загрузки.
     * Используется для автоматического обновления при изменениях.
     */
    /*fun refreshTasks() {
        refreshTask(showLoading = false)
        loadTasks(showLoading = false)
    }*/

    private fun observeTasks() {
        viewModelScope.launch {
            getTasksUseCase()
                .catch { exception ->
                    _uiState.value = HomeUiState.Error(exception.message ?: "Неизвестная ошибка")
                }
                .collect { result ->
                    result
                        .onSuccess { tasks -> _uiState.value = HomeUiState.Success(tasks) }
                        .onFailure { exception -> _uiState.value = HomeUiState.Error(exception.message ?: "Ошибка") }
                }
        }
    }

    private fun refreshFromNetwork(showLoading: Boolean = false) {
        viewModelScope.launch {
            if (showLoading) _uiState.value = HomeUiState.Loading
            refreshTasksUseCase() // просто пишет в БД, Flow сам обновит UI
        }
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
                        message = "93" ?: "Неизвестная ошибка"
                    )
                }
                .collect { result ->
                    result.onSuccess { tasks ->
                        _uiState.value = HomeUiState.Success(tasks)
                    }.onFailure { exception ->
                        _uiState.value = HomeUiState.Error(
                            message = "101" ?: "Ошибка при загрузке задач"
                        )
                    }
                }
        }
    }

    private fun refreshTask(showLoading: Boolean = true){
        viewModelScope.launch {
            if (showLoading) {
                _uiState.value = HomeUiState.Loading
            }

            refreshTasksUseCase()
                .onFailure { exception ->
                _uiState.value = HomeUiState.Error(
                    message = exception.message ?: "ошибка обновления задач"
                )
            }

        }
    }


    
    /**
     * Быстро создать задачу на сегодня.
     * @param title Название задачи
     */
    fun createQuickTask(title: String) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        createTask(title = title, description = null, day = today)
    }
    
    /**
     * Создать новую задачу с пользовательскими данными.
     * @param title Название задачи
     * @param description Описание задачи (опционально)
     * @param day День выполнения задачи
     * Список задач обновится автоматически через систему событий.
     */
    fun createTask(title: String, description: String? = null, day: String) {
        viewModelScope.launch {
            // Убеждаемся, что title не пустой
            if (title.isBlank()) {
                return@launch
            }
            
            createTaskUseCase(
                title = title,
                description = description,
                day = day
            )
                .catch { exception ->
                    _uiEvents.send(UiEvent.ShowError(
                        ("[Error]HomeViewModel - line181: " + exception.message)
                    ))
                }
                .collect { result ->
                    result.onSuccess {
                        _uiEvents.send(UiEvent.ShowMessage("Задача создана"))
                    }.onFailure { exception ->
                        _uiEvents.send(UiEvent.ShowError(
                            ("line189: " + exception.message)
                        ))
                    }
                }
        }
    }
    
    /**
     * Обновить статус выполнения задачи.
     * @param task Задача для обновления
     * @param isCompleted Новый статус выполнения
     */
    fun updateTaskCompletion(task: Task, isCompleted: Boolean) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = isCompleted)
            
            updateTaskUseCase(updatedTask)
                .catch { exception ->
                    _uiEvents.send(UiEvent.ShowError(
                        "182" ?: "Ошибка при обновлении задачи"
                    ))
                }
                .collect { result ->
                    result.onFailure { exception ->
                        _uiEvents.send(UiEvent.ShowError(
                            "188" ?: "Ошибка при обновлении задачи"
                        ))
                    }
                }
        }
    }
}


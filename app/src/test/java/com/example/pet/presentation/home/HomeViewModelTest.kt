package com.example.pet.presentation.home

import androidx.paging.PagingData
import app.cash.turbine.test
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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getTasksUseCase: GetTasksUseCase
    private lateinit var getTasksByDayUseCase: GetTasksByDayUseCase
    private lateinit var createTaskUseCase: CreateTaskUseCase
    private lateinit var updateTaskUseCase: UpdateTaskUseCase
    private lateinit var refreshTasksUseCase: RefreshTasksUseCase
    private lateinit var createTaskFromTextUseCase: CreateTaskFromTextUseCase
    private lateinit var deleteTaskUseCase: DeleteTaskUseCase
    private lateinit var taskRepository: TaskRepository
    private lateinit var speechToTextService: SpeechToTextService
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        getTasksUseCase = mockk()
        getTasksByDayUseCase = mockk()
        createTaskUseCase = mockk()
        updateTaskUseCase = mockk()
        refreshTasksUseCase = mockk()
        createTaskFromTextUseCase = mockk()
        deleteTaskUseCase = mockk()
        speechToTextService = mockk(relaxed = true, relaxUnitFun = true)
        taskRepository = mockk()

        every { taskRepository.taskEvents } returns MutableSharedFlow()

        every { taskRepository.getTasksPaged() } returns flowOf(PagingData.empty())

        every { getTasksUseCase() } returns flowOf(Result.success(emptyList()))

        every { getTasksByDayUseCase(any()) } returns flowOf(Result.success(emptyList()))

        coEvery { refreshTasksUseCase() } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = HomeViewModel(
        getTasksUseCase = getTasksUseCase,
        createTaskUseCase = createTaskUseCase,
        updateTaskUseCase = updateTaskUseCase,
        refreshTasksUseCase = refreshTasksUseCase,
        createTaskFromTextUseCase = createTaskFromTextUseCase,
        deleteTaskUseCase = deleteTaskUseCase,
        taskRepository = taskRepository,
        speechToTextService = speechToTextService,
        getTasksByDayUseCase = getTasksByDayUseCase
    )

    @Test
    fun `initial state is Loading`() = runTest {
        every { getTasksUseCase() } returns MutableSharedFlow()
        coEvery { refreshTasksUseCase() } returns Result.success(Unit)

        val viewModel = buildViewModel()

        assertEquals(HomeUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState emits Success with tasks when getTasks returns data`() = runTest {
        val tasks = listOf(
            Task(id = "1", title = "Задача 1", day = "2026-03-22", startMinutes = 0, endMinutes = 60),
            Task(id = "2", title = "Задача 2", day = "2026-03-22", startMinutes = 0, endMinutes = 60)
        )
        every { getTasksUseCase() } returns flowOf(Result.success(tasks))

        buildViewModel().uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            val states = cancelAndConsumeRemainingEvents()
                .filterIsInstance<app.cash.turbine.Event.Item<HomeUiState>>()
                .map { it.value }
                .filterIsInstance<HomeUiState.Success>()

            assertTrue(states.isNotEmpty())
            assertEquals(tasks, states.last().tasks)
        }
    }

    @Test
    fun `uiState emits Error when getTasks returns failure`() = runTest {
        every { getTasksUseCase() } returns flowOf(
            Result.failure(Exception("Ошибка сети"))
        )

        buildViewModel().uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            val states = cancelAndConsumeRemainingEvents()
                .filterIsInstance<app.cash.turbine.Event.Item<HomeUiState>>()
                .map { it.value }
                .filterIsInstance<HomeUiState.Error>()

            assertTrue(states.isNotEmpty())
            assertEquals("Ошибка сети", states.last().message)
        }
    }


    @Test
    fun `createTask does nothing when title is blank`() = runTest {
        val viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.createTask(title = "   ", day = "2026-03-22")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { createTaskUseCase(any(), any(), any()) }
    }

    @Test
    fun `createTask sends ShowMessage event on success`() = runTest {
        coEvery {
            createTaskUseCase(any(), any(), any())
        } returns flowOf(Result.success(
            Task(id = "1", title = "Тест", day = "2026-03-22", startMinutes = 0, endMinutes = 60)
        ))

        val viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEvents.test {
            viewModel.createTask(title = "Тест", day = "2026-03-22")
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is UiEvent.ShowMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateTaskCompletion calls updateTaskUseCase with correct isCompleted`() = runTest {
        val task = Task(id = "1", title = "Тест", day = "2026-03-22", startMinutes = 0, endMinutes = 60, isCompleted = false)
        coEvery { updateTaskUseCase(any()) } returns flowOf(Result.success(task))

        val viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateTaskCompletion(task, isCompleted = true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            updateTaskUseCase(match { it.isCompleted == true && it.id == "1" })
        }
    }
}
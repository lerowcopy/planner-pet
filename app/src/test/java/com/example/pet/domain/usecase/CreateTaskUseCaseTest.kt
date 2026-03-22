package com.example.pet.domain.usecase

import com.example.pet.domain.model.Task
import com.example.pet.domain.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CreateTaskUseCaseTest {
    private lateinit var repository: TaskRepository
    private lateinit var useCase: CreateTaskUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = CreateTaskUseCase(repository)
    }

    @Test
    fun `invoke returns success when repository creates task`() = runTest {
        val expectedTask = Task(
            id = "1",
            title = "Купить продукты",
            description = null,
            startMinutes = 0,
            endMinutes = 60,
            day = "2024-01-15"
        )
        coEvery {
            repository.createTask(
                title = "Купить продукты",
                description = null,
                day = "2026-01-15",
                startMinutes = 0,
                endMinutes = 60
            )
        } returns flowOf(Result.success(expectedTask))

        val result = useCase(
            title = "Купить продукты",
            description = null,
            day = "2026-01-15",
            startMinutes = 0,
            endMinutes = 60
        ).first()

        assertTrue(result.isSuccess)
        assertEquals(expectedTask, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository throws`() = runTest {
        val exception = Exception("Нет соединения")
        coEvery {
            repository.createTask(any(), any(), any(), any(), any())
        } returns flowOf(Result.failure(exception))

        val result = useCase(
            title = "Купить продукты",
            description = null,
            day = "2026-01-15"
        ).first()

        assertTrue(result.isFailure)
        assertEquals("Нет соединения", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke calls repository with correct parameters`() = runTest {
        coEvery {
            repository.createTask(any(), any(), any(), any(), any())
        } returns flowOf(
            Result.success(
                Task(
                    id = "1",
                    title = "Тест",
                    day = "2026-01-15",
                    startMinutes = 0,
                    endMinutes = 60
                )
            )
        )

        // Act
        useCase(
            title = "Тест",
            description = "Описание",
            day = "2026-01-15",
            startMinutes = 0,
            endMinutes = 60
        ).first()

        // Assert — проверяем что репозиторий вызвали с правильными параметрами
        coVerify {
            repository.createTask(
                title = "Тест",
                description = "Описание",
                day = "2026-01-15",
                startMinutes = 0,
                endMinutes = 60
            )
        }
    }
}
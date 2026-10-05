package com.focusguard.domain.usecase

import com.focusguard.fakes.FakeTaskRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AddTaskUseCaseTest {

    @Test
    fun `addTask with valid title succeeds`() = runTest {
        val repository = FakeTaskRepository()
        val useCase = AddTaskUseCase(repository)

        val id = useCase("  Study Kotlin  ")
        assertEquals(1L, id)
        assertEquals("Study Kotlin", repository.tasks.value.single().title)
    }

    @Test
    fun `addTask with blank title returns invalid id -1`() = runTest {
        val repository = FakeTaskRepository()
        val useCase = AddTaskUseCase(repository)

        val id = useCase("   ")
        assertEquals(-1L, id)
        assertEquals(0, repository.tasks.value.size)
    }
}

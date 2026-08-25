package com.focusguard.domain.usecase

import com.focusguard.domain.model.Task
import com.focusguard.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeTaskRepository : TaskRepository {
    private val tasks = mutableListOf<Task>()

    override fun getActiveTasks(): Flow<List<Task>> {
        return flowOf(tasks.filter { !it.isCompleted })
    }

    override fun getAllTasks(): Flow<List<Task>> {
        return flowOf(tasks.toList())
    }

    override suspend fun addTask(title: String): Long {
        val newId = (tasks.size + 1).toLong()
        tasks.add(Task(id = newId, title = title))
        return newId
    }

    override suspend fun completeTask(taskId: Long) {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            tasks[index] = tasks[index].copy(isCompleted = true, completedAt = System.currentTimeMillis())
        }
    }

    override suspend fun deleteTask(taskId: Long) {
        tasks.removeAll { it.id == taskId }
    }
}

class AddTaskUseCaseTest {

    @Test
    fun `addTask with valid title succeeds`() = runTest {
        val repository = FakeTaskRepository()
        val useCase = AddTaskUseCase(repository)

        val id = useCase("  Study Kotlin  ")
        assertEquals(1L, id)
    }

    @Test
    fun `addTask with blank title returns invalid id -1`() = runTest {
        val repository = FakeTaskRepository()
        val useCase = AddTaskUseCase(repository)

        val id = useCase("   ")
        assertEquals(-1L, id)
    }
}

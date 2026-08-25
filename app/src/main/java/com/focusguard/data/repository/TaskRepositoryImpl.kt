package com.focusguard.data.repository

import com.focusguard.data.local.dao.TaskDao
import com.focusguard.data.local.entity.TaskEntity
import com.focusguard.domain.model.Task
import com.focusguard.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getActiveTasks(): Flow<List<Task>> {
        return taskDao.getActiveTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addTask(title: String): Long {
        return taskDao.insertTask(TaskEntity(title = title))
    }

    override suspend fun completeTask(taskId: Long) {
        taskDao.markAsCompleted(taskId)
    }

    override suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTask(taskId)
    }

    private fun TaskEntity.toDomain(): Task = Task(
        id = id,
        title = title,
        isCompleted = isCompleted,
        createdAt = createdAt,
        completedAt = completedAt
    )
}

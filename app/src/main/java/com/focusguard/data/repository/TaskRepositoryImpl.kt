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

    override fun getActiveTasks(): Flow<List<Task>> =
        taskDao.getActiveTasks().map { entities -> entities.map { it.toDomain() } }

    override fun getAllTasks(): Flow<List<Task>> =
        taskDao.getAllTasks().map { entities -> entities.map { it.toDomain() } }

    override fun getTasksCompletedSince(since: Long): Flow<List<Task>> =
        taskDao.getTasksCompletedSince(since).map { entities -> entities.map { it.toDomain() } }

    override suspend fun addTask(title: String): Long = taskDao.insertTask(TaskEntity(title = title))

    override suspend fun completeTask(taskId: Long) = taskDao.markAsCompleted(taskId)

    override suspend fun reopenTask(taskId: Long) = taskDao.markAsActive(taskId)

    override suspend fun deleteTask(taskId: Long) = taskDao.deleteTask(taskId)

    override suspend fun restoreTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }

    override suspend fun countCompletedBetween(from: Long, to: Long): Int =
        taskDao.countCompletedBetween(from, to)
}

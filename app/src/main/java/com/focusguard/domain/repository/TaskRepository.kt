package com.focusguard.domain.repository

import com.focusguard.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getActiveTasks(): Flow<List<Task>>
    fun getAllTasks(): Flow<List<Task>>
    fun getTasksCompletedSince(since: Long): Flow<List<Task>>
    suspend fun addTask(title: String): Long
    suspend fun completeTask(taskId: Long)
    suspend fun reopenTask(taskId: Long)
    suspend fun deleteTask(taskId: Long)
    suspend fun restoreTask(task: Task)
    suspend fun countCompletedBetween(from: Long, to: Long): Int
}

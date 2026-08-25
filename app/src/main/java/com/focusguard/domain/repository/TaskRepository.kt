package com.focusguard.domain.repository

import com.focusguard.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getActiveTasks(): Flow<List<Task>>
    fun getAllTasks(): Flow<List<Task>>
    suspend fun addTask(title: String): Long
    suspend fun completeTask(taskId: Long)
    suspend fun deleteTask(taskId: Long)
}

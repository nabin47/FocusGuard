package com.focusguard.fakes

import com.focusguard.domain.model.BlockedApp
import com.focusguard.domain.model.FocusSession
import com.focusguard.domain.model.Task
import com.focusguard.domain.repository.BlockedAppRepository
import com.focusguard.domain.repository.SessionHistoryRepository
import com.focusguard.domain.repository.TaskRepository
import com.focusguard.domain.util.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.ZoneId
import java.time.ZoneOffset

class FakeTimeProvider(var now: Long = 0L) : TimeProvider {
    override fun now(): Long = now
    override fun zone(): ZoneId = ZoneOffset.UTC
}

class FakeTaskRepository(private val time: FakeTimeProvider = FakeTimeProvider()) : TaskRepository {
    val tasks = MutableStateFlow<List<Task>>(emptyList())
    private var nextId = 1L

    override fun getActiveTasks(): Flow<List<Task>> = tasks.map { list -> list.filter { !it.isCompleted } }

    override fun getAllTasks(): Flow<List<Task>> = tasks

    override fun getTasksCompletedSince(since: Long): Flow<List<Task>> =
        tasks.map { list -> list.filter { it.isCompleted && (it.completedAt ?: 0) >= since } }

    override suspend fun addTask(title: String): Long {
        val id = nextId++
        tasks.update { it + Task(id = id, title = title, createdAt = time.now) }
        return id
    }

    override suspend fun completeTask(taskId: Long) {
        tasks.update { list ->
            list.map { if (it.id == taskId) it.copy(isCompleted = true, completedAt = time.now) else it }
        }
    }

    override suspend fun reopenTask(taskId: Long) {
        tasks.update { list ->
            list.map { if (it.id == taskId) it.copy(isCompleted = false, completedAt = null) else it }
        }
    }

    override suspend fun deleteTask(taskId: Long) {
        tasks.update { list -> list.filterNot { it.id == taskId } }
    }

    override suspend fun restoreTask(task: Task) {
        tasks.update { list -> list.filterNot { it.id == task.id } + task }
    }

    override suspend fun countCompletedBetween(from: Long, to: Long): Int =
        tasks.value.count { it.isCompleted && (it.completedAt ?: -1) in from..to }
}

class FakeBlockedAppRepository : BlockedAppRepository {
    val apps = MutableStateFlow<List<BlockedApp>>(emptyList())

    override fun getBlockedApps(): Flow<List<BlockedApp>> = apps

    override suspend fun addBlockedApp(packageName: String, appName: String) {
        apps.update { it + BlockedApp(packageName, appName) }
    }

    override suspend fun removeBlockedApp(packageName: String) {
        apps.update { list -> list.filterNot { it.packageName == packageName } }
    }
}

class FakeSessionHistoryRepository : SessionHistoryRepository {
    val sessions = MutableStateFlow<List<FocusSession>>(emptyList())
    private var nextId = 1L

    override fun getSessions(): Flow<List<FocusSession>> = sessions

    override suspend fun getActiveSession(): FocusSession? = sessions.value.firstOrNull { it.isActive }

    override suspend fun startSession(startedAt: Long): Long {
        val id = nextId++
        sessions.update { it + FocusSession(id = id, startedAt = startedAt) }
        return id
    }

    override suspend fun updateBlockedAttempts(blockedAttempts: Int) {
        sessions.update { list -> list.map { if (it.isActive) it.copy(blockedAttempts = blockedAttempts) else it } }
    }

    override suspend fun endActiveSession(
        endedAt: Long,
        tasksCompleted: Int,
        blockedAttempts: Int,
        completedAllTasks: Boolean
    ) {
        sessions.update { list ->
            list.map {
                if (it.isActive) {
                    it.copy(
                        endedAt = endedAt,
                        tasksCompleted = tasksCompleted,
                        blockedAttempts = blockedAttempts,
                        completedAllTasks = completedAllTasks
                    )
                } else {
                    it
                }
            }
        }
    }
}

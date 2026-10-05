package com.focusguard.domain.repository

import com.focusguard.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow

/** Persistent record of focus sessions, used for time tracking and stats. */
interface SessionHistoryRepository {
    fun getSessions(): Flow<List<FocusSession>>
    suspend fun getActiveSession(): FocusSession?
    suspend fun startSession(startedAt: Long): Long
    suspend fun updateBlockedAttempts(blockedAttempts: Int)
    suspend fun endActiveSession(
        endedAt: Long,
        tasksCompleted: Int,
        blockedAttempts: Int,
        completedAllTasks: Boolean
    )
}

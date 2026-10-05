package com.focusguard.data.repository

import com.focusguard.data.local.dao.FocusSessionDao
import com.focusguard.data.local.entity.FocusSessionEntity
import com.focusguard.domain.model.FocusSession
import com.focusguard.domain.repository.SessionHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessionHistoryRepositoryImpl @Inject constructor(
    private val focusSessionDao: FocusSessionDao
) : SessionHistoryRepository {

    override fun getSessions(): Flow<List<FocusSession>> =
        focusSessionDao.getSessions().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getActiveSession(): FocusSession? =
        focusSessionDao.getActiveSession()?.toDomain()

    override suspend fun startSession(startedAt: Long): Long =
        focusSessionDao.insertSession(FocusSessionEntity(startedAt = startedAt))

    override suspend fun updateBlockedAttempts(blockedAttempts: Int) =
        focusSessionDao.updateActiveBlockedAttempts(blockedAttempts)

    override suspend fun endActiveSession(
        endedAt: Long,
        tasksCompleted: Int,
        blockedAttempts: Int,
        completedAllTasks: Boolean
    ) = focusSessionDao.endActiveSessions(endedAt, tasksCompleted, blockedAttempts, completedAllTasks)
}

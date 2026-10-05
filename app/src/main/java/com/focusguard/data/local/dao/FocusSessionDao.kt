package com.focusguard.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.focusguard.data.local.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    fun getSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE endedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveSession(): FocusSessionEntity?

    @Insert
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("UPDATE focus_sessions SET blockedAttempts = :blockedAttempts WHERE endedAt IS NULL")
    suspend fun updateActiveBlockedAttempts(blockedAttempts: Int)

    @Query(
        "UPDATE focus_sessions SET endedAt = :endedAt, tasksCompleted = :tasksCompleted, " +
            "blockedAttempts = :blockedAttempts, completedAllTasks = :completedAllTasks WHERE endedAt IS NULL"
    )
    suspend fun endActiveSessions(
        endedAt: Long,
        tasksCompleted: Int,
        blockedAttempts: Int,
        completedAllTasks: Boolean
    )
}
